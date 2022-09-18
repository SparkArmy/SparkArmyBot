package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.jda.AuditLogUtil;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import de.SparkArmy.utils.SqlUtil;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentEmbeds;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

public class MemberTimeoutEvent extends CustomEventListener {
    @Override
    public void onGuildMemberUpdateTimeOut(@NotNull GuildMemberUpdateTimeOutEvent event) {
        logTimeout(event);
    }

    private void logTimeout(@NotNull GuildMemberUpdateTimeOutEvent event) {
        Guild guild = event.getGuild();
        if (event.getNewTimeOutEnd() == null) return;
        AuditLogEntry entry = AuditLogUtil.getAuditLogEntryByUser(event.getUser(), ActionType.MEMBER_UPDATE, guild);
        if (entry == null) return;
        AuditLogChange change = entry.getChangeByKey(AuditLogKey.MEMBER_TIME_OUT);
        if (change == null) return;
        String reason = entry.getReason() != null ? entry.getReason() : "No reason provided";
        if (entry.getUser() == null) return;
        Member moderator = guild.getMember(entry.getUser());
        if (moderator == null) return;
        EmbedBuilder logEmbed = PunishmentEmbeds.punishmentLogEmbed(guild, reason, event.getUser(), moderator.getUser(), event.getNewTimeOutEnd(), PunishmentType.TIMEOUT);
        ChannelUtil.logInLogChannel(logEmbed, guild, LogChannelType.MOD);

        EmbedBuilder userEmbed = PunishmentEmbeds.punishmentUserEmbed(guild, reason, event.getNewTimeOutEnd(), PunishmentType.TIMEOUT);
        event.getUser().openPrivateChannel().complete().sendMessageEmbeds(userEmbed.build()).queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));

        SqlUtil.putUserDataInUserTable(guild, event.getMember());
        if (SqlUtil.isUserNotInModeratorTable(guild,moderator)){
            SqlUtil.putUserDataInUserTable(guild, moderator);
            SqlUtil.putDataInModeratorTable(guild,moderator);
        }

        SqlUtil.putDataInPunishmentTable(guild,event.getMember(),moderator,PunishmentType.TIMEOUT);

    }
}
