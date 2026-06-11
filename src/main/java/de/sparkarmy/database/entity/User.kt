package de.sparkarmy.database.entity

import de.sparkarmy.database.table.Users
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class User(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<User>(Users)

    var username by Users.username
    var displayName by Users.displayname
    var avatar by Users.avatar
    var banner by Users.banner
    var userFlags by Users.flags
}