package de.sparkarmy.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object GuildChannels: IdTable<Long>("table_guild_channel") {
    override val id = reference("pk_fk_gcl_channel_id", Channels, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val guild = reference("fk_gcl_guild_id", Guilds, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}
