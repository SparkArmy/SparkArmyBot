package de.sparkarmy.event

import de.sparkarmy.data.cache.UserCacheView
import io.github.freya022.botcommands.api.core.annotations.BEventListener
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.user.GenericUserEvent

private val logger = KotlinLogging.logger {}

@BService
class UserEvent(private val userCacheView: UserCacheView) {

    @BEventListener
    suspend fun onUserEvent(userEvent: GenericUserEvent) {
        logger.info { "Received user event ${userEvent.user.id}" }
        userCacheView.save(userEvent.user)
    }
}