package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.AuditLogUtil;
import de.SparkArmy.utils.ChannelUtil;
import de.SparkArmy.utils.LogChannelType;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import org.jetbrains.annotations.NotNull;

public class MemberRoleLogging extends CustomEventListener {


    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        if (event.getUser().isBot()) return;
        AuditLogEntry entry = AuditLogUtil.getAuditLogEntryByUser(event.getUser(), ActionType.MEMBER_ROLE_UPDATE,event.getGuild());
        User moderator = null;
        if (entry != null){
            moderator = entry.getUser();
        }
        ChannelUtil.logInLogChannel(LoggingEmbeds.memberRoleLogging(event.getMember(), event.getRoles(),event,moderator),event.getGuild(), LogChannelType.MEMBER);
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        if (event.getUser().isBot()) return;
        AuditLogEntry entry = AuditLogUtil.getAuditLogEntryByUser(event.getUser(), ActionType.MEMBER_ROLE_UPDATE,event.getGuild());
        User moderator = null;
        if (entry != null){
            moderator = entry.getUser();
        }
        ChannelUtil.logInLogChannel(LoggingEmbeds.memberRoleLogging(event.getMember(), event.getRoles(),event,moderator),event.getGuild(), LogChannelType.MEMBER);
    }
}
