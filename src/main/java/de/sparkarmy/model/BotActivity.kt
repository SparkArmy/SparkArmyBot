package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class BotActivity(override val offset: Int) : BitField {
    UNKNOWN(0),
    PLAYING(1),
    STREAMING(2),
    LISTENING(3),
    WATCHING(4),
    CUSTOM(5),
    COMPETING(6),
}