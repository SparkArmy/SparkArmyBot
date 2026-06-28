package de.sparkarmy.database.table

import de.sparkarmy.data.bitField
import de.sparkarmy.model.BotActivity
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.charLength
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object BotStatuses : IdTable<Long>("table_bot_status") {
    override val id = long("pk_bts_id").entityId()
    val status = varchar("bts_status", 128).check { it.charLength().between(1, 128) }
    val activity = short("bts_activity").clientDefault { 0 }.bitField<Short, BotActivity>()
    val url = varchar("bts_url", 10000).nullable()
}