package de.sparkarmy.data.database.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object DiscordMembers : Table("bot.table_member") {
    val memberId = uinteger("pk_mbr_id").autoIncrement()
    val userId = optReference(
        "fk_mbr_user_id",
        DiscordUsers.id,
        ReferenceOption.CASCADE,
        ReferenceOption.CASCADE,
        "table_member_table_user_pk_usr_id_fk"
    )
    val guildId = optReference(
        "fk_mbr_guild_id",
        DiscordGuilds,
        ReferenceOption.CASCADE,
        ReferenceOption.CASCADE,
        "table_member_table_guild_pk_gld_id_fk"
    )
    val values = long("mbr_values").default(0)

    override val primaryKey = PrimaryKey(memberId, name = "table_member_pk")
}