package de.sparkarmy.config

import de.sparkarmy.Main
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

@Serializable
data class Config(
    val discord: Discord,
    val database: Database
)

@Serializable
data class Discord(
    val clientId: String,
    val token: String,
    val secret: String,
    val redirect: String
)

@Serializable
data class Database(
    val host: String,
    val database: String,
    val schema: String,
    val port: Int,
    val username: String,
    val password: String,
)


fun readConfig(copyPresetIfMissing: Boolean = false): Config {
    val mainConfigPath = Path("./configs/main-config.json")
    val devConfigPath = Path("./configs/dev-config.json")

    var configContent = ""

    if (!mainConfigPath.exists() and !devConfigPath.exists()) {
        if (copyPresetIfMissing) {
            copyConfigPreset(mainConfigPath)
            throw IllegalStateException("config created. exiting")
        } else {
            throw IllegalStateException("Could not locate config file")
        }
    } else if (mainConfigPath.exists()) {
        configContent = mainConfigPath.readText()
    } else if (devConfigPath.exists()) {
        configContent = devConfigPath.readText()
    }

    if (configContent.isBlank()) {
        throw IllegalStateException("config content is empty")
    }

    return Json.decodeFromString(configContent)

}

private fun copyConfigPreset(to: Path) {
    val preset = Main::class.java.getResourceAsStream("/config.json")
        ?: throw IllegalStateException("Could not find config.toml preset in resources")

    preset.use { Files.copy(it, to) }
}