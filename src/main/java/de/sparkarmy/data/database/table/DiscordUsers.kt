package de.sparkarmy.data.database.table


import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal


object DiscordUsers : IdTable<BigDecimal>("bot.table_user") {
    override val id: Column<EntityID<BigDecimal>> = decimal("pk_usr_id", 50, 0).entityId()
    val globalname = varchar("usr_globalname", 40)


    init {
        uniqueIndex("table_user_unique_globalname", globalname)
    }

}