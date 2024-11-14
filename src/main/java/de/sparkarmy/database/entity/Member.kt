package de.sparkarmy.database.entity

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.UserCacheView
import de.sparkarmy.database.exposed.provideUsing
import de.sparkarmy.database.table.Members
import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Member(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object: CompositeEntityClass<Member>(Members), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
        private val userCacheView by inject<UserCacheView>()
    }
    var user by Members.user provideUsing userCacheView
    var guild by Members.guild provideUsing guildCacheView
}