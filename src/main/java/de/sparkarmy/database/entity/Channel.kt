package de.sparkarmy.database.entity

import de.sparkarmy.database.exposed.optionalRelated
import de.sparkarmy.database.table.Channels
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class Channel(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Channel>(Channels)

    var name by Channels.name
    var type by Channels.type
    var guildChannel by optionalRelated(GuildChannel)
}