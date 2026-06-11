package de.sparkarmy.data.cache

import de.sparkarmy.config.DatabaseSource
import de.sparkarmy.database.entity.Guild
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.entities.Guild as JDAGuild

@BService(name = "guildCacheView")
class GuildCacheView(
    private val db: DatabaseSource,
) : CacheView<Long, Guild>(1000) {
    suspend fun save(jdaGuild: JDAGuild, edit: Guild.() -> Unit = {}): Guild = db.doTransaction {
        val id = jdaGuild.idLong

        val guild = getById(id)?.apply { updateMetadata(jdaGuild); edit(this) }
            ?: Guild.new(id) { setMetadata(jdaGuild); edit(this) }

        if (id !in this@GuildCacheView)
            put(id, guild)

        guild
    }

    private fun Guild.setMetadata(jdaGuild: JDAGuild) {
        guildName = jdaGuild.name
        guildIcon = jdaGuild.iconId
        guildOwner = jdaGuild.ownerIdLong
    }

    private fun Guild.updateMetadata(jdaGuild: JDAGuild) {
        if (guildName != jdaGuild.name)
            guildName = jdaGuild.name
        if (guildIcon != jdaGuild.iconId)
            guildIcon = jdaGuild.iconId
        if (guildOwner != jdaGuild.ownerIdLong)
            guildOwner = jdaGuild.ownerIdLong
    }

    override suspend fun load(key: Long): Guild? = db.doTransaction {
        Guild.findById(key)
    }
}