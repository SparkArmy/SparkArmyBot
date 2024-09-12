package de.sparkarmy.data.repository

import de.sparkarmy.config.DatabaseSource

class Repository(database: DatabaseSource) {
    val userRepository: UserRepository = UserRepository(database)
}