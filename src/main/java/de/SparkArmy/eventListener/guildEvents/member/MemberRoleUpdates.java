package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.SqlUtil;
import de.SparkArmy.utils.jda.AuditLogUtil;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import org.jetbrains.annotations.NotNull;

public class MemberRoleUpdates extends CustomEventListener {


    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
       loggingRoleAdd(event);
       addUserRoleInDatabase(event);
       addOrUpdateMemberInModeratorTimeTable(event);
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
       loggingRoleRemove(event);
       removeUserRoleFromDatabase(event);
       addOrUpdateMemberInModeratorTimeTable(event);
    }

    private void loggingRoleAdd(@NotNull GuildMemberRoleAddEvent event){
        if (event.getUser().isBot()) return;
        AuditLogEntry entry = AuditLogUtil.getAuditLogEntryByUser(event.getUser(), ActionType.MEMBER_ROLE_UPDATE,event.getGuild());
        User moderator = null;
        if (entry != null){
            moderator = entry.getUser();
        }
        ChannelUtil.logInLogChannel(LoggingEmbeds.memberRoleLogging(event.getMember(), event.getRoles(),event,moderator),event.getGuild(), LogChannelType.MEMBER);
    }

    private void loggingRoleRemove(@NotNull GuildMemberRoleRemoveEvent event){
        if (event.getUser().isBot()) return;
        AuditLogEntry entry = AuditLogUtil.getAuditLogEntryByUser(event.getUser(), ActionType.MEMBER_ROLE_UPDATE,event.getGuild());
        User moderator = null;
        if (entry != null){
            moderator = entry.getUser();
        }
        ChannelUtil.logInLogChannel(LoggingEmbeds.memberRoleLogging(event.getMember(), event.getRoles(),event,moderator),event.getGuild(), LogChannelType.MEMBER);
    }

    private void addUserRoleInDatabase(@NotNull GuildMemberRoleAddEvent event){
        if (event.getUser().isBot()) return;
        SqlUtil.putDataInRoleUpdateTable(event.getGuild(), event.getMember(), event.getRoles());
    }

    private void removeUserRoleFromDatabase(@NotNull GuildMemberRoleRemoveEvent event){
        if (event.getUser().isBot()) return;
        SqlUtil.removeDataFromRoleUpdateTable(event);
    }

    private void addOrUpdateMemberInModeratorTimeTable(@NotNull GenericGuildMemberEvent event){
        Member member = event.getMember();
        if (member.getUser().isBot()) return;
        SqlUtil.putDataInModeratorTimeTable(event.getGuild(),member, member.hasPermission(Permission.KICK_MEMBERS));
    }
}
