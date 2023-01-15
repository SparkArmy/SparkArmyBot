package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.PostgresConnection;
import de.SparkArmy.utils.jda.AuditLogUtil;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class MemberLeaveEvent extends CustomEventListener {

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        leaveLogging(event);
    }

    private void leaveLogging(@NotNull GuildMemberRemoveEvent event){
        Guild eventGuild = event.getGuild();
        User user = event.getUser();
        Member member = event.getMember();

        eventGuild.retrieveAuditLogs().queueAfter(2, TimeUnit.SECONDS,list-> {
            AuditLogEntry lastKickEntry = AuditLogUtil.getAuditLogEntryByUser(user,ActionType.KICK,list);
            AuditLogEntry lastBanEntry = AuditLogUtil.getAuditLogEntryByUser(user,ActionType.BAN,list);
            if (lastBanEntry != null && lastBanEntry.getType().equals(ActionType.BAN) && lastBanEntry.getTimeCreated().isAfter(OffsetDateTime.now().minusSeconds(4))) {
                ChannelUtil.logInLogChannel(user.getAsTag() + " bannend", eventGuild, LogChannelType.LEAVE);
                PunishmentUtil.sendBanOrKickEmbed(lastBanEntry,member);
            } else if (lastKickEntry != null && lastKickEntry.getType().equals(ActionType.KICK) && lastKickEntry.getTimeCreated().isAfter(OffsetDateTime.now().minusSeconds(4))) {
                ChannelUtil.logInLogChannel(user.getAsTag() + " kicked", eventGuild, LogChannelType.LEAVE);
                PunishmentUtil.sendBanOrKickEmbed(lastKickEntry,member);
            } else {
                ChannelUtil.logInLogChannel(user.getAsTag() + " leaved", eventGuild, LogChannelType.LEAVE);
                PostgresConnection.addLeaveTimestampInMemberTable(member);
            }
        });
    }
}
