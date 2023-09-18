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

    // Set all the defaults
    private var pos = BlockPos.ORIGIN
    private var id: String? = null
    private var directionToScan: String? = null

    init {
        // We have different scenarios when the constructor is called. On register, we do not have a buffer when the handler gets returned by the entity we have one
        if (buf.readableBytes() > 0) {
            pos = buf.readBlockPos()
            id = buf.readString()
            directionToScan = buf.readString()
        }
    }

    // Expose id to not be able to modify it directly
    fun getId(): String? {
        return id
    }

    // Expose directionToScan to not be able to modify it directly
    fun getDirectionToScan(): String? {
        return directionToScan
    }

    // Setter for id to add some additional logic
    fun setId(id: String) {
        // Update class attribute
        this.id = id

        // Send package to client
        ClientPlayNetworking.send(
            SortSavvy.Constants.idSetterScreenSavedNetworkHandlerId,
            PacketByteBufs.create().writeBlockPos(pos).writeString(id)
        )
    }


    // We don`t have slots. Don`t know why the interface requires that
    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack? {
        return null
    }

    // We are always able to use it
    override fun canUse(player: PlayerEntity?): Boolean {
        return true
    }
}
