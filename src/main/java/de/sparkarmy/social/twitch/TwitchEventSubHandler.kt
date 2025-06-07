package de.sparkarmy.social.twitch

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.eventsub.events.StreamOnlineEvent
import com.github.twitch4j.eventsub.socket.conduit.TwitchConduitSocketPool
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes
import com.github.twitch4j.helix.domain.User
import com.github.twitch4j.kotlin.main.get
import de.sparkarmy.coroutines.virtualDispatcher
import org.koin.core.annotation.Single

@Single(createdAtStart = false)
class TwitchEventSubHandler(
    private val twitchConfig: TwitchConfig
) {
    private val twitchClient: TwitchClient = TwitchClientBuilder.builder()
        .withClientId(twitchConfig.clientId)
        .withClientSecret(twitchConfig.clientSecret)
        .withEnableHelix(true)
        .build()

    suspend fun getUserInformation(vararg userNames: String): List<User?>? {
        return twitchClient.helix.getUsers(null, null, userNames.toList()).get(virtualDispatcher).users
    }

    init {
        val conduit = TwitchConduitSocketPool.create {
            it.clientId(twitchConfig.clientId)
            it.clientSecret(twitchConfig.clientSecret)
            it.poolShards(4)
        }

        conduit.register(SubscriptionTypes.STREAM_ONLINE) { it.broadcasterUserId("").build() }
        conduit.eventManager.onEvent(StreamOnlineEvent::class.java) { streamOnlineEvent(it) }
    }

    private fun streamOnlineEvent(event: StreamOnlineEvent) {
        event.broadcasterUserLogin
        event.startedAt
    }
}

