package de.sparkarmy.model

enum class LogChannelType(val id : Short) {
    MESSAGE_LOG(0),
    MEMBER_LOG(1),
    COMMAND_LOG(2),
    SERVER_LOG(3),
    VOICE_LOG(4),
    MOD_LOG(5),
    LEAVE_LOG(6),
}