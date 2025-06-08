package de.sparkarmy.database.entity

import de.sparkarmy.database.table.GuildPunishmentConfigs
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class GuildPunishmentConfig(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, GuildPunishmentConfig>(GuildPunishmentConfigs)

    var muteRole by GuildPunishmentConfigs.muteRoleId
    var warnRole by GuildPunishmentConfigs.warnRoleId
}