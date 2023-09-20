package de.luisbeu.sort_savvy.api.dtos

import net.minecraft.util.math.Direction
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

data class QuantumInventoryReaderContext (
    val id: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val toScanDirection: Direction,
    val worldRegistryKey: RegistryKey<World>
)