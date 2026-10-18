package de.sparkarmy.database.entity

import de.sparkarmy.database.table.ReactionRoles
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class ReactionRole(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, ReactionRole>(ReactionRoles)

    var guildId by ReactionRoles.guildId
    var messageId by ReactionRoles.messageId
    var channelId by ReactionRoles.channelId
    var roleId by ReactionRoles.roleId
    var emoji by ReactionRoles.emoji
    var description by ReactionRoles.description
    var buttonText by ReactionRoles.buttonText
}
