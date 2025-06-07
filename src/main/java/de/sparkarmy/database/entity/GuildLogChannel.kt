package de.sparkarmy.database.entity

import de.sparkarmy.database.table.GuildChannels
import de.sparkarmy.database.table.GuildLogChannels
import de.sparkarmy.model.LogChannelType
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.*

class GuildLogChannel(id: EntityID<Long>): Entity<Long>(id) {
    companion object : EntityClass<Long, GuildLogChannel>(GuildLogChannels) {

        suspend fun getLogChannels(type: EnumSet<LogChannelType>, guildId: Long) = suspendTransaction {
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