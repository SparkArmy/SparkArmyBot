package de.sparkarmy.data.database.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object DiscordMessages : Table("bot.table_message") {
    val messageId = decimal("pk_msg_id", 50, 0)
    val channelId = optReference(
        "fk_msg_channel_id",
        DiscordChannels,
        ReferenceOption.CASCADE,
        ReferenceOption.CASCADE,
        "table_message_table_channel_pk_cnl_id_fk"
    )
    val content = varchar("msg_content", 6000)
    val attachments = array<ByteArray>("msg_attachments")
    val timestamp = datetime("msg_timestamp").defaultExpression(CurrentDateTime)
}