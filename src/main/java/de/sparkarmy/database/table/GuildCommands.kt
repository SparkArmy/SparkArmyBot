package de.sparkarmy.database.table

import org.jetbrains.exposed.sql.Table

object GuildCommands : Table("table_guild_commands") {
    val guild         = long("pk_gcd_guild")
    val identifier    = varchar("pk_gcd_identifier", 100)
    val commandHash   = char("gcd_hash", 64)

    override val primaryKey: PrimaryKey = PrimaryKey(guild, identifier)
}