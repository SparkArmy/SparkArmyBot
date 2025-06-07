package de.sparkarmy.database.entity

import de.sparkarmy.database.table.GuildNotificationChannels
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

class GuildNotificationChannel(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<GuildNotificationChannel>(GuildNotificationChannels)

    var channel by GuildNotificationChannels.channel
    var contentCreator by GuildNotificationChannels.contentCreator
    var pingRoles by GuildNotificationChannels.roles
    var pingMessage by GuildNotificationChannels.message
    var webhookUrl by GuildNotificationChannels.webhookUrl
    var lastTime by GuildNotificationChannels.lastTime
}