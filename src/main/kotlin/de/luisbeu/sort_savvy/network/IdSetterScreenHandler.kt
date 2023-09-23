package de.luisbeu.sort_savvy.network

import de.luisbeu.sort_savvy.SortSavvy
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.math.BlockPos

class IdSetterScreenHandler(
    syncId: Int, playerInventory: PlayerInventory, buf: PacketByteBuf = PacketByteBufs.empty()
) : ScreenHandler(SortSavvy.idSetterScreenHandlerType, syncId) {
    private var pos = BlockPos.ORIGIN

    var id: String? = null
        // Set setter private since externals should use setId
        private set

    var directionToScan: String? = null
        // Set setter private
        private set

    // Initialize the screen handler with the given buffer
    init {
        // We have different scenarios when the constructor is called. On register, we do not have a buffer when the handler gets returned by the entity we have one
        if (buf.readableBytes() > 0) {
            pos = buf.readBlockPos()
            id = buf.readString()
            directionToScan = buf.readString()
        }
    }

    // Set the id for the screen handler and send a packet to the client
    fun setId(id: String) {
        this.id = id

        ClientPlayNetworking.send(
            SortSavvy.Constants.idSetterScreenSavedNetworkHandlerId,
            PacketByteBufs.create().writeBlockPos(pos).writeString(id)
        )
    }

    // We don`t have slots. So we send null
    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack? = null

    // The screen handler can always be used
    override fun canUse(player: PlayerEntity?): Boolean = true
}