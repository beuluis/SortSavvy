package de.luisbeu.sort_savvy.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.luisbeu.sort_savvy.SortSavvy
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path
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


class SortSavvyConfig {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    // TODO: test if i need to create this folder also?
    private val configFile = File(SortSavvy.LifecycleGlobals.getMinecraftServer().getSavePath(WorldSavePath.ROOT).resolve("serverconfig/SortSavvy.json").toString())
    private var config: SortSavvyConfigModel

    init {
        if (configFile.exists()) {
            config = gson.fromJson(FileReader(configFile), SortSavvyConfigModel::class.java)
        } else {
            config = SortSavvyConfigModel()
            saveConfig()
        }
    }

    fun getConfig(): SortSavvyConfigModel {
        return config
    }

    private fun saveConfig() {
        try {
            FileWriter(configFile).use { writer ->
                gson.toJson(config, writer)
            }
        } catch (e: Exception) {
            SortSavvy.LOGGER.error("Config could not be saved: ${e.message}")
        }
    }
}
