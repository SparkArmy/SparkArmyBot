package de.sparkarmy.database.entity

import de.sparkarmy.database.table.GuildNotificationChannels
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID

class GuildNotificationChannel(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<GuildNotificationChannel>(GuildNotificationChannels)

    var channel by GuildNotificationChannels.channel
    var contentCreator by GuildNotificationChannels.contentCreator
    var pingRoles by GuildNotificationChannels.roles
    var pingMessage by GuildNotificationChannels.message
    var webhookUrl by GuildNotificationChannels.webhookUrl
    var lastTime by GuildNotificationChannels.lastTime
    var expirationTime by GuildNotificationChannels.expirationTime
}