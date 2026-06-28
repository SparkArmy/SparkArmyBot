package de.sparkarmy

import de.sparkarmy.config.Config
import de.sparkarmy.config.Environment
import io.github.freya022.botcommands.api.core.BotCommands
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.time.Duration
import kotlin.io.path.absolutePathString
import kotlin.system.exitProcess
import ch.qos.logback.classic.ClassicConstants as LogbackConstants

private val logger by lazy { KotlinLogging.logger {} }

private const val mainPackageName = "de.sparkarmy"
const val botName = "SparkArmyBot"

object Main {
    @JvmStatic
    fun main(args: Array<out String>) {
        try {
            System.setProperty(
                LogbackConstants.CONFIG_FILE_PROPERTY,
                Environment.logbackConfigPath.absolutePathString()
            )

            logger.info { "Loading logback configuration at ${Environment.logbackConfigPath.absolutePathString()}" }

            val config = Config.instance

            BotCommands.create {
                disableExceptionsInDMs = Environment.isDev

                addSearchPath(mainPackageName)

                localization {
                    responseBundles += "EventMessages"
                }

                applicationCommands {
//                    @OptIn(DevConfig::class)
//                    disableAutocompleteCache = Environment.isDev

                    // Default file-based application commands cache
                    fileCache {
                        // Check command updates based on Discord's commands.
                        // This is only useful during development,
                        // as you can develop on multiple machines (but not simultaneously!).
                        // Using this in production is only going to waste API requests.
//                        @OptIn(DevConfig::class)
//                        checkOnline = Environment.isDev
                    }
                    addLocalizations("Commands", DiscordLocale.GERMAN, DiscordLocale.ENGLISH_US)

                    // Guilds in which `@Test` commands will be inserted
                    testGuildIds += 890674837461278730

                    // Add french (and root, for default descriptions) localization for application commands
                    //addLocalizations("Commands", DiscordLocale.FRENCH)
                }

                components {
                    // Enables usage of components
                    // This can be removed if you don't have a database,
                    // but you'll need to use raw JDA components
                    enable = true
                }

                eventManager {
                    setDefaultTimeout(Duration.ofSeconds(10))
                }

            }
        } catch (e: Exception) {
            logger.error(e) { "Unable to start the bot" }
            exitProcess(1)
        }
    }
}

