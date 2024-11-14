package de.sparkarmy.database.entity

import de.sparkarmy.data.cache.ChannelCacheView
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.database.exposed.optionalRelated
import de.sparkarmy.database.exposed.provideUsing
import de.sparkarmy.database.table.GuildChannels
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GuildChannel(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, GuildChannel>(GuildChannels), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
        private val channelCacheView by inject<ChannelCacheView>()
    }

    var channel by GuildChannels.id provideUsing channelCacheView
    var guild by GuildChannels.guild provideUsing guildCacheView
    var logChannel by optionalRelated(GuildLogChannel)
//    var mediaChannelConfig by optionalRelated(GuildMediaChannelConfig)
}