package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class GuildFlag(override val offset: Int, val identifier: String) : BitField {
    DEV_GUILD(0, "Development Guild")
}