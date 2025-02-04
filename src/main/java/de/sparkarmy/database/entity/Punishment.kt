package de.sparkarmy.database.entity

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.UserCacheView
import de.sparkarmy.database.exposed.provideUsing
import de.sparkarmy.database.table.Punishments
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Punishment(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, Punishment>(Punishments), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
        private val userCacheView by inject<UserCacheView>()
    }

    var offender by Punishments.offender provideUsing userCacheView
    var moderator by Punishments.moderator provideUsing userCacheView
    var guild by Punishments.guild provideUsing guildCacheView
    var reason by Punishments.reason
}