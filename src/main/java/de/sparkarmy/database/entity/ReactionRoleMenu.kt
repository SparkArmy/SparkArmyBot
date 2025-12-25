package de.sparkarmy.database.entity

import de.sparkarmy.database.table.ReactionRoleMenus
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class ReactionRoleMenu(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, ReactionRoleMenu>(ReactionRoleMenus)

    var description by ReactionRoleMenus.description
    var entries by ReactionRoleMenus.entries

}