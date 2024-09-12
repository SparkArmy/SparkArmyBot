package de.sparkarmy.data.db.table

import de.sparkarmy.data.db.AbstractBaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.charLength

object Guilds: AbstractBaseLongIdTable("table_guilds") {
    override val id: Column<EntityID<Long>> = long("pk_gld_id").entityId()
    val guildName: Column<String> = text("gld_name").check{it.charLength().between(2,100)}

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "table_guild_id_pk")
}