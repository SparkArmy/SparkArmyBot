package de.sparkarmy.jda.listeners

import de.sparkarmy.data.cache.MessageCacheView
import de.sparkarmy.jda.JDAEventListener
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import org.koin.core.annotation.Single
import java.util.*

@Single
class MessageListener(
    private val messageRepo: MessageCacheView
) : JDAEventListener {
    override val intents: EnumSet<GatewayIntent> =
        EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is MessageReceivedEvent -> messageCreateEvent(event)
            is MessageUpdateEvent -> messageUpdateEvent(event)
            is MessageDeleteEvent -> messageDeleteEvent(event)
        }
    }

    private suspend fun messageDeleteEvent(event: MessageDeleteEvent) {
        messageRepo.setDeleted(event.messageIdLong)
    }

    private suspend fun messageUpdateEvent(event: MessageUpdateEvent) {
        messageRepo.save(event.message)
    }

    private suspend fun messageCreateEvent(event: MessageReceivedEvent) {
        messageRepo.save(event.message)
    }
}