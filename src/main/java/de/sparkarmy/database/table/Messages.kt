package de.sparkarmy.database.table

import kotlinx.datetime.Clock

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.timestamp

object Messages : IdTable<Long>("table_message") {
    override val id = long("pk_msg_id").entityId()
    val msgContent = varchar("msg_content", 10000)
    val msgChannel = reference("fk_msg_channel", Channels, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val msgAttachments = array<ByteArray>("msg_attachments").nullable()
    val lastUpdate = timestamp("msg_last_update").default(Clock.System.now())
}