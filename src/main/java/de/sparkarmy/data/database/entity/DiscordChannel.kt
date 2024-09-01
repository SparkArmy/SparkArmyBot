package de.sparkarmy.data.database.entity

import de.sparkarmy.data.database.table.DiscordChannels
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.math.BigDecimal

class DiscordChannel(id: EntityID<BigDecimal>) : Entity<BigDecimal>(id) {
    var channelId by DiscordChannels.id
    var discordGuildId by DiscordGuild referencedOn DiscordChannels
    var webhook by DiscordChannels.webhookUrl
    var channelFlags by DiscordChannels.values


    companion object : EntityClass<BigDecimal, DiscordChannel>(DiscordChannels)

}