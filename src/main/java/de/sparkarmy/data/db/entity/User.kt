package de.sparkarmy.data.db.entity

import de.sparkarmy.data.db.AbstractBaseLongEntity
import de.sparkarmy.data.db.AbstractBaseLongEntityClass
import de.sparkarmy.data.db.table.Users
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Long>) : AbstractBaseLongEntity(id) {
    companion object : AbstractBaseLongEntityClass<User>(Users)

}