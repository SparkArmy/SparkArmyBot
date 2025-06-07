package de.sparkarmy.database.entity

import de.sparkarmy.database.table.GuildPunishmentConfigs
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class GuildPunishmentConfig(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, GuildPunishmentConfig>(GuildPunishmentConfigs)

    var muteRole by GuildPunishmentConfigs.muteRoleId
    var warnRole by GuildPunishmentConfigs.warnRoleId
}