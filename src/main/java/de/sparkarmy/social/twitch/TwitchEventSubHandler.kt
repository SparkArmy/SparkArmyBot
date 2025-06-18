package de.sparkarmy.social.twitch

import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.eventsub.events.StreamOnlineEvent
import com.github.twitch4j.eventsub.socket.conduit.TwitchConduitSocketPool
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes
import com.github.twitch4j.helix.domain.User
import com.github.twitch4j.kotlin.main.get
import de.sparkarmy.coroutines.newCoroutineScope
import de.sparkarmy.coroutines.virtualDispatcher
import de.sparkarmy.database.entity.ContentCreator
import de.sparkarmy.database.table.ContentCreators
import de.sparkarmy.jda.JDAService
import de.sparkarmy.model.PlatformType
import de.sparkarmy.social.misc.createNotificationMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

@Single(createdAtStart = true)
class TwitchEventSubHandler(
    private val twitchConfig: TwitchConfig,
    private val jdaService: JDAService,
    private val scope: CoroutineScope = newCoroutineScope<TwitchEventSubHandler>(virtualDispatcher)
) {
    private val twitchClient: TwitchClient = TwitchClientBuilder.builder()
        .withClientId(twitchConfig.clientId)
        .withClientSecret(twitchConfig.clientSecret)
        .withEnableHelix(true)
        .build()

    private val conduit = TwitchConduitSocketPool.create {
        it.clientId(twitchConfig.clientId)
        it.clientSecret(twitchConfig.clientSecret)
        it.poolShards(4)
    }

    suspend fun getUserInformation(vararg userNames: String): List<User?>? {
        return twitchClient.helix.getUsers(null, null, userNames.toList()).get(virtualDispatcher).users
    }

    fun registerContentCreator(userId: String) {
        conduit.register(SubscriptionTypes.STREAM_ONLINE) { it.broadcasterUserId(userId).build() }
    }

    init {
        scope.launch {
            newSuspendedTransaction {
                ContentCreator.find {
                    ContentCreators.platform eq PlatformType.TWITCH
                }.forEach { contentCreator ->
                    val id = contentCreator.id.value
                    conduit.register(SubscriptionTypes.STREAM_ONLINE) { it.broadcasterUserId(id).build() }
                }
            }
        }

        conduit.eventManager.onEvent(StreamOnlineEvent::class.java) { streamOnlineEvent(it) }

    }

    private fun streamOnlineEvent(event: StreamOnlineEvent) {
        val id = event.broadcasterUserId
        val userLogin = event.broadcasterUserLogin
        val started = event.startedAt.let { Instant.fromEpochMilliseconds(it.toEpochMilli()) }
        val link = "https://twitch.tv/$userLogin"

        scope.launch { createNotificationMessage(jdaService, id, link, started) }

    }
}

