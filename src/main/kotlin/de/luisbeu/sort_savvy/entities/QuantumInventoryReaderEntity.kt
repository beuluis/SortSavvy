package de.luisbeu.sort_savvy.entities

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.network.IdSetterScreenHandler
import de.luisbeu.sort_savvy.persistent.PositionalContext
import de.luisbeu.sort_savvy.persistent.SerializedWorldRegistryKey
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

    private fun getWorldKey(): SerializedWorldRegistryKey {
        val worldRegistryKey = world?.registryKey ?: run {
            val msg = "Could not get world for $quantumInventoryReaderId"
            SortSavvy.logger.error(msg)
            throw Exception(msg)
        }

        return SerializedWorldRegistryKey(worldRegistryKey.registry.toString(), worldRegistryKey.value.toString())
    }

    // Expose a function to set the quantum inventory reader id from the screen
    override fun setId(newId: String, player: ServerPlayerEntity?) {
        // Read the saved data
        val persistentManager = SortSavvy.LifecycleGlobals.getPersistentManager()

        if (newId.isEmpty() && this.quantumInventoryReaderId.isNotEmpty()) {
            val oldQuantumInventoryReaderId = this.quantumInventoryReaderId

            persistentManager.deleteQuantumInventoryReaderData(this.quantumInventoryReaderId)
            this.quantumInventoryReaderId = ""

            // When it was triggered by a player send a message
            player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.deletedDataEntry", oldQuantumInventoryReaderId), true)
        } else if (this.quantumInventoryReaderId.isNotEmpty() && newId != this.quantumInventoryReaderId) {
            val oldQuantumInventoryReaderId = this.quantumInventoryReaderId

            persistentManager.renameQuantumInventoryReaderData(this.quantumInventoryReaderId, newId)
            this.quantumInventoryReaderId = newId

            // When it was triggered by a player send a message
            player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.renamedDataEntry", oldQuantumInventoryReaderId, newId), true)
        } else if (this.quantumInventoryReaderId != newId) {
            if (persistentManager.getQuantumInventoryReaderData().containsKey(newId)) {
                player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.dataEntryAlreadyExists", newId), true)
            } else {
                persistentManager.addQuantumInventoryReaderData(newId, PositionalContext(pos.x, pos.y, pos.z, Direction.UP, getWorldKey()))
                this.quantumInventoryReaderId = newId
                // When it was triggered by a player send a message
                player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.newDataEntry", this.quantumInventoryReaderId), true)
            }
        }

        // Mark entity as dirty to trigger saving
        markDirty()
    }

    // Expose a function to set the quantum inventory reader to scan direction from the screen
    fun setToScanDirection(toScanDirection: Direction, player: ServerPlayerEntity?) {
        // Read the saved data
        val persistentManager = SortSavvy.LifecycleGlobals.getPersistentManager()

        persistentManager.modifyQuantumInventoryReaderData(this.quantumInventoryReaderId) { currentContext ->
            // Create a new PositionalContext with modified values
            currentContext.copy(toScanDirection = toScanDirection)
        }

        // When it was triggered by a player send a message
        player?.sendMessageToClient(Text.translatable("overlay.sort_savvy.toScanDirectoryChanged", toScanDirection.name), true)

        // Mark entity as dirty to trigger saving
        markDirty()
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
            SortSavvy.logger.warn("Could not retrieve quantum inventory reader with id $quantumInventoryReaderId from persisted data")
            return
        }

        buf.writeBlockPos(pos)
        buf.writeString(quantumInventoryReaderId)
        buf.writeString(directionToScan.name)
    }
}