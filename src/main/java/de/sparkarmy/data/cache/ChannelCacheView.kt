package de.sparkarmy.data.cache

import de.sparkarmy.data.database.DBContext
import de.sparkarmy.data.database.entity.Channel
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import net.dv8tion.jda.api.entities.channel.Channel as JDAChannel

@Single
class ChannelCacheView(
    private val db: DBContext
) : CacheView<Long, Channel>(1000), KoinComponent {
    suspend fun save(jdaChannel: JDAChannel, edit: Channel.() -> Unit = {}): Channel = db.doTransaction {
        val id = jdaChannel.idLong
        val name = jdaChannel.name
        val type = jdaChannel.type



        val channel = getById(id)?.apply(edit)
            ?: Channel.new(id) {
                this.name = name
                this.type = type
            }

        if (id !in this@ChannelCacheView)
            put(id,channel)

        channel
    }

    override suspend fun load(key: Long): Channel? = db.doTransaction {
        Channel.findById(key)
    }
}