package de.sparkarmy.database.entity

import de.sparkarmy.database.table.BotStatuses
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class BotStatus(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, BotStatus>(BotStatuses)

    var status by BotStatuses.status
    var activity by BotStatuses.activity
    var url by BotStatuses.url
}