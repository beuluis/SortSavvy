package de.luisbeu.sort_savvy.util

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import net.minecraft.world.World

// TODO: add command to rescan blocks
class ServerState : PersistentState() {
    // Add out data map as attribute
    var quantumChestReaderData: Map<String, Triple<Int, Int, Int>> = mapOf()

    // Convert the map to nbt data and return nbt. This functions get called before nbt gets write to the disk to inject the data we want to save
    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        val quantumChestReaderDataNbt = NbtCompound()

        // Iterate over all entries and add them in the nbt
        quantumChestReaderData.forEach { (key, triple) ->
            val (x, y, z) = triple
            val posNbt = NbtCompound()
            posNbt.putInt("x", x)
            posNbt.putInt("y", y)
            posNbt.putInt("z", z)
            quantumChestReaderDataNbt.put(key, posNbt)
        }

        // Write them to the final nbt
        nbt.put("data", quantumChestReaderDataNbt)

        return nbt
    }

    companion object {
        // This gets called when the data gets loaded. We translate the tags to out map attribute for easy code usage
        private fun createFromNbt(nbt: NbtCompound): ServerState {
            val serverState = ServerState()
            val quantumChestReaderDataNbt = nbt.getCompound("data")
            val quantumChestReaderData = mutableMapOf<String, Triple<Int, Int, Int>>()
            quantumChestReaderDataNbt.keys.forEach { key ->
                val posNbt = quantumChestReaderDataNbt.getCompound(key)
                val x = posNbt.getInt("x")
                val y = posNbt.getInt("y")
                val z = posNbt.getInt("z")
                quantumChestReaderData[key] = Triple(x, y, z)
            }
            serverState.quantumChestReaderData = quantumChestReaderData.toMap()
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