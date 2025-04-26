package de.sparkarmy

import de.sparkarmy.database.DatabaseConfig
import de.sparkarmy.jda.JdaConfig
import de.sparkarmy.jdui.JduiConfig
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

@Module
@ComponentScan("de.sparkarmy")
class Module {
    @Single fun provideConfig(): Config = readConfig()
    @Single fun provideDatabaseConfig(config: Config): DatabaseConfig = config.database
    @Single fun provideJdaConfig(config: Config): JdaConfig = config.discord
    @Single
    fun provideJduiConfig(config: Config): JduiConfig = config.jduiConfig

}

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
    val preset = de.sparkarmy.Module::class.java.getResourceAsStream("/config.json")
        ?: throw IllegalStateException("Could not find config.toml preset in resources")

    preset.use { Files.copy(it, to) }
}