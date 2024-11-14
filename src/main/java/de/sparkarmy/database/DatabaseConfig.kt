package de.sparkarmy.database

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseConfig(
    val host: String,
    val database: String,
    val schema: String,
    val port: Int,
    val username: String,
    val password: String,
)
