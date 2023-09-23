package de.luisbeu.sort_savvy.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.luisbeu.sort_savvy.SortSavvy
import net.minecraft.util.WorldSavePath
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.security.SecureRandom
import java.util.Base64

fun generateBearerToken(length: Int): String {
    val random = SecureRandom()
    val bytes = ByteArray(length)
    random.nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

// Model data class to specify structure and make serialization easier
data class SortSavvyConfigModel(
    var webserverPort: Int = 8080,
    var webserverBearerToken: String = generateBearerToken(32),
)

object ConfigManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val configFile = File(SortSavvy.LifecycleGlobals.getMinecraftServer().getSavePath(WorldSavePath.ROOT).resolve("serverconfig/SortSavvy.json").toString())

    // Initialize with default. Gets maybe overridden if a file already exists
    private var config: SortSavvyConfigModel = SortSavvyConfigModel()

    // Load the config file if it exists, otherwise save the default config
    fun load() {
        try {
            if (configFile.exists()) {
                config = gson.fromJson(FileReader(configFile), SortSavvyConfigModel::class.java)
            } else {
                saveConfig()
            }
        } catch (error: Exception) {
            SortSavvy.logger.error("Config could not be loaded: ${error.message}")
            throw error
        }
    }

    fun getConfig(): SortSavvyConfigModel = config

    private fun saveConfig() {
        try {
            // Get the parent directory of the config file
            val parentDirectory = configFile.parentFile

            // Create the parent directory if it doesn't exist
            if (!parentDirectory.exists()) {
                parentDirectory.mkdirs()
            }

            FileWriter(configFile).use { writer ->
                gson.toJson(config, writer)
            }
        } catch (error: Exception) {
            SortSavvy.logger.error("Config could not be saved: ${error.message}")
            throw error
        }
    }
}