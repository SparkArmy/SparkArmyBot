package de.sparkarmy.data.database.entity

import de.sparkarmy.data.database.table.Guilds
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Guild(id: EntityID<Long>): Entity<Long>(id) {
    companion object: EntityClass<Long,Guild>(Guilds) {
    }

    var guildId by Guilds.id
    var guildName by Guilds.guildName
}