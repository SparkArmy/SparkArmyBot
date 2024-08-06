package de.sparkarmy.data.db.enties;

import de.sparkarmy.data.db.DatabaseSource;
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

@SuppressWarnings("unused")
public record DBChannel(long channelId,
                        long guildId,
                        String webhookUrl,
                        boolean isFeedbackChannel,
                        boolean isArchiveCategory,
                        boolean isMessageLog,
                        boolean isMemberLog,
                        boolean isCommandLog,
                        boolean isLeaveLog,
                        boolean isServerLog,
                        boolean isVoiceLog,
                        boolean isModLog) {

    private static final Logger logger = LoggerFactory.getLogger("");

    public static List<DBChannel> getChannelListFromDatabaseFromGuild(long guildId) {
        List<DBChannel> dbLogChannels = new ArrayList<>();
        try {
            Connection conn = DatabaseSource.connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM bot."table_channel"
                    WHERE "fk_cnlguildid" = ?;
                    """);
            prepStmt.setLong(1, guildId);
            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                dbLogChannels.add(new DBChannel(
                        rs.getLong("pk_cnlid"),
                        rs.getLong("fk_cnlguildid"),
                        rs.getString("cnlwebhookurl"),
                        rs.getBoolean("cnlisfeedbackchannel"),
                        rs.getBoolean("cnlisarchivecategory"),
                        rs.getBoolean("cnlismessagelog"),
                        rs.getBoolean("cnlismemberlog"),
                        rs.getBoolean("cnliscommandlog"),
                        rs.getBoolean("cnlisleavelog"),
                        rs.getBoolean("cnlisserverlog"),
                        rs.getBoolean("cnlisvoicelog"),
                        rs.getBoolean("cnlismodlog")
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

    public static List<DBChannel> getChannelListFromDatabaseFromGuild(@NotNull Guild guild) {
        return getChannelListFromDatabaseFromGuild(guild.getIdLong());
    }

    public static @Nullable DBChannel getMessageLogFromGuild(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isMessageLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBChannel getMessageLogFromGuild(@NotNull Guild guild) {
        return getMessageLogFromGuild(guild.getIdLong());
    }

    public static DBChannel getMemberLogFromGuild(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isMemberLog).toList();
        return logChannels.getFirst();
    }

    public static DBChannel getMemberLogFromGuild(@NotNull Guild guild) {
        return getMemberLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBChannel getCommandLogFromGuild(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isCommandLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBChannel getCommandLogFromGuild(@NotNull Guild guild) {
        return getCommandLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBChannel getLeaveLogFromGuild(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isLeaveLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBChannel getLeaveLogFromGuild(@NotNull Guild guild) {
        return getLeaveLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBChannel getServerLogFromGuild(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isServerLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBChannel getServerLogFromGuild(@NotNull Guild guild) {
        return getServerLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBChannel getVoiceLogFromGuild(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isVoiceLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBChannel getVoiceLogFromGuild(@NotNull Guild guild) {
        return getVoiceLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBChannel getModLogFromGuild(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isModLog).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBChannel getModLogFromGuild(@NotNull Guild guild) {
        return getModLogFromGuild(guild.getIdLong());
    }

    public static @Nullable DBChannel getFeedbackChannel(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isFeedbackChannel).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBChannel getFeedbackChannel(@NotNull Guild guild) {
        return getFeedbackChannel(guild.getIdLong());
    }

    public static @Nullable DBChannel getArchiveCategory(long guildId) {
        List<DBChannel> logChannels = getChannelListFromDatabaseFromGuild(guildId).stream().filter(DBChannel::isArchiveCategory).toList();
        if (logChannels.isEmpty()) return null;
        return logChannels.getFirst();
    }

    public static DBChannel getArchiveCategory(@NotNull Guild guild) {
        return getArchiveCategory(guild.getIdLong());
    }

    public long createOrUpdateChannel() {
        try (Connection conn = DatabaseSource.connection()) {
            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO bot."table_channel"
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?) on conflict
                    (pk_cnlid) do update SET
                        excluded."cnlisfeedbackchannel" = "cnlisfeedbackchannel",
                        excluded."cnlisarchivecategory" = "cnlisarchivecategory",
                        excluded."cnlismessagelog" = "cnlismessagelog",
                        excluded."cnlismemberlog" = "cnlismemberlog",
                        excluded."cnliscommandlog" = "cnliscommandlog",
                        excluded."cnlisserverlog" = "cnlisserverlog",
                        excluded."cnlisvoicelog" = "cnlisvoicelog",
                        excluded."cnlismodlog" = "cnlismodlog",
                        excluded."cnlisleavelog" = "cnlisleavelog"
                    """);
            prepStmt.setLong(1, this.channelId);
            prepStmt.setLong(2, this.guildId);
            prepStmt.setString(3, this.webhookUrl);
            prepStmt.setBoolean(4, this.isFeedbackChannel);
            prepStmt.setBoolean(5, this.isArchiveCategory);
            prepStmt.setBoolean(6, this.isMessageLog);
            prepStmt.setBoolean(7, this.isMemberLog);
            prepStmt.setBoolean(8, this.isCommandLog);
            prepStmt.setBoolean(9, this.isServerLog);
            prepStmt.setBoolean(10, this.isVoiceLog);
            prepStmt.setBoolean(11, this.isModLog);
            prepStmt.setBoolean(12, this.isVoiceLog);
            return prepStmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Can't insert or update DBChannel: ", e);
            return -200;
        }
    }

    public static long deleteChannel(long channelId) {
        try (Connection conn = DatabaseSource.connection()) {
            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM bot."table_channel" WHERE pk_cnlid = ?;
                    """);
            prepStmt.setLong(1, channelId);
            return prepStmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Can't delete DBChannel:", e);
            return -200;
        }
    }
}
