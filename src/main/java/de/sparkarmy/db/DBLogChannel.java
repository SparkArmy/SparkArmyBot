package de.sparkarmy.db;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public record DBLogChannel(long channelId,
                           String webhookUrl,
                           boolean isMessageLog,
                           boolean isMemberLog,
                           boolean isCommandLog,
                           boolean isLeaveLog,
                           boolean isServerLog,
                           boolean isVoiceLog,
                           boolean isModLog) {

    private static final Logger logger = LoggerFactory.getLogger("");

    public static List<DBLogChannel> getLogChannelFromDatabaseByGuildId(long guildId) {
        List<DBLogChannel> dbLogChannels = new ArrayList<>();
        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM guidconfigs."tblLogChannel" tLC
                    INNER JOIN guilddata."tblChannel" tC ON tLC."fk_lgcChannelId" = tC."cnlChannelId"
                    WHERE tC."fk_cnlGuildId" = ?;
                    """);
            prepStmt.setLong(1, guildId);
            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                logger.info(rs.getString("lgcWebhookUrl"));
                dbLogChannels.add(new DBLogChannel(
                        rs.getLong("fk_lgcChannelId"),
                        rs.getString("lgcWebhookUrl"),
                        rs.getBoolean("lgcIsMessageLog"),
                        rs.getBoolean("lgcIsMemberLog"),
                        rs.getBoolean("lgcIsCommandLog"),
                        rs.getBoolean("lgcIsLeaveLog"),
                        rs.getBoolean("lgcIsServerLog"),
                        rs.getBoolean("lgcIsVoiceLog"),
                        rs.getBoolean("lgcIsModLog")
                ));
            }
            conn.close();
            logger.info(dbLogChannels.toString());
            return dbLogChannels;
        } catch (SQLException e) {
            logger.error("Can't get DBLogChannel from database: ", e);
            return dbLogChannels;
        }
    }

    @SuppressWarnings("unused")
    public static List<DBLogChannel> getLogChannelFromDatabaseByGuild(@NotNull Guild guild) {
        return getLogChannelFromDatabaseByGuildId(guild.getIdLong());
    }

    public static @Nullable DBLogChannel getMessageLogFromGuild(long guildId) {
        List<DBLogChannel> logChannels = getLogChannelFromDatabaseByGuildId(guildId).stream().filter(DBLogChannel::isMessageLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    @SuppressWarnings("unused")
    public static DBLogChannel getMessageLogFromGuild(@NotNull Guild guild) {
        return getMessageLogFromGuild(guild.getIdLong());
    }

    public static DBLogChannel getMemberLogFromGuild(long guildId) {
        List<DBLogChannel> logChannels = getLogChannelFromDatabaseByGuildId(guildId).stream().filter(DBLogChannel::isMemberLog).toList();
        return logChannels.getFirst();
    }

    @SuppressWarnings("unused")
    public static DBLogChannel getMemberLogFromGuild(@NotNull Guild guild) {
        return getMemberLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBLogChannel getCommandLogFromGuild(long guildId) {
        List<DBLogChannel> logChannels = getLogChannelFromDatabaseByGuildId(guildId).stream().filter(DBLogChannel::isCommandLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    @SuppressWarnings("unused")
    public static DBLogChannel getCommandLogFromGuild(@NotNull Guild guild) {
        return getCommandLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBLogChannel getLeaveLogFromGuild(long guildId) {
        List<DBLogChannel> logChannels = getLogChannelFromDatabaseByGuildId(guildId).stream().filter(DBLogChannel::isLeaveLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    @SuppressWarnings("unused")
    public static DBLogChannel getLeaveLogFromGuild(@NotNull Guild guild) {
        return getLeaveLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBLogChannel getServerLogFromGuild(long guildId) {
        List<DBLogChannel> logChannels = getLogChannelFromDatabaseByGuildId(guildId).stream().filter(DBLogChannel::isServerLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBLogChannel getServerLogFromGuild(@NotNull Guild guild) {
        return getServerLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBLogChannel getVoiceLogFromGuild(long guildId) {
        List<DBLogChannel> logChannels = getLogChannelFromDatabaseByGuildId(guildId).stream().filter(DBLogChannel::isVoiceLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    @SuppressWarnings("unused")
    public static DBLogChannel getVoiceLogFromGuild(@NotNull Guild guild) {
        return getVoiceLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBLogChannel getModLogFromGuild(long guildId) {
        List<DBLogChannel> logChannels = getLogChannelFromDatabaseByGuildId(guildId).stream().filter(DBLogChannel::isModLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBLogChannel getModLogFromGuild(@NotNull Guild guild) {
        return getModLogFromGuild(guild.getIdLong());
    }

    @SuppressWarnings("unused")
    public long createEntry() {
        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO guidconfigs."tblLogChannel"("fk_lgcChannelId", "lgcWebhookUrl", "lgcIsMessageLog", "lgcIsMemberLog", "lgcIsCommandLog", "lgcIsLeaveLog", "lgcIsServerLog", "lgcIsVoiceLog", "lgcIsModLog")
                    VALUES (?,?,?,?,?,?,?,?,?) on conflict do nothing;
                    """);
            prepStmt.setLong(1, this.channelId);
            prepStmt.setString(2, this.webhookUrl);
            prepStmt.setBoolean(3, this.isMessageLog);
            prepStmt.setBoolean(4, this.isMemberLog);
            prepStmt.setBoolean(5, this.isCommandLog);
            prepStmt.setBoolean(6, this.isLeaveLog);
            prepStmt.setBoolean(7, this.isServerLog);
            prepStmt.setBoolean(8, this.isVoiceLog);
            prepStmt.setBoolean(9, this.isModLog);
            long result = prepStmt.executeUpdate();
            conn.close();
            return result;
        } catch (SQLException e) {
            return -200;
        }
    }
    // TODO Add Method to update Entry
}
