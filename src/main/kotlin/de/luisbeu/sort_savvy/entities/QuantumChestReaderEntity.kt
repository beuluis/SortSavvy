package de.luisbeu.sort_savvy.entities

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.network.QuantumChestReaderScreenHandler
import de.luisbeu.sort_savvy.util.ServerState
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class QuantumChestReaderEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(SortSavvy.quantumChestReaderBlockEntityType, pos, state), ExtendedScreenHandlerFactory {

    private var quantumChestReaderId = ""

    // Gets called when entity is dirty to save all to the disk
    override fun writeNbt(nbt: NbtCompound) {
        nbt.putString("quantumChestReaderId", quantumChestReaderId)

        return super.writeNbt(nbt)
    }

    // Reads all data from the disk on load
    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)

        quantumChestReaderId = nbt.getString("quantumChestReaderId")
    }

    // Expose a function to set the quantum chest reader id from the screen
    fun setQuantumChestReaderId(newQuantumChestReaderId: String, player: ServerPlayerEntity?) {
        // Don`t know when this is the case but we null check it to log an error and don`t crash something
        val server = world?.server ?: run {
            SortSavvy.LOGGER.error("World is null while setting quantum chest reader id for $pos")
            return
        }

        // Retrieve the server state containing the quantum chest reader data we are interested
        val serverState = ServerState.getServerState(server)

        // Get the data we are interested
        val newData = serverState.quantumChestReaderData.toMutableMap()

        if (newQuantumChestReaderId.isEmpty()) {
            if (this.quantumChestReaderId.isNotEmpty()) {
                val oldQuantumChestReaderId = this.quantumChestReaderId

                // When we get an empty string we remove the previous key but only when there was a previous one read
                newData.remove(this.quantumChestReaderId)

                // Set the new id to the class attribute
                this.quantumChestReaderId = newQuantumChestReaderId

                // When it was triggered by a player send a message
                player?.sendMessageToClient(Text.of("${oldQuantumChestReaderId} was deleted."), true)
            }
        } else if (this.quantumChestReaderId.isNotEmpty() && newQuantumChestReaderId != this.quantumChestReaderId) {
            // When there was a previous one we first remove the old one and then save the new key
            newData.remove(this.quantumChestReaderId)
            newData[newQuantumChestReaderId] = Triple(pos.x, pos.y, pos.z)

            // Set the new id to the class attribute
            this.quantumChestReaderId = newQuantumChestReaderId

            // When it was triggered by a player send a message
            player?.sendMessageToClient(Text.of("Was renamed to ${newQuantumChestReaderId}."), true)
        } else {
            // When the key is already us do nothing
            if (this.quantumChestReaderId == newQuantumChestReaderId) {
                return
            }

            // When new key already exists we skip it and sent and error
            if (newData.containsKey(newQuantumChestReaderId)) {
                player?.sendMessageToClient(
                    Text.of("$newQuantumChestReaderId exists already. Was not added."),
                    true
                )
                return
            }

            // When we have a new key we add it without doing something else
            newData[newQuantumChestReaderId] = Triple(pos.x, pos.y, pos.z)

            // Set the new id to the class attribute
            this.quantumChestReaderId = newQuantumChestReaderId

            // When it was triggered by a player send a message
            player?.sendMessageToClient(Text.of("Was added as ${this.quantumChestReaderId}."), true)
        }

        // Add the new map back to the server state
        serverState.quantumChestReaderData = newData.toMap()

        // Mark it dirty to trigger saving
        serverState.markDirty()

        // Mark entity as dirty to trigger saving
        markDirty()
    }

    // When the block is used this gets called by openHandledScreen to spawn a new instance of the handler
    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return QuantumChestReaderScreenHandler(syncId, inv)
    }

    // Construct the display name
    override fun getDisplayName(): Text {
        // When no id is set we display it as unnamed
        // When there is an id we display this
        return if (quantumChestReaderId == "") Text.translatable("gui.sort_savvy.unnamed").append(" ")
            .append(Text.translatable(cachedState.block.translationKey)) else Text.literal(quantumChestReaderId)
    }

    // Write the entity data we have to the buffer to read it at the client
    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeString(quantumChestReaderId)
    }
}