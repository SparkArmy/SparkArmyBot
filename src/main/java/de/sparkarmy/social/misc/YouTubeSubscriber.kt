package de.sparkarmy.social.misc


import de.sparkarmy.coroutines.virtualDispatcher
import de.sparkarmy.database.entity.ContentCreator
import de.sparkarmy.database.table.ContentCreators
import de.sparkarmy.model.PlatformType
import de.sparkarmy.social.twitch.TwitchEventSubHandler
import de.sparkarmy.social.youtube.YouTubeConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

@Single(createdAtStart = true)
class YouTubeSubscriber(
    private val youTubeConfig: YouTubeConfig,
    scope: CoroutineScope = de.sparkarmy.coroutines.newCoroutineScope<TwitchEventSubHandler>(virtualDispatcher)
) {
    init {
        scope.launch {
            newSuspendedTransaction {
                ContentCreator.find { ContentCreators.platform eq PlatformType.YOUTUBE }
                    .forEach {
                        val topicUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=${it.id.value}"
                        youTubeSubscribeCall(youTubeConfig.redirect, topicUrl)
                    }
            }
        }
    }
}

suspend fun youTubeSubscribeCall(redirectUrl: String, topicUrl: String) {
    val client = HttpClient(CIO)
    client.post("https://pubsubhubbub.appspot.com/subscribe") {
        setBody(
            MultiPartFormDataContent(
            formData {
                append("hub.callback", "$redirectUrl/pubsubservice/youtube")
                append("hub.mode", "subscribe")
                append("hub.topic", topicUrl)
                append("hub.verify", "async")
            }
        ))
    }
    client.close()
}

