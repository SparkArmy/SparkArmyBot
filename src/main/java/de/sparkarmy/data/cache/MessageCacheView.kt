package de.sparkarmy.data.cache

import de.sparkarmy.data.DBContext
import de.sparkarmy.database.entity.Message
import kotlinx.coroutines.future.await
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import kotlin.time.Instant
import net.dv8tion.jda.api.entities.Message as JDAMessage

@Single(createdAtStart = true)
class MessageCacheView(
    private val db: DBContext,
    private val channelCacheView: ChannelCacheView
) : CacheView<Long, Message>(1000), KoinComponent {

    suspend fun save(jdaMessage: JDAMessage, edit: Message.() -> Unit = {}): Message = db.doTransaction {
        val id = jdaMessage.idLong
        val channel = channelCacheView.save(jdaMessage.channel)
        val attachments = jdaMessage.attachments.map { mapEntry -> mapEntry.proxy.download().await().readAllBytes() }

        val message = Message.findById(id)?.apply { updateMetadata(jdaMessage); edit(this) }
            ?: Message.new(id) {
                this.msgContent = jdaMessage.contentRaw
                this.msgChannel = channel
                this.msgAttachments = attachments
                this.lastUpdate = Instant.parse(jdaMessage.timeCreated.toString())
            }

        if (id !in this@MessageCacheView) {
            put(id, message)
        }

        message
    }

    suspend fun setDeleted(jdaMessageId: Long, edit: Message.() -> Unit = {}) = db.doTransaction {
        Message.findById(jdaMessageId)?.apply {
            isDeleted = true
            edit(this)
        }
    }

    private fun Message.updateMetadata(jdaMessage: JDAMessage) {
        if (msgContent != jdaMessage.contentRaw)
            msgContent = jdaMessage.contentRaw
        if (lastUpdate != jdaMessage.timeEdited)
            lastUpdate = Instant.parse(jdaMessage.timeEdited.toString())
    }


    override suspend fun load(key: Long): Message? = db.doTransaction {
        Message.findById(key)
    }
}