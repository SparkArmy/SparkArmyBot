package de.sparkarmy.model

import de.sparkarmy.data.BitField

@Suppress("unused")
enum class UserFlag(override val offset: Int, val identifier: String) : BitField {
    ADMIN(1, "Administrator"),
    VIP(0, "VIP"),
    OWNER(2, "Owner")
}