package de.sparkarmy.database.entity

import de.sparkarmy.database.table.GuildChannels
import de.sparkarmy.database.table.GuildLogChannels
import de.sparkarmy.model.LogChannelType
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class GuildLogChannel(id: EntityID<Long>): Entity<Long>(id) {
    companion object : EntityClass<Long, GuildLogChannel>(GuildLogChannels) {

        suspend fun getLogChannels(type: LogChannelType, guildId: Long) = newSuspendedTransaction {
            val query = GuildLogChannels
                .innerJoin(GuildChannels)
                .select(GuildLogChannels.columns)
                .where {
                    GuildChannels.guild eq guildId and (GuildLogChannels.logChannelType eq type)
                }
            wrapRows(query).toList()
        }

        suspend fun getLogChannels(guild: Guild) = newSuspendedTransaction {
            val query = GuildLogChannels
                .innerJoin(GuildChannels)
                .select(GuildLogChannels.columns)
                .where {
                    GuildChannels.guild eq guild.idLong
                }
            wrapRows(query).toList()
        }


    }

    var webhookUrl by GuildLogChannels.webhookUrl
    var channelType by GuildLogChannels.logChannelType
}