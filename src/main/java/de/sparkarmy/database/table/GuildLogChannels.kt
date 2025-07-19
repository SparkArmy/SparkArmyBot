package de.sparkarmy.database.table

import de.sparkarmy.data.bitField
import de.sparkarmy.model.LogChannelType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ShortColumnType
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object GuildLogChannels: IdTable<Long>("table_log_guild_channel") {
    override val id =
        reference("pk_fk_lcn_id", GuildChannels, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val webhookUrl = text("lcn_webhook_url")
    val logChannelType = registerColumn("lcn_type", ShortColumnType()).bitField<Short, LogChannelType>()

    override val primaryKey: PrimaryKey = PrimaryKey(id)

}