package de.sparkarmy.database.entity

import de.sparkarmy.database.table.GuildLogChannels
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildLogChannel(id: EntityID<Long>): Entity<Long>(id) {
    companion object : EntityClass<Long,GuildLogChannel>(GuildLogChannels)

    var webhookUrl by GuildLogChannels.webhookUrl
    var channelType by GuildLogChannels.logChannelType


}