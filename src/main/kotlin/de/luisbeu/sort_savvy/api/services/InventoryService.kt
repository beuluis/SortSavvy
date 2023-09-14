package de.luisbeu.sort_savvy.api.services

import de.luisbeu.sort_savvy.api.dtos.Coordinates
import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderScannedContent
import de.luisbeu.sort_savvy.util.PositionWithToScanDirection
import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.DoubleInventory
import net.minecraft.inventory.Inventory
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

object InventoryService {
    // TODO: support vaults
    // TODO: adapt new data structure for better expandability
    fun getInventoryContents(inventory: Inventory): List<QuantumInventoryReaderScannedContent> {
        return generateSequence(0) { it + 1 }
            .take(inventory.size())
            .map { inventory.getStack(it) }
            .filterNot { it.isEmpty }
            .map { stack ->
                // Get ID
                val itemId = Registry.ITEM.getId(stack.item).toString()
                // Get count
                val count = stack.count
                // Get category
                val category = stack.item.group?.name
                // Check if it can take damage and return the durability. If not return null will result in undefined in the JSON object
                val durability = if (stack.isDamageable) stack.maxDamage - stack.damage else null
                // Check if it can take damage and return it. If not return null will result in undefined in the JSON object
                val damage = if (stack.isDamageable) stack.damage else null
                // Check if it has enchantments and return them. If not return null will result in undefined in the JSON object
                val enchantments = if (stack.hasEnchantments()) stack.enchantments.toList() else null
                // Construct the data class
                QuantumInventoryReaderScannedContent(itemId, count, category, durability, damage, enchantments)
            }
            .toList()
    }

    // TODO: support vaults
    fun getInventoryEntityFromScannerPos(
        server: MinecraftServer,
        positionWithToScanDirection: PositionWithToScanDirection,
    ): Pair<Inventory?, Pair<Coordinates, Coordinates?>> {
        val (x, y, z, toScanDirection) = positionWithToScanDirection

        // Check the block pos above the scanner
        val potentialInventoryPos = BlockPos(x, y, z).offset(toScanDirection)

        // Go over the chunk to also handle unloaded chunks
        val chunkPos = ChunkPos(potentialInventoryPos)
        val chunk = server.getWorld(World.OVERWORLD)?.getChunk(chunkPos.x, chunkPos.z)

        // Check if we have a chunk
        if (chunk != null) {
            // Get our potential chest block entity
            val blockEntity = chunk.getBlockEntity(potentialInventoryPos)

            // Check if we have an entity with an inventory
            if (blockEntity is ChestBlockEntity) {
                // Handle double chests by determining the other chest half
                val facing = ChestBlock.getFacing(blockEntity.cachedState)
                val potentialDoublePos = potentialInventoryPos.offset(facing)
                // Get it again from the chunk for unloaded chunks
                val potentialDoubleBlockEntity = chunk.getBlockEntity(potentialDoublePos)

                // Check if we have an inventory again and if we are facing correctly
                if (potentialDoubleBlockEntity is ChestBlockEntity && ChestBlock.getFacing(potentialDoubleBlockEntity.cachedState) == facing.opposite) {
                    val doubleInventory = DoubleInventory(blockEntity, potentialDoubleBlockEntity)
                    return Pair(
                        doubleInventory,
                        Pair(
                            Coordinates(potentialInventoryPos.x, potentialInventoryPos.y, potentialInventoryPos.z),
                            Coordinates(potentialDoublePos.x, potentialDoublePos.y, potentialDoublePos.z)
                        )
                    )
                }
            }

            // Handle single inventories
            if (blockEntity is Inventory) {
                return Pair(
                    blockEntity,
                    Pair(Coordinates(potentialInventoryPos.x, potentialInventoryPos.y, potentialInventoryPos.z), null)
                )
            }
        }

        // Nothing found or checks failed, return null for inventory and only the first position
        // TODO: maybe throw?
        return Pair(
            null,
            Pair(Coordinates(potentialInventoryPos.x, potentialInventoryPos.y, potentialInventoryPos.z), null)
        )
    }
}