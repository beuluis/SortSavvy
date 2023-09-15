package de.luisbeu.sort_savvy.network;

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
    override fun receive(
        server: MinecraftServer,
        player: ServerPlayerEntity,
        handler: ServerPlayNetworkHandler,
        buf: PacketByteBuf,
        responseSender: PacketSender
    ) {
        val pos = buf.readBlockPos()
        val newId = buf.readString()

        // Check and load chunk on pos from the buffer
        val chunkPos = ChunkPos(pos)
        val chunk = player.world.getChunk(chunkPos.x, chunkPos.z)
        val blockEntity = (chunk?.getBlockEntity(pos) as? EntityWithId)?: run {
            SortSavvy.LOGGER.error("No entity with id found at $pos")
            return
        }

        blockEntity.setId(newId, player)
    }

}