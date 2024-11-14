package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class UserFlag(override val offset: Int, val identifier: String) : BitField {
    ADMIN(1, "Administrator"),
    VIP(0, "VIP"),
}