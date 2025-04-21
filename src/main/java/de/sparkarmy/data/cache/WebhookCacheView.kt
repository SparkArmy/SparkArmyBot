package de.sparkarmy.data.cache

import de.sparkarmy.database.entity.GuildLogChannel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.IncomingWebhookClient
import net.dv8tion.jda.api.entities.WebhookClient
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

@Single(createdAtStart = true)
class WebhookCacheView : CacheView<Long, IncomingWebhookClient?>(1000) {

    private var webhookList: HashMap<Long, IncomingWebhookClient> = HashMap()

    suspend fun save(jda: JDA, webhookChannelList: List<GuildLogChannel>) {
        webhookChannelList.forEach {
            webhookList.put(it.id.value, WebhookClient<Any>.createClient(jda, it.webhookUrl))
        }
    }

    suspend fun remove(key: Long) = newSuspendedTransaction {
        webhookList.remove(key)
        GuildLogChannel[key].delete()
    }


    override suspend fun load(key: Long): IncomingWebhookClient? {
        return webhookList[key]
    }

}