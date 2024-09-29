package de.sparkarmy.data.database.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.charLength

object Guilds: IdTable<Long>("table_guild") {
    override val id: Column<EntityID<Long>> = long("pk_gld_id").entityId()
    val guildName: Column<String> = text("gld_name").check{it.charLength().between(2,100)}
    val guildType = enumeration<GuildType>("gld_type").default(GuildType.NONE)

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "table_guild_id_pk")
}

enum class GuildType(id: Short) {
    NONE(0),
    DEV_GUILD(1)
}