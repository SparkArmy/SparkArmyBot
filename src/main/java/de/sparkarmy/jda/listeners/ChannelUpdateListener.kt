package de.sparkarmy.jda.listeners

import de.sparkarmy.data.cache.ChannelCacheView
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.database.entity.GuildChannel
import dev.minn.jda.ktx.events.CoroutineEventListener
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.channel.GenericChannelEvent
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

private val log = KotlinLogging.logger { }

@Single
class ChannelUpdateListener(
    private val channelRepo: ChannelCacheView,
    private val guildRepo: GuildCacheView
) : CoroutineEventListener {
    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is GenericChannelEvent -> updateChannel(event)
        }
    }

    private suspend fun updateChannel(event: GenericChannelEvent) {
        val identifier = when (event) {
            is GenericChannelUpdateEvent<*> -> event.propertyIdentifier
            is ChannelCreateEvent -> event.channel.name
            is ChannelDeleteEvent -> event.channel.name
            else -> "Not Provided"
        }

        val jdaChannel = event.channel
        val channelId = event.channel.idLong

        newSuspendedTransaction {
            val channel = channelRepo.getById(channelId)
            if (channel == null) {
                log.warn { "Got GenericChannelEvent($identifier) for channel ($channelId) not stored in database!" }
                channelRepo.save(jdaChannel)
                if(event.isFromGuild) {
                    val guild = guildRepo.save(event.guild)
                    GuildChannel.new(channelId) {this.guild = guild}
                }
            }
        }

        when (event) {is ChannelDeleteEvent -> {
            newSuspendedTransaction {
                channelRepo.getById(channelId)?.delete()
            }
        }}
    }
}