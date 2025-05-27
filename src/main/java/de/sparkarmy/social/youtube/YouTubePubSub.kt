package de.sparkarmy.social.youtube

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val logger = KotlinLogging.logger("YouTubePubSub")

fun Application.youTubePubSub() {
    routing {
        get("/pubsubservice/youtube") {
            val response = call.request.queryParameters.getOrFail("hub.challenge")
            call.respond(response)
        }
        post("/pubsubservice/youtube") {
            val feed = call.receive<Feed>()
            logger.info { feed.entry.id }
        }
    }
}

@Serializable
private data class Feed(
    @SerialName("xmlns:yt") val xmlnsYt: String,
    val xmlns: String,
    @SerialName("link") val hubLink: Array<Link>,
    val title: String,
    val updated: String,
    val entry: Entry
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Feed

        if (xmlnsYt != other.xmlnsYt) return false
        if (xmlns != other.xmlns) return false
        if (!hubLink.contentEquals(other.hubLink)) return false
        if (title != other.title) return false
        if (updated != other.updated) return false
        if (entry != other.entry) return false

        return true
    }

    override fun hashCode(): Int {
        var result = xmlnsYt.hashCode()
        result = 31 * result + xmlns.hashCode()
        result = 31 * result + hubLink.contentHashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + updated.hashCode()
        result = 31 * result + entry.hashCode()
        return result
    }
}

@Serializable
private data class Link(
    val rel: String,
    val href: String
)

@Serializable
private data class Entry(
    val id: String,
    @SerialName("yt:videoId") val videoId: String,
    @SerialName("yt:channelId") val channelId: String,
    val title: String,
    val link: Link,
    val author: Author,
    val published: String,
    val updated: String
)

@Serializable
private data class Author(
    val name: String,
    val uri: String,
)
