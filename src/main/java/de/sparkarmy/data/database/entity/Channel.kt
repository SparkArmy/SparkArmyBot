package de.sparkarmy.data.database.entity

import de.sparkarmy.data.database.exposed.optionalRelated
import de.sparkarmy.data.database.table.Channels
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Channel(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Channel>(Channels)

    var name by Channels.name
    var type by Channels.type
    var guildChannel by optionalRelated(GuildChannel)
}