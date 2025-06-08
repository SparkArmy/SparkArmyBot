package de.sparkarmy.social.youtube

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeConfig(
    val redirect: String,
    val token: String,
)
