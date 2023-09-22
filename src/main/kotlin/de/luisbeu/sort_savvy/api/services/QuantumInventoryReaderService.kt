package de.luisbeu.sort_savvy.api.services

import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity
import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.api.dtos.CoordinatesDto
import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderContextDto
import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedInterface
import de.luisbeu.sort_savvy.api.dtos.responses.QuantumInventoryReaderErrorResponseDto
import de.luisbeu.sort_savvy.api.dtos.responses.QuantumInventoryReaderResponseDto
import de.luisbeu.sort_savvy.api.dtos.responses.interfaces.QuantumInventoryReaderResponseInterface
import de.luisbeu.sort_savvy.api.exceptions.NoBlockEntityFoundToScanException
import de.luisbeu.sort_savvy.api.exceptions.QuantumInventoryReaderNotFoundException
import de.luisbeu.sort_savvy.api.exceptions.UnsupportedBlockEntityFoundToScanException
import de.luisbeu.sort_savvy.persistence.PersistentManager
import de.luisbeu.sort_savvy.persistence.PositionalContext
import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.RegistryKey

object QuantumInventoryReaderService {
    private fun scanBlockEntityFromScannerPos(
        positionalContext: PositionalContext,
    ): QuantumInventoryReaderScannedInterface {
        val server = SortSavvy.LifecycleGlobals.getMinecraftServer()
        val (x, y, z, toScanDirection, worldRegistryKey) = positionalContext

        // Check the block pos above the scanner
        val potentialInventoryPos = BlockPos(x, y, z).offset(toScanDirection)

        // Go over the chunk to also handle unloaded chunks
        val chunkPos = ChunkPos(potentialInventoryPos)

        // Retrieve the world and chunk based on the offseted pos
        val world = server.getWorld(RegistryKey.of(Identifier(worldRegistryKey.registryId), Identifier(worldRegistryKey.valueId))) ?: run {
            val msg = "Could not get ${worldRegistryKey.valueId} for for $potentialInventoryPos"
            SortSavvy.logger.error(msg)
            throw Exception(msg)
        }

        val chunk = world.getChunk(chunkPos.x, chunkPos.z)

        val blockEntity = chunk.getBlockEntity(potentialInventoryPos) ?: run {
            SortSavvy.logger.warn("Could not get block entity for $potentialInventoryPos")
            throw NoBlockEntityFoundToScanException(CoordinatesDto(x, y, z))
        }

        // Check what entity we are dealing with to decide what handler to call
        when (blockEntity) {
            is ChestBlockEntity -> {
                // Handle double chests by determining the other chest half
                val facing = ChestBlock.getFacing(blockEntity.cachedState)

                val potentialDoublePos = blockEntity.pos.offset(facing)

                // Get it again from the chunk for unloaded chunks
                val potentialDoubleBlockEntity = chunk.getBlockEntity(potentialDoublePos)

                if (potentialDoubleBlockEntity is ChestBlockEntity && ChestBlock.getFacing(potentialDoubleBlockEntity.cachedState) == facing.opposite) {
                    return StorageBlockEntityService.scanDoubleChestEntity(blockEntity, potentialDoubleBlockEntity)
                }

                return StorageBlockEntityService.scanChestEntity(blockEntity)
            }
            is ItemVaultBlockEntity -> {
                return StorageBlockEntityService.scanItemVaultEntity(blockEntity)
            }
            else -> {
                SortSavvy.logger.warn("Found a unsupported block entity for $potentialInventoryPos")
                throw UnsupportedBlockEntityFoundToScanException(CoordinatesDto(x, y, z))
            }
        }
    }

    fun getInventoryContentByQuantumInventoryReaderId(quantumInventoryReaderId: String): QuantumInventoryReaderResponseInterface {
        // Try to get the data by id form the persisted data. Throw if not found
        val  positionalContext = PersistentManager.getQuantumInventoryReaderData()[quantumInventoryReaderId] ?: throw QuantumInventoryReaderNotFoundException(quantumInventoryReaderId)

        // Already construct out context class to have it in the try and catch
        val quantumInventoryReaderContextDto = QuantumInventoryReaderContextDto(
            quantumInventoryReaderId,
            positionalContext.x,
            positionalContext.y,
            positionalContext.z,
            positionalContext.toScanDirection,
            positionalContext.worldRegistryKey.valueId
        )

        try {
            val scannedBase = scanBlockEntityFromScannerPos(positionalContext)

            return QuantumInventoryReaderResponseDto(quantumInventoryReaderContextDto, scannedBase)
        } catch (error: Exception) {
            // Some exception need to be hydrated with additional context
            if (error is NoBlockEntityFoundToScanException) {
                return QuantumInventoryReaderErrorResponseDto(
                    quantumInventoryReaderContextDto,
                    "no-block-entity-found-to-scan",
                    "No block entity was found at the to scan direction."
                )
            }

            if (error is UnsupportedBlockEntityFoundToScanException) {
                return QuantumInventoryReaderErrorResponseDto(
                    quantumInventoryReaderContextDto,
                    "unsupported-block-entity-found-to-scan",
                    "An unsupported block entity was found at the to scan direction."
                )
            }

            throw error
        }
    }

    fun getAllInventoryContentsFromQuantumInventoryReaders(): List<QuantumInventoryReaderResponseInterface> {
        // Get all data entries from the persisted data
        val  quantumInventoryReaderData = PersistentManager.getQuantumInventoryReaderData()

        // Construct an empty list of the data class we want to return
        val quantumInventoryReaderResponses = mutableListOf<QuantumInventoryReaderResponseInterface>()

        // Read and loop over our saved data
        for ((quantumInventoryReaderId) in quantumInventoryReaderData) {
            // Add it to the return list
            quantumInventoryReaderResponses.add(this.getInventoryContentByQuantumInventoryReaderId(quantumInventoryReaderId))
        }

        // Return empty or not
        return quantumInventoryReaderResponses
    }
}