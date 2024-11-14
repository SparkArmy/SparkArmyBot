package de.sparkarmy.jda.listeners

import de.sparkarmy.data.cache.ChannelCacheView
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.database.entity.Channel
import de.sparkarmy.database.entity.GuildChannel
import dev.minn.jda.ktx.events.CoroutineEventListener
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import net.dv8tion.jda.api.entities.channel.Channel as JDAChannel

private val log = KotlinLogging.logger { }

@Single
class ChannelUpdateListener(
    private val channelRepo: ChannelCacheView,
    private val guildRepo: GuildCacheView
) : CoroutineEventListener {
    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is GenericChannelUpdateEvent<*> -> updateChannel(event)
        }
    }

    private val channelChanges: Map<String, Channel.(JDAChannel) -> Unit> = mapOf(
        ChannelUpdateNameEvent.IDENTIFIER to {name = it.name},
    )

    private suspend fun updateChannel(event: GenericChannelUpdateEvent<*>) {
        val identifier = event.propertyIdentifier

        val jdaChannel = event.channel
        val channelId = event.channel.idLong

        newSuspendedTransaction {
            val channel = channelRepo.getById(channelId)
            if (channel == null) {
                log.warn { "Got GenericChannelUpdateEvent($identifier) for channel ($channelId) not stored in database!" }
                channelRepo.save(jdaChannel)
                if(event.isFromGuild) {
                    val guild = guildRepo.save(event.guild)
                    GuildChannel.new(channelId) {this.guild = guild}
                }
            }
        }
    }
}