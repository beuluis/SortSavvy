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

fun getServerConfig(server: MinecraftServer): SortSavvyConfigModel {
    return SortSavvyConfig(server.getSavePath(WorldSavePath.ROOT)).getConfig()
}

data class SortSavvyConfigModel(
    var webserverPort: Int = 8080,
    var webserverBearerToken: String = generateBearerToken(32),
)


class SortSavvyConfig(worldSaveDirectory: Path) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File(worldSaveDirectory.resolve("serverconfig/SortSavvy.json").toString())
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
