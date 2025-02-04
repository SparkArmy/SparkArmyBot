package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class RoleFlag(override val offset: Int, val identifier: String) : BitField {
    WARN(1, "Warn Role"),
    MUTE(2, "Mute Role")
}