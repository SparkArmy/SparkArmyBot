package de.sparkarmy.database.entity

import de.sparkarmy.database.table.PersistentMessageViews
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class PersistentMessageView(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, PersistentMessageView>(PersistentMessageViews)

    var data: ByteArray by PersistentMessageViews.data
    var className: String by PersistentMessageViews.className
}