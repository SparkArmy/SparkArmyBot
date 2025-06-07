package de.sparkarmy.database.entity

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.database.exposed.optionalRelated
import de.sparkarmy.database.exposed.provideUsing
import de.sparkarmy.database.table.GuildChannels
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GuildChannel(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, GuildChannel>(GuildChannels), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
    }

    var guild by GuildChannels.guild provideUsing guildCacheView
    var logChannel by optionalRelated(GuildLogChannel)
//    var mediaChannelConfig by optionalRelated(GuildMediaChannelConfig)
}