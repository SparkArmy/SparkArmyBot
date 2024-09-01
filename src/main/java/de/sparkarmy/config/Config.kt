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
    val database: DatabaseConfig,
    val twitch: Twitch,
    val youtube: YouTube
)

@Serializable
data class Discord(
    val discordClientId: String,
    val discordToken: String,
    val log: String
)

@Serializable
data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String
)

@Serializable
data class Twitch(
    val twitchClientId: String,
    val twitchClientSecret: String
)

@Serializable
data class YouTube(
    val youtubeApiKey: String,
    val springCallbackUrl: String
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
        ?: throw IllegalStateException("Could not find config.json preset in resources")

    preset.use { Files.copy(it, to) }
}