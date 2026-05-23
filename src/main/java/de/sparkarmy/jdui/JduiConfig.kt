package de.sparkarmy.jdui

import kotlinx.serialization.Serializable

@Serializable
data class JduiConfig(
    val password: String,
    val salt: String,
)
