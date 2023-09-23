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
    fun getInventoryContentByQuantumInventoryReaderId(quantumInventoryReaderId: String): QuantumInventoryReaderResponseInterface {
        // Try to get the data by id form the persisted data. Throw if not found
        val positionalContext = PersistentManager.getQuantumInventoryReaderData()[quantumInventoryReaderId] ?: throw QuantumInventoryReaderNotFoundException(quantumInventoryReaderId)

        return scanWithScannerById(quantumInventoryReaderId, positionalContext)
    }

    fun getAllInventoryContentsFromQuantumInventoryReaders(): List<QuantumInventoryReaderResponseInterface> {
        // Get all data entries from the persisted data
        val quantumInventoryReaderData = PersistentManager.getQuantumInventoryReaderData()

        return quantumInventoryReaderData.mapNotNull { (quantumInventoryReaderId, positionalContext) ->
            scanWithScannerById(quantumInventoryReaderId, positionalContext)
        }
    }

    private fun scanWithScannerById(quantumInventoryReaderId: String, positionalContext: PositionalContext): QuantumInventoryReaderResponseInterface =
        try {
            // Already construct out context class to have it in the try and catch
            val quantumInventoryReaderContextDto = QuantumInventoryReaderContextDto(
                quantumInventoryReaderId,
                positionalContext.x,
                positionalContext.y,
                positionalContext.z,
                positionalContext.toScanDirection,
                positionalContext.worldRegistryKey.valueId
            )

            val scannedBase = scanBlockEntityFromScannerPos(positionalContext)

            QuantumInventoryReaderResponseDto(quantumInventoryReaderContextDto, scannedBase)
        } catch (error: NoBlockEntityFoundToScanException) {
            QuantumInventoryReaderErrorResponseDto(
                QuantumInventoryReaderContextDto(
                    quantumInventoryReaderId,
                    positionalContext.x,
                    positionalContext.y,
                    positionalContext.z,
                    positionalContext.toScanDirection,
                    positionalContext.worldRegistryKey.valueId
                ),
                "no-block-entity-found-to-scan",
                "No block entity was found at the to scan direction."
            )
        } catch (error: UnsupportedBlockEntityFoundToScanException) {
            QuantumInventoryReaderErrorResponseDto(
                QuantumInventoryReaderContextDto(
                    quantumInventoryReaderId,
                    positionalContext.x,
                    positionalContext.y,
                    positionalContext.z,
                    positionalContext.toScanDirection,
                    positionalContext.worldRegistryKey.valueId
                ),
                "unsupported-block-entity-found-to-scan",
                "An unsupported block entity was found at the to scan direction."
            )
        }

    private fun scanBlockEntityFromScannerPos(positionalContext: PositionalContext): QuantumInventoryReaderScannedInterface {
        val server = SortSavvy.LifecycleGlobals.getMinecraftServer()
        val (x, y, z, toScanDirection, worldRegistryKey) = positionalContext

        // Get the block pos of the block to scann
        val potentialInventoryPos = BlockPos(x, y, z).offset(toScanDirection)

        // Go over chunks to also handle unloaded chunks
        val chunkPos = ChunkPos(potentialInventoryPos)

        val world = server.getWorld(RegistryKey.of(Identifier(worldRegistryKey.registryId), Identifier(worldRegistryKey.valueId))) ?: throw Exception("Could not get ${worldRegistryKey.valueId} for for $potentialInventoryPos")

        val chunk = world.getChunk(chunkPos.x, chunkPos.z)

        val blockEntity = chunk.getBlockEntity(potentialInventoryPos) ?: run {
            SortSavvy.logger.warn("Could not get block entity for $potentialInventoryPos")
            throw NoBlockEntityFoundToScanException(CoordinatesDto(x, y, z))
        }

        return when (blockEntity) {
            is ChestBlockEntity -> {
                // Handle double chests by determining the other chest half
                val facing = ChestBlock.getFacing(blockEntity.cachedState)

                // Get the potential pos of this offset
                val potentialDoublePos = blockEntity.pos.offset(facing)

                val potentialDoubleBlockEntity = chunk.getBlockEntity(potentialDoublePos)

                if (potentialDoubleBlockEntity is ChestBlockEntity && ChestBlock.getFacing(potentialDoubleBlockEntity.cachedState) == facing.opposite) {
                    StorageBlockEntityService.scanDoubleChestEntity(blockEntity, potentialDoubleBlockEntity)
                } else {
                    StorageBlockEntityService.scanChestEntity(blockEntity)
                }
            }
            is ItemVaultBlockEntity -> StorageBlockEntityService.scanItemVaultEntity(blockEntity)
            else -> {
                SortSavvy.logger.warn("Found a unsupported block entity for $potentialInventoryPos")
                throw UnsupportedBlockEntityFoundToScanException(CoordinatesDto(x, y, z))
            }
        }
    }
}