package de.sparkarmy.data.cache

import de.sparkarmy.data.database.DBContext
import de.sparkarmy.data.database.entity.GuildChannel
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel as JDAGuildChannel

@Single
class GuildChannelCacheView(
    private val guildCacheView: GuildCacheView,
    private val db: DBContext
) : CacheView<Long, GuildChannel>(1000), KoinComponent {
    suspend fun save(jdaGuildChannel: JDAGuildChannel, edit: GuildChannel.() -> Unit = {}): GuildChannel = db.doTransaction {
        val id = jdaGuildChannel.idLong
        val guild = guildCacheView.save(jdaGuildChannel.guild)
        val name = jdaGuildChannel.name
        val type = jdaGuildChannel.type

        val guildChannel = getById(id)?.apply(edit)
            ?: GuildChannel.new(id) {
                this.guild = guild
                this.name = name
                this.type = type
            }

        if (id !in this@GuildChannelCacheView)
            put(id,guildChannel)

        guildChannel
    }

    override suspend fun load(key: Long): GuildChannel? = db.doTransaction {
        GuildChannel.findById(key)
    }
}