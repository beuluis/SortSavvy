package de.luisbeu.sort_savvy.entities

import net.minecraft.server.network.ServerPlayerEntity

// Common interface to make the screen part generic
interface EntityWithId {
    fun setId(newId: String, player: ServerPlayerEntity?)
}