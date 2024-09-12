package de.sparkarmy.data.db.entity

import de.sparkarmy.data.db.AbstractBaseLongEntity
import de.sparkarmy.data.db.AbstractBaseLongEntityClass
import de.sparkarmy.data.db.table.Guilds
import org.jetbrains.exposed.dao.id.EntityID

class Guild(id: EntityID<Long>): AbstractBaseLongEntity(id) {
    companion object: AbstractBaseLongEntityClass<Guild>(Guilds)
}