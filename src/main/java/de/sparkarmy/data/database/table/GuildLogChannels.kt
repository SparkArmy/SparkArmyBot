package de.sparkarmy.data.database.table

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object GuildLogChannels: IdTable<Long>("table_log_guild_channel") {
    override val id = reference("pk_fk_lcn_id", GuildChannels, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val webhookUrl = text("lcn_webhook_url")
    val channelType = enumeration<ChannelType>("lcn_type")

    override val primaryKey: PrimaryKey = PrimaryKey(id)

}

enum class ChannelType(val id : Short) {
    MESSAGE_LOG(0),
    MEMBER_LOG(1),
    COMMAND_LOG(2),
    SERVER_LOG(3),
    VOICE_LOG(4),
    MOD_LOG(5),
    LEAVE_LOG(6),
}