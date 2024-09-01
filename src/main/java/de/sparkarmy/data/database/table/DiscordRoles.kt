package de.sparkarmy.data.database.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object DiscordRoles : Table("bot.table_role") {
    val roleId = decimal("pk_rol_id", 50, 0)
    val guildId = optReference(
        "fk_rol_guild_id",
        DiscordGuilds,
        ReferenceOption.CASCADE,
        ReferenceOption.CASCADE,
        "table_role_table_guild_pk_gld_id"
    )
    val value = long("rol_value")

    override val primaryKey = PrimaryKey(roleId, name = "table_role_pk")
}