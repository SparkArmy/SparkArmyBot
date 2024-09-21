package de.sparkarmy.jda

import kotlinx.serialization.Serializable

@Serializable
data class JdaConfig(
    val clientId: String,
    val token: String,
    val secret: String,
    val redirect: String
)