package de.luisbeu.sort_savvy.api.services

import de.luisbeu.sort_savvy.api.dtos.Coordinates
import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderChestScanned
import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderDoubleChestScanned
import de.luisbeu.sort_savvy.api.dtos.ScannedContent
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.DoubleInventory
import net.minecraft.inventory.Inventory
import net.minecraft.util.registry.Registry

// TODO: add suffix to names
object StorageBlockEntityService {
    // TODO: support vaults
    private fun getInventoryContents(inventory: Inventory): List<ScannedContent> {
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
                ScannedContent(itemId, count, category, durability, damage, enchantments)
            }
            .toList()
    }

    fun scanChestEntity(
        chestBlockEntity: ChestBlockEntity
    ): QuantumInventoryReaderChestScanned {
        return QuantumInventoryReaderChestScanned(
            Coordinates(chestBlockEntity.pos.y, chestBlockEntity.pos.y, chestBlockEntity.pos.z),
            getInventoryContents(chestBlockEntity),
        )
    }

    fun scanDoubleChestEntity(
        chestBlockEntity: ChestBlockEntity,
        counterChestBlockEntity: ChestBlockEntity
    ): QuantumInventoryReaderDoubleChestScanned {
        return QuantumInventoryReaderDoubleChestScanned(
            Coordinates(chestBlockEntity.pos.x, chestBlockEntity.pos.y, chestBlockEntity.pos.z),
            Coordinates(counterChestBlockEntity.pos.x, counterChestBlockEntity.pos.y, counterChestBlockEntity.pos.z),
            getInventoryContents(DoubleInventory(chestBlockEntity, counterChestBlockEntity)),
        )
    }
}