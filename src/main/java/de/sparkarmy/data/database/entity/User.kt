package de.sparkarmy.data.database.entity

import de.sparkarmy.data.database.table.Users
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class User(id: EntityID<Long>) : Entity<Long>(id) {
    companion object : EntityClass<Long, de.sparkarmy.data.database.entity.User>(Users) {
        fun new(user: User): de.sparkarmy.data.database.entity.User {
            return super.new(user.idLong) {username=user.name}
        }
    }

    var username by Users.username
    var displayName by Users.displayname
    var userFlags by Users.flags
}