package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class GuildFeature(override val offset: Int, val identifier: String) : BitField {
    LEVELING(0, "Leveling"),
    MOD_TICKET(1, "Ticket-Function"),
    FEEDBACK(2, "Feedback-Function"),
    MODERATION(3, "Moderation-Feature"),
    PUNISHMENT(4, "Punishment-Function")

}