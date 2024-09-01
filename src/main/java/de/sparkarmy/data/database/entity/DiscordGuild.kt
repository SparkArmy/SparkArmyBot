package de.sparkarmy.data.database.entity

import de.sparkarmy.data.database.table.DiscordChannels
import de.sparkarmy.data.database.table.DiscordGuilds
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class DiscordGuild(id: EntityID<BigDecimal>) : Entity<BigDecimal>(id) {
    var guildId by DiscordGuilds.id

    fun deleteEntry() {
        transaction(db) {
            DiscordGuilds.deleteWhere {
                id eq guildId
            }
        }
    }

    fun getDiscordChannelList(): List<DiscordChannel> {
        return transaction(db) {
            DiscordChannel.find(DiscordChannels.guildId eq guildId).toList()
        }
    }


    companion object : EntityClass<BigDecimal, DiscordGuild>(DiscordGuilds)
}