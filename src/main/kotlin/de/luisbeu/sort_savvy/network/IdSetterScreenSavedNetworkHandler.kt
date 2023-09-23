package de.luisbeu.sort_savvy.network

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.entities.EntityWithId
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.ChunkPos

class IdSetterScreenSavedNetworkHandler : PlayChannelHandler {
    // Receive the packet and set the new id for the entity
    override fun receive(
        server: MinecraftServer,
        player: ServerPlayerEntity,
        handler: ServerPlayNetworkHandler,
        buf: PacketByteBuf,
        responseSender: PacketSender
    ) {
        // Read the block position and new id from the buffer
        val pos = buf.readBlockPos()
        val newId = buf.readString()

        // Check if there is an entity with the given id at the block position
        val chunkPos = ChunkPos(pos)
        val chunk = player.world.getChunk(chunkPos.x, chunkPos.z)
        val blockEntity = (chunk?.getBlockEntity(pos) as? EntityWithId) ?: run {
            SortSavvy.logger.warn("No entity with id found at $pos")
            return
        }

        // Set the new id for the entity
        blockEntity.setId(newId, player)
    }
}