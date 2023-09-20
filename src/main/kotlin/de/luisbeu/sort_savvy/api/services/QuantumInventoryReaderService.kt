package de.luisbeu.sort_savvy.api.services

import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity
import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.api.dtos.Coordinates
import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderContext
import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedBase
import de.luisbeu.sort_savvy.api.dtos.responses.QuantumInventoryReaderErrorResponse
import de.luisbeu.sort_savvy.api.dtos.responses.QuantumInventoryReaderResponse
import de.luisbeu.sort_savvy.api.dtos.responses.interfaces.QuantumInventoryReaderResponseBase
import de.luisbeu.sort_savvy.api.exceptions.NoBlockEntityFoundToScan
import de.luisbeu.sort_savvy.api.exceptions.QuantumInventoryReaderNotFound
import de.luisbeu.sort_savvy.api.exceptions.UnsupportedBlockEntityFoundToScan
import de.luisbeu.sort_savvy.persistent.PositionalContext
import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

object QuantumInventoryReaderService {
    private fun scanBlockEntityFromScannerPos(
        positionalContext: PositionalContext,
    ): QuantumInventoryReaderScannedBase {
        val server = SortSavvy.LifecycleGlobals.getMinecraftServer()
        val (x, y, z, toScanDirection, worldRegistryKey) = positionalContext

        // Check the block pos above the scanner
        val potentialInventoryPos = BlockPos(x, y, z).offset(toScanDirection)

        // Go over the chunk to also handle unloaded chunks
        val chunkPos = ChunkPos(potentialInventoryPos)
        val chunk = server.getWorld(worldRegistryKey)?.getChunk(chunkPos.x, chunkPos.z) ?: run {
            SortSavvy.logger.error("Could not get chunk for x=${chunkPos.x} z=${chunkPos.z}")
            throw Exception() // TODO: ex
        }

        val blockEntity = chunk.getBlockEntity(potentialInventoryPos) ?: run {
            SortSavvy.logger.warn("Could not get block entity for x=${potentialInventoryPos.x} y=${potentialInventoryPos.y} z=${potentialInventoryPos.z}")
            throw NoBlockEntityFoundToScan(Coordinates(x, y, z))
        }

        when (blockEntity) {
            is ChestBlockEntity -> {
                // Handle double chests by determining the other chest half
                val facing = ChestBlock.getFacing(blockEntity.cachedState)

                val potentialDoublePos = blockEntity.pos.offset(facing)

                // Get it again from the chunk for unloaded chunks
                val potentialDoubleBlockEntity = chunk.getBlockEntity(potentialDoublePos)

                if (potentialDoubleBlockEntity is ChestBlockEntity && ChestBlock.getFacing(potentialDoubleBlockEntity.cachedState) == facing.opposite) {
                    return InventoryService.scanDoubleChestEntity(blockEntity, potentialDoubleBlockEntity)
                }

                return InventoryService.scanChestEntity(blockEntity)
            }
            is ItemVaultBlockEntity -> {
                // TODO:
                throw NotImplementedError()
            }
            else -> {
                SortSavvy.logger.warn("Found a unsupported block entity for x=${potentialInventoryPos.x} y=${potentialInventoryPos.y} z=${potentialInventoryPos.z}")
                throw UnsupportedBlockEntityFoundToScan(Coordinates(x, y, z))
            }
        }
    }

    fun getInventoryContentByQuantumInventoryReaderId(quantumInventoryReaderId: String): QuantumInventoryReaderResponseBase {
        // Read the saved data
        val persistentManager = SortSavvy.LifecycleGlobals.getPersistentManager()

        // Try to get the data by id form the persisted data. Throw if not found
        val  positionalContext = persistentManager.getQuantumInventoryReaderData()[quantumInventoryReaderId] ?: throw QuantumInventoryReaderNotFound(quantumInventoryReaderId)

        val quantumInventoryReaderContext = QuantumInventoryReaderContext(
            quantumInventoryReaderId,
            positionalContext.x,
            positionalContext.y,
            positionalContext.z,
            positionalContext.toScanDirection,
            positionalContext.worldRegistryKey
        )

        try {
            val scannedBase = scanBlockEntityFromScannerPos(positionalContext)

            return QuantumInventoryReaderResponse(quantumInventoryReaderContext, scannedBase)
        } catch (error: Exception) {
            if (error is NoBlockEntityFoundToScan) {
                return QuantumInventoryReaderErrorResponse(
                    quantumInventoryReaderContext,
                    "no-block-entity-found-to-scan",
                    "No block entity was found at the to scan direction."
                )
            }

            if (error is UnsupportedBlockEntityFoundToScan) {
                return QuantumInventoryReaderErrorResponse(
                    quantumInventoryReaderContext,
                    "unsupported-block-entity-found-to-scan",
                    "An unsupported block entity was found at the to scan direction."
                )
            }

            throw error
        }
    }

    fun getAllInventoryContentsFromQuantumInventoryReaders(): List<QuantumInventoryReaderResponseBase> {
        // Read the saved data
        val persistentManager = SortSavvy.LifecycleGlobals.getPersistentManager()

        // Get all data entries from the persisted data
        val  quantumInventoryReaderData = persistentManager.getQuantumInventoryReaderData()

        // Construct an empty list of the data class we want to return
        val quantumInventoryReaderResponses = mutableListOf<QuantumInventoryReaderResponseBase>()

        // Read and loop over our saved data
        for ((quantumInventoryReaderId) in quantumInventoryReaderData) {
            // Add it to the return list
            quantumInventoryReaderResponses.add(this.getInventoryContentByQuantumInventoryReaderId(quantumInventoryReaderId))
        }

        // Return empty or not
        return quantumInventoryReaderResponses
    }
}