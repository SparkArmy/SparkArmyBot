package de.sparkarmy.model

import kotlinx.serialization.Serializable

@Serializable
data class JsonReactionRoleMenu(
    val description: String,
    val entries: kotlin.collections.List<ReactionRoleMenuEntry>,
)

@Serializable
data class ReactionRoleMenuEntry(
    val role: Long,
    val header: String,
    val description: String
)
