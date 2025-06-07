package de.sparkarmy.jda.listeners

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.WebhookCacheView
import de.sparkarmy.database.entity.Guild
import de.sparkarmy.database.entity.GuildLogChannel
import de.sparkarmy.jda.JDAEventListener
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.update.GenericGuildUpdateEvent
import net.dv8tion.jda.api.events.guild.update.GuildUpdateNameEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.core.annotation.Single
import java.util.*
import net.dv8tion.jda.api.entities.Guild as JDAGuild

private val log = KotlinLogging.logger { }

@Single
class GuildUpdateListener(
    private val guildRepo: GuildCacheView,
    private val webhookRepo: WebhookCacheView
) : JDAEventListener {
    override val intents: EnumSet<GatewayIntent> = EnumSet.of(GatewayIntent.GUILD_MODERATION)

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildJoinEvent -> saveGuild(event.guild)
            is GuildReadyEvent -> saveGuild(event.guild)
            is GenericGuildUpdateEvent<*> -> updateGuild(event)
        }
    }

    private suspend fun saveGuild(guild: JDAGuild) {
        guildRepo.save(guild)
        webhookRepo.save(guild.jda, GuildLogChannel.getLogChannels(guild))
    }

    private val guildChanges: Map<String, Guild.(JDAGuild) -> Unit> = mapOf(
        GuildUpdateNameEvent.IDENTIFIER to { guildName = it.name },
    )

    private suspend fun updateGuild(event: GenericGuildUpdateEvent<*>) {
        val identifier = event.propertyIdentifier
        val change = guildChanges[identifier]
            ?: return

        val jdaGuild = event.guild
        val guildId = jdaGuild.idLong

        suspendTransaction {
            val guild = guildRepo.getById(guildId)
            if (guild == null) {
                log.warn { "Got GenericGuildUpdateEvent($identifier) for guild ($guildId) not stored in database!" }
                guildRepo.save(jdaGuild)
                return@suspendTransaction
            }

            guild.change(jdaGuild)
        }
    }
}