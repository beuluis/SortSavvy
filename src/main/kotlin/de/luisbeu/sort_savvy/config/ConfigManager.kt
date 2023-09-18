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

data class SortSavvyConfigModel(
    var webserverPort: Int = 8080,
    var webserverBearerToken: String = generateBearerToken(32),
)


class ConfigManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File(SortSavvy.LifecycleGlobals.getMinecraftServer().getSavePath(WorldSavePath.ROOT).resolve("serverconfig/SortSavvy.json").toString())
    val config: SortSavvyConfigModel

    init {
        try {
            if (configFile.exists()) {
                config = gson.fromJson(FileReader(configFile), SortSavvyConfigModel::class.java)
            } else {
                config = SortSavvyConfigModel()
                saveConfig()
            }
        } catch (e: Exception) {
            SortSavvy.LOGGER.error("Config could not be loaded: ${e.message}")
            throw Exception() // TODO:
        }
    }

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
        } catch (e: Exception) {
            SortSavvy.LOGGER.error("Config could not be saved: ${e.message}")
        }
    }
}
