package de.sparkarmy.database.table

import de.sparkarmy.model.LogChannelType
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object GuildLogChannels: IdTable<Long>("table_log_guild_channel") {
    override val id = reference("pk_fk_lcn_id", Channels, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val webhookUrl = text("lcn_webhook_url")
    val logChannelType = enumeration<LogChannelType>("lcn_type")

    override val primaryKey: PrimaryKey = PrimaryKey(id)

}