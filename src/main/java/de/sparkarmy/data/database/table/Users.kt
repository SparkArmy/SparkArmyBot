package de.sparkarmy.data.database.table

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.charLength

object Users : IdTable<Long>("table_user") {
    override val id = long("pk_usr_id").entityId()
    val username = varchar("usr_username",32).check{it.charLength().between(2,32)}
    val displayname = varchar("usr_displayname",100).nullable()
    val flags = long("usr_flags").default(0)

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "table_user_id_pk")
}