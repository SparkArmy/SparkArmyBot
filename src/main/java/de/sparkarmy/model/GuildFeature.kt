package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class GuildFeature(override val offset: Int, val identifier: String) : BitField {
    LEVELING(0, "Leveling")
}