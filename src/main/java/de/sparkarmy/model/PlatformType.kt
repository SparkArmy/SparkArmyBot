package de.sparkarmy.model

enum class PlatformType(val offset: Int, val identifier: String) {
    UNKNOWN(-1, "Unknown"),
    YOUTUBE(1, "YouTube"),
    TWITCH(2, "Twitch"),
}