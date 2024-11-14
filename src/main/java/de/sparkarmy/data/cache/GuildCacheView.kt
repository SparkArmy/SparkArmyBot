package de.sparkarmy.data.cache


import de.sparkarmy.data.DBContext
import de.sparkarmy.database.entity.Guild
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import net.dv8tion.jda.api.entities.Guild as JDAGuild

private val log = KotlinLogging.logger { }

@Single(createdAtStart = true)
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