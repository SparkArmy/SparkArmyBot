package de.sparkarmy.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object ReactionRoles : IdTable<Long>("table_reaction_role") {
    override val id = long("pk_rr_id").entityId().autoIncrement()
    val guildId = reference("fk_rr_guild_id", Guilds,onDelete = ReferenceOption.CASCADE,onUpdate = ReferenceOption.CASCADE)
    val messageId = long("rr_message_id")
    val channelId = long("rr_channel_id")
    val roleId = long("rr_role_id")
    val emoji  = varchar("rr_emoji", 100).nullable()

    override val primaryKey = PrimaryKey(id, name = "table_reaction_role_id_pk")
}
