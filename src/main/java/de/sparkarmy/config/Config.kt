package de.sparkarmy.config

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.DefaultObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

data class Discord(
    val clientId: String,
    val token: String,
    val secret: String,
    val redirect: String,
)

data class Database(
    val host: String,
    val database: String,
    val schema: String,
    val port: Int,
    val username: String,
    val password: String,
)

data class Twitch(
    val clientId: String,
    val clientSecret: String,
)

data class Youtube(
    val redirect: String,
    val token: String,
)

data class Config(
    @get:BService
    val discord: Discord,
    @get:BService
    val database: Database,
    val twitch: Twitch,
    val youTube: Youtube,
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        private val configFilePath : Path = Environment.configFolder.resolve("config.json")

        @get:BService
        val instance: Config by lazy {
            logger.info { "Loading configuration at ${configFilePath.absolutePathString()}" }

            return@lazy DefaultObjectMapper.mapper.readValue(configFilePath.readText())
        }
    }
}