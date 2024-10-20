package de.sparkarmy.data.database.table

import net.dv8tion.jda.api.entities.channel.ChannelType
import org.jetbrains.exposed.dao.id.IdTable

object Channels: IdTable<Long>("table_channel") {
    override val id = long("pk_cnl_id").entityId()
    val name = varchar("cnl_name",100)
    val type = enumeration<ChannelType>("cnl_type")
}