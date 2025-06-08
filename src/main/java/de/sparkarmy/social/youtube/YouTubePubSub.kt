package de.sparkarmy.social.youtube

import de.sparkarmy.database.entity.GuildNotificationChannel
import de.sparkarmy.database.table.GuildNotificationChannels
import de.sparkarmy.jda.JDAService
import de.sparkarmy.social.misc.createNotificationMessage
import de.sparkarmy.social.misc.youTubeSubscribeCall
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.json.XML
import kotlin.time.Duration.Companion.seconds

val logger = KotlinLogging.logger { "YouTubePubSub" }

fun Application.youTubePubSub(jdaService: JDAService, config: YouTubeConfig) {
    routing {
        get("/pubsubservice/youtube") {

            val queryParams = call.request.queryParameters
            val response = queryParams["hub.challenge"] ?: "Error"
            val topicUrl = queryParams["hub.topic"]
            val leaseSeconds = queryParams["hub.lease_seconds"]?.toInt()

            logger.info { response }
            logger.info { topicUrl }
            logger.info { leaseSeconds }
            logger.info { queryParams }

            youTubeResubscriber(leaseSeconds, topicUrl, config)

            call.respondText(response)
        }
        postYouTubePubSub(jdaService)
    }
}

private suspend fun youTubeResubscriber(leaseSeconds: Int?, topicUrl: String?, config: YouTubeConfig) {
    if (leaseSeconds == null || topicUrl == null) return

    val id = topicUrl.removePrefix("https://www.youtube.com/feeds/videos.xml?channel_id=")

    logger.info { "ContentCreatorId: $id" }

    newSuspendedTransaction {
        GuildNotificationChannel.find {
            GuildNotificationChannels.contentCreator eq id
        }
            .forEach {
                it.expirationTime = Clock.System.now().plus(leaseSeconds.seconds)
            }
    }

    delay((leaseSeconds - 60).seconds)
    val notificationChannels = newSuspendedTransaction {
        GuildNotificationChannel.find {
            GuildNotificationChannels.contentCreator eq id
        }
    }
    if (notificationChannels.empty()) return

    youTubeSubscribeCall(config.redirect, topicUrl)
}

private fun Routing.postYouTubePubSub(jdaService: JDAService) {
    post("/pubsubservice/youtube") {
        val contentType = call.request.contentType()

        if (!contentType.match(ContentType.Application.Xml) and
            !contentType.match(ContentType.Application.Atom) and
            !contentType.match(ContentType.Application.Json)
        ) {
            call.respond(HttpStatusCode.UnsupportedMediaType)
            return@post
        }

        val callAsText = call.receiveText()

        val obj = when {
            contentType.match(ContentType.Application.Json) -> {
                Json.decodeFromString<MainObject>(callAsText)
            }

            else -> {
                val callAsJsonString = XML.toJSONObject(callAsText).toString()
                Json.decodeFromString<MainObject>(callAsJsonString)
            }
        }

        for (entry in obj.feed.entry) {
            val id = entry.ytChannelId
            val videoId = entry.ytVideoId

            val link = "https://youtu.be/$videoId"

            val lastPublishing = Instant.parse(entry.published)

            createNotificationMessage(jdaService, id, link, lastPublishing)
        }

        call.respond(HttpStatusCode.OK)
    }
}

private object EntryListSerializer : JsonTransformingSerializer<List<Entry>>(ListSerializer(Entry.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element as? JsonArray ?: JsonArray(listOf(element))

}

@Serializable
@SerialName("")
private data class MainObject(
    val feed: Feed
)

@Serializable
@SerialName("feed")
private data class Feed(
    @SerialName("title")
    val title: String,

    @SerialName("published")
    val published: String,

    @SerialName("id")
    val id: String,

    @SerialName("xmlns:yt")
    val xmlnsYt: String,

    @SerialName("link")
    val link: List<Link>,

    @SerialName("xmlns:media")
    val xmlnsMedia: String,

    @SerialName("author")
    val author: Author,

    @SerialName("yt:channelId")
    val ytChannelId: String,

    @SerialName("xmlns")
    val xmlns: String,

    @Serializable(with = EntryListSerializer::class)
    @SerialName("entry")
    val entry: List<Entry>
)

@Serializable
private data class Entry(
    val id: String,

    val published: String,

    val title: String,

    val updated: String,

    @SerialName("yt:channelId")
    val ytChannelId: String,

    @SerialName("yt:videoId")
    val ytVideoId: String,

    val author: Author,

    val link: Link,

    @SerialName("media:group")
    val mediaGroup: MediaGroup,
)

@Serializable
private data class MediaGroup(
    @SerialName("media:description")
    val mediaDescription: String,

    @SerialName("media:thumbnail")
    val mediaThumbnail: MediaThumbnail,

    @SerialName("media:title")
    val mediaTitle: String,

    @SerialName("media:community")
    val mediaCommunity: MediaCommunity,

    @SerialName("media:content")
    val mediaContent: MediaContent,
)

@Serializable
private data class MediaThumbnail(
    val width: Int,

    val url: String,

    val height: Int,
)

@Serializable
private data class MediaCommunity(
    @SerialName("media:statistics")
    val mediaStatistics: MediaStatistics,

    @SerialName("media:starRating")
    val mediaStarRating: MediaStarRating,
)

@Serializable
private data class MediaStatistics(
    val views: Int,
)

@Serializable
private data class MediaStarRating(
    val average: Int,

    val min: Int,

    val max: Int,

    val count: Int,
)

@Serializable
private data class MediaContent(
    val width: Int,

    val type: String,

    val url: String,

    val height: Int,
)


@Serializable
private data class Link(
    val rel: String,

    val href: String,
)

@Serializable
private data class Author(
    val name: String,

    val uri: String,
)
