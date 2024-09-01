package de.sparkarmy.data.database.entity

import com.apollographql.apollo.api.BigDecimal
import de.sparkarmy.data.database.table.DiscordUsers
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordUser(id: EntityID<BigDecimal>) : Entity<BigDecimal>(id) {
    var userId by DiscordUsers.id
    var userName by DiscordUsers.globalname

    fun deleteEntry() {
        transaction(db) {
            DiscordUsers.deleteWhere {
                id eq userId
            }
        }
    }


    companion object : EntityClass<BigDecimal, DiscordUser>(DiscordUsers)
}