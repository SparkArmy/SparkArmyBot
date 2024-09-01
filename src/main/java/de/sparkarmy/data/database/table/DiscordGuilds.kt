package de.sparkarmy.data.database.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import java.math.BigDecimal

object DiscordGuilds : IdTable<BigDecimal>("bot.table_guild") {
    override val id: Column<EntityID<BigDecimal>> = decimal("pk_gld_id", 50, 0).entityId()
}