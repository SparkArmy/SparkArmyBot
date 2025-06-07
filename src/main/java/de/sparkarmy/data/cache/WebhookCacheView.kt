package de.sparkarmy.data.cache

import de.sparkarmy.database.entity.GuildLogChannel
import dev.minn.jda.ktx.coroutines.await
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.IncomingWebhookClient
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.WebhookClient
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.core.annotation.Single

private val log = KotlinLogging.logger { "WebhookCacheView" }

@Single(createdAtStart = true)
class WebhookCacheView : CacheView<Long, IncomingWebhookClient?>(1000) {

    private var webhookList: HashMap<Long, IncomingWebhookClient> = HashMap()

    suspend fun save(jda: JDA, webhookChannelList: List<GuildLogChannel>) {
        webhookChannelList.forEach {
            webhookList.put(it.id.value, WebhookClient<Any>.createClient(jda, it.webhookUrl))
        }
    }

    private suspend fun remove(key: Long) = suspendTransaction {
        webhookList.remove(key)
        GuildLogChannel[key].delete()
    }


    override suspend fun load(key: Long): IncomingWebhookClient? {
        return webhookList[key]
    }

    suspend fun sendMessageEmbeds(id: Long, vararg embeds: MessageEmbed) {
        try {
            webhookList.get(id)?.sendMessageEmbeds(embeds.toList())?.await()
        } catch (e: ErrorResponseException) {
            log.warn { "Webhook from $id removed, can't send webhook message" }
            remove(id)
        }
    }

}