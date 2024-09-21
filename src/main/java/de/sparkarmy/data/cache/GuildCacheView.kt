package de.sparkarmy.data.cache

import de.sparkarmy.data.database.DBContext
import de.sparkarmy.data.database.entity.Guild
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import net.dv8tion.jda.api.entities.Guild as JDAGuild

private val log = KotlinLogging.logger { }

@Single
class GuildCacheView(
    private val db: DBContext
) : CacheView<Long, Guild>(1000), KoinComponent {
    suspend fun save(jdaGuild: JDAGuild, edit: Guild.() -> Unit = {}): Guild = db.doTransaction {
        val id = jdaGuild.idLong

        val guild = getById(id)?.apply { updateMetadata(jdaGuild); edit(this) }
            ?: Guild.new(id) { setMetadata(jdaGuild); edit(this) }

        if (id !in this@GuildCacheView)
            put(id, guild)

        guild
    }

    private fun Guild.setMetadata(guild: JDAGuild) {
        guildName = guild.name
    }

    private fun Guild.updateMetadata(guild: JDAGuild) {
        if (guildName != guild.name)
            guildName = guild.name
    }

    override suspend fun load(key: Long): Guild? = db.doTransaction {
        Guild.findById(key)
    }
}