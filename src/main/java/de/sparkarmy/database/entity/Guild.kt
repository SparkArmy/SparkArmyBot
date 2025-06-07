package de.sparkarmy.database.entity

import de.sparkarmy.database.exposed.optionalRelated
import de.sparkarmy.database.table.Guilds
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class Guild(id: EntityID<Long>): Entity<Long>(id) {
    companion object : EntityClass<Long, Guild>(Guilds)

    var guildName by Guilds.guildName
    var guildIcon by Guilds.guildIcon
    var guildOwner by Guilds.guildOwnerId
    var guildFlags by Guilds.guildFlags
    var guildFeatures by Guilds.features
    var guildPunishmentConfig by optionalRelated(GuildPunishmentConfig)
}