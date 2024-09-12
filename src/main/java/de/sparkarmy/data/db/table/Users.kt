package de.sparkarmy.data.db.table

import de.sparkarmy.data.db.AbstractBaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.charLength

object Users : AbstractBaseLongIdTable("table_user") {
    override val id: Column<EntityID<Long>> = long("pk_usr_id").entityId()
    val username: Column<String> = varchar("usr_username",32).check{it.charLength().between(2,32)}
    val flags: Column<Long> = long("usr_flags").default(0)

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "table_user_id_pk")
}