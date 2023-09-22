package de.luisbeu.sort_savvy.api.dtos

// Scan result items. Inspired by the minecrafts item class
data class ItemDto (
    val id: String,
    val amount: Int,
    val category: String?,
    val durability: Int?,
    val damage: Int?,
    val enchantments: List<Any>?
)