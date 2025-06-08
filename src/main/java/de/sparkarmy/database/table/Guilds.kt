package de.sparkarmy.database.table

import de.sparkarmy.data.bitField
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.model.GuildFlag
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.charLength

object Guilds: IdTable<Long>("table_guild") {
    override val id     = long("pk_gld_id").entityId()
    val guildName       = text("gld_name").check{it.charLength().between(2,100)}
    val guildIcon       = text("gld_icon").nullable()
    val guildOwnerId    = long("gld_owner_id")
    val guildFlags      = short("gld_flags").clientDefault { 0 }.bitField<Short, GuildFlag>()
    val features        = integer("gld_features").bitField<Int, GuildFeature>().nullable()

    override val primaryKey = PrimaryKey(id, name = "table_guild_id_pk")
}