package de.sparkarmy.data.repository

import de.sparkarmy.data.database.Database


class Repository(private val database: Database) {
    val userRepository = UserRepository(database)
    val guildRepository = GuildRepository(database)
    val channelRepository = ChannelRepository(database)
}