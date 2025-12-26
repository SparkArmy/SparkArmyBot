package de.sparkarmy.database.entity

import de.sparkarmy.data.cache.ChannelCacheView
import de.sparkarmy.database.exposed.optionalRelated
import de.sparkarmy.database.exposed.provideUsing
import de.sparkarmy.database.table.Messages
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class Message(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Message>(Messages), KoinComponent {
        private val channelCacheView: ChannelCacheView by inject<ChannelCacheView>()
    }

    var msgContent by Messages.msgContent
    var msgChannel by Messages.msgChannel provideUsing channelCacheView
    var msgAttachments by Messages.msgAttachments
    var lastUpdate by Messages.lastUpdate
    var isDeleted by Messages.msgIsDeleted
    var reactionRoleMenu by optionalRelated(ReactionRoleMenu)
}