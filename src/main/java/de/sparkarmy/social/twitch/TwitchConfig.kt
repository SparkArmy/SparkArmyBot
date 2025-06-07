package de.sparkarmy.social.twitch

import kotlinx.serialization.Serializable

@Serializable
data class TwitchConfig(
    val clientId: String,
    val clientSecret: String,
)
