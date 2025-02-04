package de.sparkarmy.database.table

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object GuildPunishmentConfigs : IdTable<Long>("table_guild_punishment_config") {
    override val id = reference("pk_fk_gco_guild_id", Guilds, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val muteRoleId = long("gco_mute_role_id").nullable()
    val warnRoleId = long("gco_warn_role_id").nullable()

    override val primaryKey = PrimaryKey(id)
}