package de.luisbeu.sort_savvy.api.dtos

data class ScannedContent (
    val id: String,
    val amount: Int,
    val category: String?,
    val durability: Int?,
    val damage: Int?,
    val enchantments: List<Any>?
)