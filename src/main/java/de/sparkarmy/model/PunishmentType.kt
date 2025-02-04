package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class PunishmentType(override val offset: Int, val identifier: String) : BitField {
    WARN(1, "Warn"),
    MUTE(2, "Mute"),
    KICK(3, "Kick"),
    BAN(4, "Ban"),

    TEMPBAN(14, "Temp-Ban")
}