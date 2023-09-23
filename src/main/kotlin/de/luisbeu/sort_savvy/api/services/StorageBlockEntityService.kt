package de.luisbeu.sort_savvy.api.services

import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity
import de.luisbeu.sort_savvy.api.dtos.*
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.DoubleInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.registry.Registry

object StorageBlockEntityService {
    // Get the contents of an inventory as a list of ItemDto objects
    private fun getInventoryContents(inventory: Inventory): List<ItemDto> {
        return generateSequence(0) { it + 1 }
            .take(inventory.size())
            .map { inventory.getStack(it) }
            .filterNot { it.isEmpty }
            .map { stack ->
                ItemDto(
                    Registry.ITEM.getId(stack.item).toString(),
                    stack.count,
                    stack.item.group?.name,
                    if (stack.isDamageable) stack.maxDamage - stack.damage else null,
                    if (stack.isDamageable) stack.damage else null,
                    if (stack.hasEnchantments()) stack.enchantments.toList() else null
                )
            }.toList()
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
    ): QuantumInventoryReaderChestScannedDto = QuantumInventoryReaderChestScannedDto(
        CoordinatesDto(chestBlockEntity.pos.y, chestBlockEntity.pos.y, chestBlockEntity.pos.z),
        getInventoryContents(chestBlockEntity),
    )

    // Handler for double chests
    fun scanDoubleChestEntity(
        chestBlockEntity: ChestBlockEntity,
        counterChestBlockEntity: ChestBlockEntity
    ): QuantumInventoryReaderDoubleChestScannedDto = QuantumInventoryReaderDoubleChestScannedDto(
        CoordinatesDto(chestBlockEntity.pos.x, chestBlockEntity.pos.y, chestBlockEntity.pos.z),
        CoordinatesDto(counterChestBlockEntity.pos.x, counterChestBlockEntity.pos.y, counterChestBlockEntity.pos.z),
        getInventoryContents(DoubleInventory(chestBlockEntity, counterChestBlockEntity)),
    )
}