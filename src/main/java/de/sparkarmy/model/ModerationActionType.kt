package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class ModerationActionType(override val offset: Int, val identifier: String) : BitField {
    UNKNOWN(-1, "Unknown"),

    WARN(1, "Warn"),
    MUTE(2, "Mute"),
    TIMEOUT(3, "Timeout"),
    KICK(7, "Kick"),
    BAN(9, "Ban"),
    UNBAN(10, "Unban"),

    TEMPBAN(14, "Temp-Ban"),
}