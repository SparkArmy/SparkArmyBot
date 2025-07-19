package de.sparkarmy.jda.listeners

import de.sparkarmy.jda.JDAEventListener
import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import org.koin.core.annotation.Single
import java.util.*

@Single
class MessageUpdateListener : JDAEventListener {
    override val intents: EnumSet<GatewayIntent> =
        EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is MessageReceivedEvent -> messageCreateEvent(event)
            is MessageUpdateEvent -> messageUpdateEvent(event)
            is MessageDeleteEvent -> messageDeleteEvent(event)
        }
    }

    private fun messageDeleteEvent(event: MessageDeleteEvent) {
        TODO("Not yet implemented")
    }

    private suspend fun messageUpdateEvent(event: MessageUpdateEvent) {
        event.message.attachments[0].proxy.download().await().readAllBytes()
        TODO("Not yet implemented")
    }

    private fun messageCreateEvent(event: MessageReceivedEvent) {
        TODO("Not yet implemented")
    }
}