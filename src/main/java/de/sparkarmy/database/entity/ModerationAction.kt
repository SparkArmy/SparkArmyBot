package de.sparkarmy.database.entity

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.UserCacheView
import de.sparkarmy.database.exposed.provideUsing
import de.sparkarmy.database.table.ModerationActions
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ModerationAction(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, ModerationAction>(ModerationActions), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
        private val userCacheView by inject<UserCacheView>()
    }

    var type by ModerationActions.type
    var offender by ModerationActions.offender provideUsing userCacheView
    var moderator by ModerationActions.moderator provideUsing userCacheView
    var guild by ModerationActions.guild provideUsing guildCacheView
    var reason by ModerationActions.reason
}