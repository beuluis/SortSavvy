package de.luisbeu.sort_savvy.persistent

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.luisbeu.sort_savvy.SortSavvy
import net.minecraft.util.WorldSavePath
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.RegistryKey
import net.minecraft.world.World
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class PositionalContext(val x: Int, val y: Int, val z: Int, val toScanDirection: Direction, val worldRegistryKey: RegistryKey<World>)

data class DataStateModel(
    var quantumInventoryReaderData: MutableMap<String, PositionalContext> = mutableMapOf(),
)

class PersistentManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dataStateFile = File(SortSavvy.LifecycleGlobals.getMinecraftServer().getSavePath(WorldSavePath.ROOT).resolve("data/SortSavvy.json").toString())
    private var dataState: DataStateModel

    init {
        if (dataStateFile.exists()) {
            try {
                dataState = gson.fromJson(FileReader(dataStateFile), DataStateModel::class.java)
            } catch (e: Exception) {
                SortSavvy.LOGGER.error("Data could not be loaded: ${e.message}")
                throw Exception() // TODO:
            }
        } else {
            dataState = DataStateModel()
            saveData()
        }
    }

    fun getQuantumInventoryReaderData(): Map<String, PositionalContext> {
        return dataState.quantumInventoryReaderData
    }

    fun deleteQuantumInventoryReaderData(key: String) {
        dataState.quantumInventoryReaderData.remove(key)
        saveData()
    }

    fun addQuantumInventoryReaderData(key: String, context: PositionalContext) {
        dataState.quantumInventoryReaderData[key] = context
        saveData()
    }

    fun modifyQuantumInventoryReaderData(key: String, modifier: (PositionalContext) -> PositionalContext) {
        if (dataState.quantumInventoryReaderData.containsKey(key)) {
            val currentContext = dataState.quantumInventoryReaderData[key] ?: return
            val updatedContext = modifier(currentContext)
            dataState.quantumInventoryReaderData[key] = updatedContext
            saveData()
            return
        }
        // TODO: throw?
    }

    fun renameQuantumInventoryReaderData(oldKey: String, newKey: String) {
        if (dataState.quantumInventoryReaderData.containsKey(oldKey)) {
            val context = dataState.quantumInventoryReaderData.remove(oldKey)
            context?.let {
                dataState.quantumInventoryReaderData[newKey] = it
                saveData()
            }
        }
    }

    fun saveData() {
        try {
            // Get the parent directory of the data file
            val parentDirectory = dataStateFile.parentFile

            // Create the parent directory if it doesn't exist
            if (!parentDirectory.exists()) {
                parentDirectory.mkdirs()
            }

            val jsonObject = gson.toJsonTree(dataState).asJsonObject

            // Add a comment as a JSON property
            jsonObject.addProperty("comment", "Please do not edit this file manually! Especially not when the game is running.")

            FileWriter(dataStateFile).use { writer ->
                gson.toJson(jsonObject, writer)
            }
        } catch (e: Exception) {
            SortSavvy.LOGGER.error("Data could not be saved: ${e.message}")
        }
    }
}