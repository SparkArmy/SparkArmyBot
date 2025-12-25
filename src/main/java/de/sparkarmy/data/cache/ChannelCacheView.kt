package de.sparkarmy.data.cache

import de.sparkarmy.data.DBContext
import de.sparkarmy.database.entity.Channel
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import net.dv8tion.jda.api.entities.channel.Channel as JDAChannel

@Single(createdAtStart = true)
class ChannelCacheView(
    private val db: DBContext
) : CacheView<Long, Channel>(1000), KoinComponent {
    suspend fun save(jdaChannel: JDAChannel, edit: Channel.() -> Unit = {}): Channel = db.doTransaction {
        val id = jdaChannel.idLong

        val channel = getById(id)?.apply { updateMetadata(jdaChannel); edit(this) }
            ?: Channel.new(id) { setMetadata(jdaChannel); edit(this) }

        if (id !in this@ChannelCacheView)
            put(id,channel)

        channel
    }

    private fun Channel.setMetadata(jdaChannel: JDAChannel) {
        name = jdaChannel.name
        type = jdaChannel.type
    }

    private fun Channel.updateMetadata(jdaChannel: JDAChannel) {
        if (name != jdaChannel.name)
            name = jdaChannel.name
    }

    override suspend fun load(key: Long): Channel? = db.doTransaction {
        Channel.findById(key)
    }
}