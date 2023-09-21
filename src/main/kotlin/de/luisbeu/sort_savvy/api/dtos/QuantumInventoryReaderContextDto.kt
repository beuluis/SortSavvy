package de.luisbeu.sort_savvy.api.dtos

import net.minecraft.util.math.Direction

// Context for the quantum inventory reader
data class QuantumInventoryReaderContextDto (
    val id: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val toScanDirection: Direction,
    val dimension: String
)