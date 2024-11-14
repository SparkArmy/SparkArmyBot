package de.sparkarmy.database.entity

import de.sparkarmy.database.table.Guilds
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Guild(id: EntityID<Long>): Entity<Long>(id) {
    companion object: EntityClass<Long,Guild>(Guilds)

    var guildName by Guilds.guildName
    var guildIcon by Guilds.guildIcon
    var guildOwner by Guilds.guildOwnerId
    var guildFlags by Guilds.guildFlags
    var guildFeatures by Guilds.features
//    var guildChannel by optionalRelated(GuildChannel)
}