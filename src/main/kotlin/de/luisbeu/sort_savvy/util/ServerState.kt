package de.luisbeu.sort_savvy.util

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.Direction
import net.minecraft.world.PersistentState
import net.minecraft.world.World

// TODO: add command to rescan blocks
data class PositionWithToScanDirection(val x: Int, val y: Int, val z: Int, val toScanDirection: Direction)

class ServerState() : PersistentState() {
    // Add out data map as attribute
    var quantumInventoryReaderData: Map<String, PositionWithToScanDirection> = mapOf()

    // Convert the map to nbt data and return nbt. This functions get called before nbt gets write to the disk to inject the data we want to save
    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val quantumInventoryReaderDataNbt = NbtCompound()

        // Iterate over all entries and add them in the nbt
        quantumInventoryReaderData.forEach { (key, positionWithDirection) ->
            val posNbt = NbtCompound()

            posNbt.putInt("x", positionWithDirection.x)
            posNbt.putInt("y", positionWithDirection.y)
            posNbt.putInt("z", positionWithDirection.z)

            posNbt.putString("toScanDirection", positionWithDirection.toScanDirection.name) // Store direction as a string

            quantumInventoryReaderDataNbt.put(key, posNbt)
        }

        // Write them to the final nbt
        nbt.put("quantumInventoryReaderData", quantumInventoryReaderDataNbt)

        return nbt
    }

    companion object {
        // This gets called when the data gets loaded. We translate the tags to out map attribute for easy code usage
        private fun createFromNbt(nbt: NbtCompound): ServerState {
            val serverState = ServerState()

            val quantumInventoryReaderDataNbt = nbt.getCompound("quantumInventoryReaderData")

            val quantumInventoryReaderData = mutableMapOf<String, PositionWithToScanDirection>()

            quantumInventoryReaderDataNbt.keys.forEach { key ->
                val posNbt = quantumInventoryReaderDataNbt.getCompound(key)

                val x = posNbt.getInt("x")
                val y = posNbt.getInt("y")
                val z = posNbt.getInt("z")

                val toScanDirectionName = posNbt.getString("toScanDirection")
                val toScanDirection = Direction.valueOf(toScanDirectionName) // Parse direction from string

                quantumInventoryReaderData[key] = PositionWithToScanDirection(x, y, z, toScanDirection)
            }

            serverState.quantumInventoryReaderData = quantumInventoryReaderData.toMap()

            return serverState
        }

        fun getServerState(server: MinecraftServer): ServerState {
            // We get the persistent manager from the overworld
            // TODO: maybe add saving for other dimensions?
            val persistentStateManager = server.getWorld(World.OVERWORLD)?.persistentStateManager
                ?: throw IllegalStateException("Overworld not loaded")


            // Get the mod based manager or create a new one
            val serverState = persistentStateManager.getOrCreate(
                ServerState::createFromNbt, { ServerState() }, SortSavvyConstants.MOD_ID
            )

            // Mark the manager as dirty to save on disk
            serverState.markDirty()

            return serverState
        }
    }
}