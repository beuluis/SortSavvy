package de.luisbeu.sort_savvy.network

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.util.SortSavvyConstants
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.math.BlockPos

class QuantumChestReaderScreenHandler(
    syncId: Int, playerInventory: PlayerInventory, buf: PacketByteBuf = PacketByteBufs.empty()
) : ScreenHandler(SortSavvy.quantumChestReaderScreenHandlerType, syncId) {

    // Set all the defaults
    private var pos = BlockPos.ORIGIN
    private var quantumChestReaderId: String? = null

    init {
        // We have different scenarios when the constructor is called. On register, we do not have a buffer when the handler gets returned by the entity we have one
        if (buf.readableBytes() > 0) {
            pos = buf.readBlockPos()
            quantumChestReaderId = buf.readString()
        }
    }

    // Expose quantum chest reader id to not be able to modify it directly
    fun getQuantumChestReaderId(): String? {
        return quantumChestReaderId
    }

    // Setter for quantum chest reader id to add some additional logic
    fun setQuantumChestReaderId(quantumChestReaderId: String) {
        // Update class attribute
        this.quantumChestReaderId = quantumChestReaderId

        // Send package to client
        ClientPlayNetworking.send(
            SortSavvyConstants.quantumChestReaderSavedNetworkHandlerId,
            PacketByteBufs.create().writeBlockPos(pos).writeString(quantumChestReaderId)
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
