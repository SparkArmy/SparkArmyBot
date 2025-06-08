package de.sparkarmy.database.table

import de.sparkarmy.data.bitField
import de.sparkarmy.model.UserFlag
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ShortColumnType
import org.jetbrains.exposed.sql.charLength

object Users : IdTable<Long>("table_user") {
    override val id             = long("pk_usr_id").entityId()
    val username                = varchar("usr_username", 32).check { it.charLength().between(2, 32) }
    val displayname             = varchar("usr_displayname", 100).nullable()
    val avatar                  = text("usr_avatar").nullable()
    val banner                  = text("usr_banner").nullable()
    val flags                   = registerColumn("usr_flags", ShortColumnType()).clientDefault { 0 }.bitField<Short, UserFlag>()

    override val primaryKey = PrimaryKey(id, name = "table_user_id_pk")
}