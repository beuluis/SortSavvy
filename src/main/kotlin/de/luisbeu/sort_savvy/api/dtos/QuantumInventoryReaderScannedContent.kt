package de.luisbeu.sort_savvy.api.dtos

import net.minecraft.nbt.NbtElement

data class QuantumInventoryReaderScannedContent(
    val id: String,
    val amount: Int,
    val category: String?,
    val durability: Int?,
    val damage: Int?,
    val enchantments: List<NbtElement>?
)