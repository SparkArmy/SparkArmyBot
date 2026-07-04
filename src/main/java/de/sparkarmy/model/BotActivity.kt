package de.sparkarmy.model

enum class BotActivity(val offset: Int) {
    UNKNOWN(0),
    PLAYING(1),
    STREAMING(2),
    LISTENING(3),
    WATCHING(4),
    CUSTOM(5),
    COMPETING(6),
}