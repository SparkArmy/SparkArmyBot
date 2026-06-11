package de.sparkarmy.database.entity

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.UserCacheView
import de.sparkarmy.database.exposed.provideUsing
import de.sparkarmy.database.table.Members
import io.github.freya022.botcommands.api.core.service.annotations.BService
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

class Member(id: EntityID<CompositeID>) : CompositeEntity(id) {
    @BService
    companion object: CompositeEntityClass<Member>(Members){
        private val guildCacheView: GuildCacheView by lazy {guildCacheView}
        private val userCacheView: UserCacheView by lazy {userCacheView}
    }
    var user by Members.user provideUsing userCacheView
    var guild by Members.guild provideUsing guildCacheView
}