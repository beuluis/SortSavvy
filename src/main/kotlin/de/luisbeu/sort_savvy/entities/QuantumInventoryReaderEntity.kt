package de.luisbeu.sort_savvy.entities

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.network.IdSetterScreenHandler
import de.luisbeu.sort_savvy.persistent.PositionalContext
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
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World

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

    private fun saveToPersistedData(newDataMap: MutableMap<String, PositionalContext>) {
        // Add the new map back to the persisted data
        SortSavvy.LifecycleGlobals.getPersistentManager().setQuantumInventoryReaderData(newDataMap.toMap())

        // Mark entity as dirty to trigger saving
        markDirty()
    }

    private fun getWorldKey(): RegistryKey<World> = world?.registryKey ?: run {
        // TODO: Rework throws
        SortSavvy.LOGGER.error("Could not get world for $quantumInventoryReaderId")
        throw Exception()
    }

    private fun removeFromDataMap(dataMap: MutableMap<String, PositionalContext>) {
        // If an old key is present we know it is a delete action
        if (this.quantumInventoryReaderId.isNotEmpty()) {
            dataMap.remove(this.quantumInventoryReaderId)
            this.quantumInventoryReaderId = ""
        }
    }

    private fun addNewToDataMap(dataMap: MutableMap<String, PositionalContext>, newId: String, toScanDirection: Direction = Direction.UP) {
        dataMap[newId] = PositionalContext(pos.x, pos.y, pos.z, toScanDirection, getWorldKey())
        this.quantumInventoryReaderId = newId
    }

    // TODO: extract those as generics to the persistent manager?
    private fun updateDataMapToScanDirection(dataMap: MutableMap<String, PositionalContext>, toScanDirection: Direction = Direction.UP) {
        val oldValue = dataMap[this.quantumInventoryReaderId] ?: run {
            SortSavvy.LOGGER.error("Could not retrieve quantum inventory reader with id $quantumInventoryReaderId to update")
            throw Exception() // TODO: rework throws
        }

        dataMap[this.quantumInventoryReaderId] = oldValue.copy(
            toScanDirection = toScanDirection
        )
    }

    // Expose a function to set the quantum inventory reader id from the screen
    override fun setId(newId: String, player: ServerPlayerEntity?) {
        // Read the saved data
        val persistentManager = SortSavvy.LifecycleGlobals.getPersistentManager()

        val newDataMap = persistentManager.getQuantumInventoryReaderData().toMutableMap()

        if (newId.isEmpty() && this.quantumInventoryReaderId.isNotEmpty()) {
            val oldQuantumInventoryReaderId = this.quantumInventoryReaderId

            removeFromDataMap(newDataMap)

            // When it was triggered by a player send a message
            player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.deletedDataEntry", oldQuantumInventoryReaderId), true)
        } else if (this.quantumInventoryReaderId.isNotEmpty() && newId != this.quantumInventoryReaderId) {
            val oldQuantumInventoryReaderId = this.quantumInventoryReaderId

            removeFromDataMap(newDataMap)
            addNewToDataMap(newDataMap, newId)

            // When it was triggered by a player send a message
            player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.renamedDataEntry", oldQuantumInventoryReaderId, newId), true)
        } else if (this.quantumInventoryReaderId != newId) {
            if (newDataMap.containsKey(newId)) {
                player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.dataEntryAlreadyExists", newId), true)
            } else {
                addNewToDataMap(newDataMap, newId)
                // When it was triggered by a player send a message
                player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.newDataEntry", this.quantumInventoryReaderId), true)
            }
        }

        saveToPersistedData(newDataMap)
    }

    // Expose a function to set the quantum inventory reader to scan direction from the screen
    fun setToScanDirection(toScanDirection: Direction, player: ServerPlayerEntity?) {
        // Read the saved data
        val persistentManager = SortSavvy.LifecycleGlobals.getPersistentManager()

        val newDataMap = persistentManager.getQuantumInventoryReaderData().toMutableMap()

        // We save the new direction to scan
        updateDataMapToScanDirection(newDataMap, toScanDirection)

        // When it was triggered by a player send a message
        player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.toScanDirectoryChanged", toScanDirection.name), true)

        saveToPersistedData(newDataMap)
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

        val (_, _, _, directionToScan) = SortSavvy.LifecycleGlobals.getPersistentManager().getQuantumInventoryReaderData()[quantumInventoryReaderId] ?: run {
            SortSavvy.LOGGER.error("Could not retrieve quantum inventory reader with id $quantumInventoryReaderId from persisted data")
            return
        }

        buf.writeBlockPos(pos)
        buf.writeString(quantumInventoryReaderId)
        buf.writeString(directionToScan.name)
    }
}