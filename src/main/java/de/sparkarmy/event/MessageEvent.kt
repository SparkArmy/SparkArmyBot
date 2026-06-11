package de.sparkarmy.event

import de.sparkarmy.data.cache.UserCacheView
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

private val logger = KotlinLogging.logger {}

@BService
class MessageEvent(private val userCacheView: UserCacheView) {
@BEventListener
suspend fun message(event: MessageReceivedEvent) {
    userCacheView.save(event.author)
    logger.info { "Received message ${event.messageId}" }

}
}