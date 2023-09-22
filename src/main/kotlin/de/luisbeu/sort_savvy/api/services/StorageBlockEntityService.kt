package de.luisbeu.sort_savvy.api.services

import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity
import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.api.dtos.*
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.DoubleInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.registry.Registry

object StorageBlockEntityService {
    private fun getInventoryContents(inventory: Inventory): List<ItemDto> {
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
                ItemDto(itemId, count, category, durability, damage, enchantments)
            }
            .toList()
    }

    // Handler for item vaults
    fun scanItemVaultEntity(
        itemVaultBlockEntity: ItemVaultBlockEntity
    ): QuantumInventoryReaderItemVaultScannedDto {
        throw NotImplementedError()
    }

    // Handler for single chests
    fun scanChestEntity(
        chestBlockEntity: ChestBlockEntity
    ): QuantumInventoryReaderChestScannedDto {
        return QuantumInventoryReaderChestScannedDto(
            CoordinatesDto(chestBlockEntity.pos.y, chestBlockEntity.pos.y, chestBlockEntity.pos.z),
            getInventoryContents(chestBlockEntity),
        )
    }

    // Handler for double chests
    fun scanDoubleChestEntity(
        chestBlockEntity: ChestBlockEntity,
        counterChestBlockEntity: ChestBlockEntity
    ): QuantumInventoryReaderDoubleChestScannedDto {
        return QuantumInventoryReaderDoubleChestScannedDto(
            CoordinatesDto(chestBlockEntity.pos.x, chestBlockEntity.pos.y, chestBlockEntity.pos.z),
            CoordinatesDto(counterChestBlockEntity.pos.x, counterChestBlockEntity.pos.y, counterChestBlockEntity.pos.z),
            getInventoryContents(DoubleInventory(chestBlockEntity, counterChestBlockEntity)),
        )
    }
}