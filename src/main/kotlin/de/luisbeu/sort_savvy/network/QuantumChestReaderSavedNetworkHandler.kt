package de.luisbeu.sort_savvy.network;

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.entities.QuantumChestReaderEntity
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.ChunkPos

class QuantumChestReaderSavedNetworkHandler : PlayChannelHandler {
    override fun receive(
        server: MinecraftServer,
        player: ServerPlayerEntity,
        handler: ServerPlayNetworkHandler,
        buf: PacketByteBuf,
        responseSender: PacketSender
    ) {
        val pos = buf.readBlockPos()
        val newQuantumChestReaderId = buf.readString()

        // Check and load chunk on pos from the buffer
        val chunkPos = ChunkPos(pos)
        val chunk = player.world.getChunk(chunkPos.x, chunkPos.z)
        val blockEntity = chunk?.getBlockEntity(pos)

        if (blockEntity is QuantumChestReaderEntity) {
            // Get the entity and remap value to null when sting was empty
            blockEntity.setQuantumChestReaderId(newQuantumChestReaderId, player)
        } else {
            if (blockEntity == null) {
                SortSavvy.LOGGER.error("No quantum chest reader entity found at $pos")

                return
            }

            SortSavvy.LOGGER.error("No quantum chest reader entity found at $pos found $blockEntity")
        }
    }

}