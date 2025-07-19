package de.sparkarmy.database.entity

import de.sparkarmy.data.cache.ChannelCacheView
import de.sparkarmy.database.exposed.provideUsing
import de.sparkarmy.database.table.Messages
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Message(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Message>(Messages), KoinComponent {
        private val channelCacheView: ChannelCacheView by inject<ChannelCacheView>()
    }

    var msgContent by Messages.msgContent
    var msgChannel by Messages.msgChannel provideUsing channelCacheView
    var msgAttachments by Messages.msgAttachments
    var lastUpdate by Messages.lastUpdate
//    var reactionRoleMenu by optionalRelated(ReactionRoleMenu)
}