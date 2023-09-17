package de.luisbeu.sort_savvy.entities

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.network.IdSetterScreenHandler
import de.luisbeu.sort_savvy.util.PositionWithToScanDirection
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
import net.minecraft.util.math.Direction

// TODO: change ktor logger to own
class QuantumInventoryReaderEntity(pos: BlockPos, state: BlockState) :
    EntityWithId, BlockEntity(SortSavvy.quantumInventoryReaderBlockEntityType, pos, state), ExtendedScreenHandlerFactory {

    private var quantumInventoryReaderId = ""

    // Gets called when entity is dirty to save all to the disk
    override fun writeNbt(nbt: NbtCompound) {
        nbt.putString("quantumInventoryReaderId", quantumInventoryReaderId)

        return super.writeNbt(nbt)
    }

    // Reads all data from the disk on load
    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)

        quantumInventoryReaderId = nbt.getString("quantumInventoryReaderId")
    }

    private fun saveServerState(serverState: ServerState, newDataMap: MutableMap<String, PositionWithToScanDirection>) {
        // Add the new map back to the server state
        serverState.quantumInventoryReaderData = newDataMap.toMap()

        // Mark it dirty to trigger saving
        serverState.markDirty()

        // Mark entity as dirty to trigger saving
        markDirty()
    }

    private fun removeStateData(dataMap: MutableMap<String, PositionWithToScanDirection>) {
        // If an old key is present we know it is a delete action
        if (this.quantumInventoryReaderId.isNotEmpty()) {
            dataMap.remove(this.quantumInventoryReaderId)
            this.quantumInventoryReaderId = ""
        }
    }

    private fun addNewStateData(dataMap: MutableMap<String, PositionWithToScanDirection>, newId: String, toScanDirection: Direction = Direction.UP) {
        dataMap[newId] = PositionWithToScanDirection(pos.x, pos.y, pos.z, toScanDirection)
        this.quantumInventoryReaderId = newId
    }

    private fun updateStateData(dataMap: MutableMap<String, PositionWithToScanDirection>, toScanDirection: Direction = Direction.UP) {
        dataMap[this.quantumInventoryReaderId] = PositionWithToScanDirection(pos.x, pos.y, pos.z, toScanDirection)
    }

    // Expose a function to set the quantum inventory reader id from the screen
    override fun setId(newId: String, player: ServerPlayerEntity?) {
        val serverState = ServerState.getServerState()

        val newDataMap = serverState.quantumInventoryReaderData.toMutableMap()

        if (newId.isEmpty() && this.quantumInventoryReaderId.isNotEmpty()) {
            val oldQuantumInventoryReaderId = this.quantumInventoryReaderId

            removeStateData(newDataMap)

            // When it was triggered by a player send a message
            player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.deletedDataEntry", oldQuantumInventoryReaderId), true)
        } else if (this.quantumInventoryReaderId.isNotEmpty() && newId != this.quantumInventoryReaderId) {
            val oldQuantumInventoryReaderId = this.quantumInventoryReaderId

            removeStateData(newDataMap)
            addNewStateData(newDataMap, newId)

            // When it was triggered by a player send a message
            player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.renamedDataEntry", oldQuantumInventoryReaderId, newId), true)
        } else if (this.quantumInventoryReaderId != newId) {
            if (newDataMap.containsKey(newId)) {
                player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.dataEntryAlreadyExists", newId), true)
            } else {
                addNewStateData(newDataMap, newId)
                // When it was triggered by a player send a message
                player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.newDataEntry", this.quantumInventoryReaderId), true)
            }
        }

        saveServerState(serverState, newDataMap)
    }

    // Expose a function to set the quantum inventory reader to scan direction from the screen
    fun setToScanDirection(toScanDirection: Direction, player: ServerPlayerEntity?) {
        val serverState = ServerState.getServerState()

        val newDataMap = serverState.quantumInventoryReaderData.toMutableMap()

        // We save the new direction to scan
        updateStateData(newDataMap, toScanDirection)

        // When it was triggered by a player send a message
        player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.toScanDirectoryChanged", toScanDirection.name), true)

        saveServerState(serverState, newDataMap)
    }

    // When the block is used this gets called by openHandledScreen to spawn a new instance of the handler
    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        return IdSetterScreenHandler(syncId, inv)
    }

    // Construct the display name
    override fun getDisplayName(): Text {
        // When no id is set we display it as unnamed
        // When there is an id we display this
        return if (quantumInventoryReaderId == "") Text.translatable("gui.sort_savvy.unnamed").append(" ")
            .append(Text.translatable(cachedState.block.translationKey)) else Text.literal(quantumInventoryReaderId)
    }

    // Write the entity data we have to the buffer to read it at the client
    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        if (quantumInventoryReaderId == "") {
            buf.writeBlockPos(pos)
            buf.writeString(quantumInventoryReaderId)
            buf.writeString("")
            return
        }

        val serverState = ServerState.getServerState()

        val (_, _, _, directionToScan) = serverState.quantumInventoryReaderData[quantumInventoryReaderId] ?: run {
            SortSavvy.LOGGER.error("Could not retrieve server state data writing to buffer $pos")
            return
        }

        buf.writeBlockPos(pos)
        buf.writeString(quantumInventoryReaderId)
        buf.writeString(directionToScan.name)
    }
}