package de.luisbeu.sort_savvy.entities

import net.minecraft.server.network.ServerPlayerEntity

interface EntityWithId {
    fun setId(newId: String, player: ServerPlayerEntity?)
}