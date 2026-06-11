package de.sparkarmy

import de.sparkarmy.config.Config
import io.github.freya022.botcommands.api.core.JDAService
import io.github.freya022.botcommands.api.core.defaultSharded
import io.github.freya022.botcommands.api.core.events.BReadyEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

/**
 * Service to start JDA at the appropriate time
 */
@BService
class Bot(private val config: Config) : JDAService() {
    override val intents: Set<GatewayIntent> = defaultIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)

    override val cacheFlags: Set<CacheFlag> = enumSetOf()



    override fun createJDA(event: BReadyEvent, eventManager: IEventManager) {
        defaultSharded(
            token = config.discord.token,
            activityProvider = { activityPProvider() }
        )
    }

    private fun activityPProvider() : Activity {
        return Activity.customStatus("Ich bin ein Test")
    }
}