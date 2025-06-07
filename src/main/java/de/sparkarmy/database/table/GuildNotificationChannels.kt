package de.sparkarmy.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.datetime.timestamp

object GuildNotificationChannels : CompositeIdTable("table_guild_notification_channel") {
    val channel = reference("pk_fk_ncl_channel_id", GuildChannels, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val contentCreator =
        reference("pk_fk_ncl_content_creator", ContentCreators, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val roles = array<Long>("ncl_roles")
    val message = varchar("ncl_message", 1000)
    val webhookUrl = varchar("ncl_webhook_url", 500)
    val lastTime = timestamp("ncl_last_time").nullable()

    init {
        addIdColumn(channel)
        addIdColumn(contentCreator)
    }

    override val primaryKey: PrimaryKey = PrimaryKey(channel, contentCreator)
}