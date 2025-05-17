package de.sparkarmy.model

import de.sparkarmy.data.BitField

enum class LogChannelType(override val offset: Int, val identifier: String) : BitField {
    MESSAGE_LOG(0, "Message-Log"),
    MEMBER_LOG(1, "Member-Log"),
    COMMAND_LOG(2, "Command-Log"),
    SERVER_LOG(3, "Server-Log"),
    VOICE_LOG(4, "Voice-Log"),
    MOD_LOG(5, "Mod-Log"),
    LEAVE_LOG(6, "Leave-Log"),
}