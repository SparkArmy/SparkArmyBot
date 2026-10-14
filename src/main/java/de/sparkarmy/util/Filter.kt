package de.sparkarmy.util

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member

fun checkGuildMember(member: Member?, guild: Guild?): String? {

    if (member == null) {
        return "Member was null"
    }
    if (guild == null) {
        return "Guild was null"
    }

    return null
}