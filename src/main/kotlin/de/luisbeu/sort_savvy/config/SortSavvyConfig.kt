import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
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


class SortSavvyConfig() {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File(FabricLoader.getInstance().configDir.resolve("SortSavvy.json").toString())
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

    fun saveConfig() {
            FileWriter(configFile).use { writer ->
                gson.toJson(config, writer)
            }
    }
}
