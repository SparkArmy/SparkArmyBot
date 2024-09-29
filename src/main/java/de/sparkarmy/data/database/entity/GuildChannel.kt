package de.sparkarmy.data.database.entity

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.database.exposed.optionalRelated
import de.sparkarmy.data.database.exposed.provideUsing
import de.sparkarmy.data.database.table.GuildChannels
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GuildChannel(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, GuildChannel>(GuildChannels), KoinComponent {
        private val guildCacheView by inject<GuildCacheView>()
    }

    var guild by GuildChannels.guild provideUsing guildCacheView
    var name by GuildChannels.name
    var type by GuildChannels.type
    var logChannelConfig by optionalRelated(GuildLogChannel)
}