package de.SparkArmy.db;

import de.SparkArmy.Main;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.NotificationService;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.SparkArmy.db.DatabaseSource.connection;

public class Postgres {

    private final boolean isPostgresDisabled;

    public Postgres(@NotNull Main main) {
        Logger logger = main.getLogger();
        boolean disabled = true;
        // try connection
        try {
            Connection conn = connection();
            logger.info("postgres-connected");
            conn.close();
            // set config global and set postgresEnabled
            disabled = false;
        } catch (SQLException e) {
            logger.error("Please setup a PostgresDatabase and establish a connection");
            main.systemExit(40);
        }
        this.isPostgresDisabled = disabled;
    }

    private boolean isGuildIdInDatabase(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("SELECT COUNT(*) FROM guilddata.\"tblGuild\" WHERE \"gldId\" = ?;");
        prepStmt.setLong(1, guildId);
        return checkResultSetForARow(prepStmt);
    }

    private void putGuildIdInGuildTable(@NotNull Connection conn, long guildId) throws SQLException {
        if (isGuildIdInDatabase(conn, guildId)) return;
        PreparedStatement prepStmt = conn.prepareStatement(
                "INSERT INTO guilddata.\"tblGuild\" (\"gldId\")VALUES (?);");
        prepStmt.setLong(1, guildId);
        prepStmt.execute();
    }

    private boolean isUserIdInDatabase(@NotNull Connection conn, long userId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("SELECT COUNT(*) FROM guilddata.\"tblUser\" WHERE \"usrId\" = ?;");
        prepStmt.setLong(1, userId);
        return checkResultSetForARow(prepStmt);
    }

    private void putUserIdInUserTable(@NotNull Connection conn, long userId) throws SQLException {
        if (isUserIdInDatabase(conn, userId)) return;
        PreparedStatement prepStmt = conn.prepareStatement(
                "INSERT INTO guilddata.\"tblUser\" (\"usrId\")VALUES (?);");
        prepStmt.setLong(1, userId);
        prepStmt.execute();
    }

    private boolean isMemberInMemberTable(@NotNull Connection conn, long userId, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM guilddata.\"tblMember\" WHERE \"fk_mbrUserId\" = ? AND \"fk_mbrGuildId\" = ?;");
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        return checkResultSetForARow(prepStmt);
    }

    private void putMemberInMemberTable(Connection conn, long userId, long guildId) throws SQLException {
        if (isMemberInMemberTable(conn, userId, guildId)) return;

        putUserIdInUserTable(conn, userId);
        putGuildIdInGuildTable(conn, guildId);

        PreparedStatement prepStmt = conn.prepareStatement(
                "INSERT INTO guilddata.\"tblMember\" (\"fk_mbrUserId\",\"fk_mbrGuildId\") VALUES (?,?);");
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        prepStmt.execute();
    }

    private long getMemberIdFromMemberTable(@NotNull Connection conn, long userId, long guildId) throws SQLException {
        putMemberInMemberTable(conn, userId, guildId);
        ResultSet rs;
        try (PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT \"mbrId\" FROM guilddata.\"tblMember\" WHERE \"fk_mbrUserId\" = ? AND \"fk_mbrGuildId\" = ?;")) {
            prepStmt.setLong(1, userId);
            prepStmt.setLong(2, guildId);
            rs = prepStmt.executeQuery();
            if (!rs.next()) return -1;
            return rs.getLong(1);
        }
    }

    private long getUserIdFromMemberTableByMemberId(@NotNull Connection conn, long mbrId) throws SQLException {
        ResultSet rs;
        try (PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT \"fk_mbrUserId\" FROM guilddata.\"tblMember\" WHERE \"mbrId\" = ?;")) {
            prepStmt.setLong(1, mbrId);
            rs = prepStmt.executeQuery();
            if (!rs.next()) return -1;
            return rs.getLong(1);
        }
    }


    private boolean isModeratorInModeratorTable(@NotNull Connection conn, long databaseMemberId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM guilddata.\"tblModerator\" WHERE \"fk_modMemberId\" = ?;");
        prepStmt.setLong(1, databaseMemberId);
        return checkResultSetForARow(prepStmt);
    }

    private long getModeratorIdFromModeratorTable(@NotNull Connection conn, long databaseMemberId) throws SQLException {
        putDataInModeratorTable(conn, databaseMemberId);
        PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT \"modId\" FROM guilddata.\"tblModerator\" WHERE \"fk_modMemberId\" = ?;");
        prepStmt.setLong(1, databaseMemberId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return -1;
        return rs.getLong(1);
    }

    private long getMemberIdFromModeratorId(@NotNull Connection conn, long databaseModeratorId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_mbrUserId" FROM guilddata."tblMember" WHERE "mbrId" = (SELECT "fk_modMemberId" FROM guilddata."tblModerator" WHERE "modId" = ?);""");
        prepStmt.setLong(1, databaseModeratorId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return -1;
        return rs.getLong(1);
    }

    private void putDataInModeratorTable(Connection conn, long databaseMemberId) throws SQLException {
        if (isModeratorInModeratorTable(conn, databaseMemberId)) return;
        PreparedStatement prepStmt = conn.prepareStatement(
                "INSERT INTO guilddata.\"tblModerator\" (\"fk_modMemberId\")VALUES (?);");
        prepStmt.setLong(1, databaseMemberId);
        prepStmt.execute();
    }

    private long getPunishmentCountFromGuild(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM guilddata.\"tblPunishment\" WHERE \"fk_psmGuildId\" = ?;");
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        checkResultSetForARow(rs);
        return rs.getLong(1);
    }

    public long getPunishmentCountFromGuild(Guild guild) {
        if (isPostgresDisabled) return -1;
        try {
            long guildId = guild.getIdLong();

            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT COUNT(*) FROM guilddata."tblPunishment" WHERE "fk_psmGuildId" = ?;
                    """);
            prepStmt.setLong(1, guildId);
            ResultSet rs = prepStmt.executeQuery();
            checkResultSetForARow(rs);
            long results = rs.getLong(1);
            conn.close();
            return results;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return -1;
        }
    }

    public boolean putPunishmentDataInPunishmentTable(User target, Member moderator, int punishmentType, String reason) {
        if (isPostgresDisabled) return true;
        try {
            Connection conn = connection();

            // Get Guild and Member ID's from target and moderator
            long guildId = moderator.getGuild().getIdLong();
            long moderatorId = moderator.getIdLong();
            long targetId = target.getIdLong();

            // Get DatabaseMember ID's
            long modMemberId = getMemberIdFromMemberTable(conn, moderatorId, guildId);
            long targetMemberId = getMemberIdFromMemberTable(conn, targetId, guildId);

            // Get Moderator ID
            long modId = getModeratorIdFromModeratorTable(conn, modMemberId);

            // Get guild punishment count
            long psmCount = getPunishmentCountFromGuild(conn, guildId) + 1; // Get the Count and add one

            PreparedStatement prepStmt = conn.prepareStatement(
                    """
                                INSERT INTO guilddata."tblPunishment" ("fk_psmMemberId","fk_psmModeratorId",
                                "fk_psmPunishmentTypeId","psmReason","psmTimestamp","psmGuildCount","fk_psmGuildId")
                                VALUES (?,?,?,?,now(),?,?);
                            """);

            prepStmt.setLong(1, targetMemberId);
            prepStmt.setLong(2, modId);
            prepStmt.setInt(3, punishmentType);
            prepStmt.setString(4, reason);
            prepStmt.setLong(5, psmCount);
            prepStmt.setLong(6, guildId);

            prepStmt.execute();
            conn.close();
            return false;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return true;
        }
    }

    public boolean putDataInNoteTable(String note, long targetMemberId, long moderatorId, long guildId) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            long databaseMemberId = getMemberIdFromMemberTable(conn, targetMemberId, guildId);
            long databaseModeratorId = getModeratorIdFromModeratorTable(conn, getMemberIdFromMemberTable(conn, moderatorId, guildId));

            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO guilddata."tblNote" ("fk_notMemberId", "notContent", "fk_notModeratorId","notTimestamp") VALUES (?,?,?,now());
                    """);
            prepStmt.setLong(1, databaseMemberId);
            prepStmt.setLong(3, databaseModeratorId);
            prepStmt.setString(2, note);

            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return false;
        }
    }

    public boolean updateDataFromNoteTable(String note, long mbrId, long modId, long guildId, Timestamp timestamp) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            long databaseMemberId = getMemberIdFromMemberTable(conn, mbrId, guildId);
            long databaseModeratorId = getModeratorIdFromModeratorTable(conn, getMemberIdFromMemberTable(conn, modId, guildId));

            PreparedStatement prepStmt = conn.prepareStatement("""
                    UPDATE guilddata."tblNote" SET "notContent" = ? WHERE "fk_notMemberId" = ? AND "fk_notModeratorId" = ? AND "notTimestamp" = ?;
                    """);
            prepStmt.setString(1, note);
            prepStmt.setLong(2, databaseMemberId);
            prepStmt.setLong(3, databaseModeratorId);
            prepStmt.setTimestamp(4, timestamp);
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return false;
        }
    }

    public boolean deleteDataFromNoteTable(long mbrId, long modId, long guildId, Timestamp timestamp) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            long databaseMemberId = getMemberIdFromMemberTable(conn, mbrId, guildId);
            long databaseModeratorId = getModeratorIdFromModeratorTable(conn, getMemberIdFromMemberTable(conn, modId, guildId));

            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM guilddata."tblNote" WHERE "notTimestamp" = ? AND "fk_notModeratorId" = ? AND  "fk_notMemberId" = ?;
                    """);
            prepStmt.setTimestamp(1, timestamp);
            prepStmt.setLong(2, databaseModeratorId);
            prepStmt.setLong(3, databaseMemberId);
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return false;
        }
    }

    public JSONObject getDataFromNoteTable(long targetId, long guildId) {
        if (isPostgresDisabled) return new JSONObject();
        try {
            Connection conn = connection();
            long databaseMemberId = getMemberIdFromMemberTable(conn, targetId, guildId);

            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM guilddata."tblNote" WHERE "fk_notMemberId" = ? ORDER BY "notTimestamp";
                                        """);

            prepStmt.setLong(1, databaseMemberId);

            ResultSet rs = prepStmt.executeQuery();

            JSONObject results = new JSONObject();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("notTimestamp");
                Long moderatorMemberId = getMemberIdFromModeratorId(conn, rs.getLong("fk_notModeratorId"));
                String content = rs.getString("notContent");
                JSONObject entry = new JSONObject();
                entry.put("moderatorId", moderatorMemberId);
                entry.put("noteContent", content);
                results.put(timestamp.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), entry);
            }
            conn.close();
            return results;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return new JSONObject();
        }
    }

    private long getServiceIdByNotificationService(@NotNull Connection conn, @NotNull NotificationService service) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "srvId" FROM notification."tblService" WHERE "srvName" = ?;
                   """);
        prepStmt.setString(1, service.getServiceName());
        ResultSet rs = prepStmt.executeQuery();
        checkResultSetForARow(rs);
        return rs.getLong(1);
    }

    private boolean isContentCreatorInContentCreatorTable(@NotNull Connection conn, String channelId, long databaseServiceId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM notification."tblContentCreator" WHERE "ctcServiceId" = ? AND "fk_ctcService" = ?;
                """);
        prepStmt.setString(1, channelId);
        prepStmt.setLong(2, databaseServiceId);
        return checkResultSetForARow(prepStmt);
    }

    public boolean putDataInContentCreatorTable(NotificationService service, String channelName, String channelId) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            long databaseServiceId = getServiceIdByNotificationService(conn, service);

            if (isContentCreatorInContentCreatorTable(conn, channelId, databaseServiceId)) return true;

            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO notification."tblContentCreator" ("ctcName", "fk_ctcService", "ctcServiceId") VALUES (?,?,?);
                    """);
            prepStmt.setString(1, channelName);
            prepStmt.setLong(2, databaseServiceId);
            prepStmt.setString(3, channelId);
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return false;
        }
    }

    private boolean existRowInSubscribedChannelTable(@NotNull Connection conn, long guildChannelId, String contentCreatorId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM notification."tblSubscribedChannel" WHERE "sbcChannelld" = ? AND "fk_sbcContentCreatorId" = ?;
                """);
        prepStmt.setLong(1, guildChannelId);
        prepStmt.setString(2, contentCreatorId);
        return checkResultSetForARow(prepStmt);
    }

    public boolean existRowInSubscribedChannelTable(long guildChannelId, String contentCreatorId) {
        if (isPostgresDisabled) return true;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT COUNT(*) FROM notification."tblSubscribedChannel" WHERE "sbcChannelld" = ? AND "fk_sbcContentCreatorId" = ?;
                    """);

            prepStmt.setLong(1, guildChannelId);
            prepStmt.setString(2, contentCreatorId);
            boolean checkResult = checkResultSetForARow(prepStmt);
            conn.close();
            return checkResult;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return true;
        }
    }

    public boolean putDataInSubscribedChannelTable(Collection<GuildChannel> guildChannels, String userId, String message) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();

            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO notification."tblSubscribedChannel" ("sbcChannelld", "fk_sbcGuildId", "fk_sbcContentCreatorId", "sctMessageText") VALUES (?,?,?,?);
                    """);
            prepStmt.setString(3, userId);
            prepStmt.setString(4, message);

            for (GuildChannel guildChannel : guildChannels) {
                long guildId = guildChannel.getGuild().getIdLong();
                long channelId = guildChannel.getIdLong();
                prepStmt.setLong(1, channelId);
                prepStmt.setLong(2, guildId);
                if (!existRowInSubscribedChannelTable(conn, channelId, userId)) {
                    prepStmt.execute();
                }
            }
            conn.close();
            return true;

        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return false;
        }
    }

    public JSONArray getDataFromSubscribedChannelTableByService(NotificationService service) {
        if (isPostgresDisabled) return new JSONArray();
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT  "ctcServiceId","ctcName","sbcChannelld","fk_sbcGuildId","sctMessageText" FROM notification."tblSubscribedChannel"
                    INNER JOIN notification."tblContentCreator" tCC on tCC."ctcServiceId" = "tblSubscribedChannel"."fk_sbcContentCreatorId"
                    WHERE tCC."fk_ctcService" = ?;
                    """);
            prepStmt.setLong(1, getServiceIdByNotificationService(conn, service));
            ResultSet rs = prepStmt.executeQuery();
            JSONArray results = new JSONArray();
            while (rs.next()) {
                extractContentDataInContentCreatorTable(rs, results);
            }
            conn.close();
            return results;

        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return new JSONArray();
        }
    }

    public JSONArray getDataFromSubscribedChannelTableByContentCreatorId(String contentCreatorId) {
        if (isPostgresDisabled) return new JSONArray();
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT  "ctcServiceId","ctcName","sbcChannelld","fk_sbcGuildId","sctMessageText" FROM notification."tblSubscribedChannel"
                    INNER JOIN notification."tblContentCreator" tCC on tCC."ctcServiceId" = "tblSubscribedChannel"."fk_sbcContentCreatorId"
                    WHERE tCC."ctcServiceId" = ?
                    ORDER BY "ctcName";
                    """);

            prepStmt.setString(1, contentCreatorId);

            ResultSet rs = prepStmt.executeQuery();
            JSONArray results = new JSONArray();
            while (rs.next()) {
                extractContentDataInContentCreatorTable(rs, results);
            }
            conn.close();
            return results;

        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return new JSONArray();
        }
    }

    public boolean removeDataFromSubscribedChannelTable(List<String> channelIds, String contentCreatorId) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            conn.setAutoCommit(false);
            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM notification."tblSubscribedChannel" WHERE "sbcChannelld" = ? AND "fk_sbcContentCreatorId" = ?;
                    """);
            prepStmt.setString(2, contentCreatorId);

            for (String s : channelIds) {
                prepStmt.setLong(1, Long.parseLong(s));
                prepStmt.execute();
            }
            conn.commit();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return false;
        }
    }

    public boolean updateDataInSubscribedChannelTable(String message, String channelId, String contentCreatorId) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    UPDATE notification."tblSubscribedChannel" SET "sctMessageText" = ?
                    WHERE "fk_sbcContentCreatorId" = ? AND "sbcChannelld" = ?;
                    """);
            prepStmt.setString(1, message);
            prepStmt.setString(2, contentCreatorId);
            prepStmt.setLong(3, Long.parseLong(channelId));
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return false;
        }
    }


    public boolean isIdInReceivedVideosTable(String videoId) {
        if (isPostgresDisabled) return true;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                                SELECT COUNT(*) FROM notification."tblReceivedVideos" WHERE "rcvId" = ?;
                    """);
            prepStmt.setString(1, videoId);
            boolean checkResult = checkResultSetForARow(prepStmt);
            conn.close();
            return checkResult;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return true;
        }
    }

    public void putIdInReceivedVideosTable(String videoId) {
        if (isPostgresDisabled) return;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO notification."tblReceivedVideos" VALUES (?);
                    """);

            prepStmt.setString(1, videoId);
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
        }
    }

    private void extractContentDataInContentCreatorTable(@NotNull ResultSet rs, @NotNull JSONArray results) throws SQLException {
        String contentCreatorId = rs.getString(1);
        String contentCreatorName = rs.getString(2);
        long channelId = rs.getLong(3);
        long guildId = rs.getLong(4);
        String messageText = rs.getString(5);

        JSONObject content = new JSONObject();
        content.put("contentCreatorId", contentCreatorId);
        content.put("contentCreatorName", contentCreatorName);
        content.put("messageChannelId", channelId);
        content.put("guildId", guildId);
        content.put("messageText", messageText);
        results.put(content);

    }

    public List<Long> getMessageDataBeforeTimestamp(LocalDateTime timestamp) {
        if (isPostgresDisabled) return new ArrayList<>();
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT "msgId" FROM guilddata."tblMessage" WHERE "msgTimestamp" < ?;
                    """);

            prepStmt.setTimestamp(1, Timestamp.valueOf(timestamp));

            ResultSet rs = prepStmt.executeQuery();
            List<Long> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rs.getLong(1));
            }
            conn.close();
            return results;

        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return new ArrayList<>();
        }
    }

    public JSONArray getMessageAttachmentsByMessageIDs(List<Long> messageIds) {
        if (isPostgresDisabled) return new JSONArray();
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM guilddata."tblMessageAttachment" WHERE "fk_msaMessageId" = ?;
                    """);

            JSONArray results = new JSONArray();

            for (Long msgId : messageIds) {
                prepStmt.setLong(1, msgId);
                ResultSet rs = prepStmt.executeQuery();
                JSONObject entry = new JSONObject();
                entry.put("msaId", rs.getLong(1));
                entry.put("fk_msaMessageId", rs.getLong(2));
                entry.put("msaAttachment", rs.getByte(3));
                results.put(entry);
            }
            conn.close();
            return results;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return new JSONArray();
        }
    }

    public void putMessageDataAndAttachmentsInTables(Message msg) {
        if (isPostgresDisabled) return;
        if (!msg.isFromGuild()) return;
        try {
            Connection conn = connection();

            long guildId = msg.getGuild().getIdLong();
            long userId = msg.getAuthor().getIdLong();
            long mbrId = getMemberIdFromMemberTable(conn, userId, guildId);

            PreparedStatement msgTablePrepStmt = conn.prepareStatement("""
                    INSERT INTO guilddata."tblMessage" ("msgId", "fk_msgMemberId", "msgContent", "msgTimestamp") VALUES
                    (?,?,?,?);
                    """);
            PreparedStatement msgAttachmentTablePrepStmt = conn.prepareStatement("""
                    INSERT INTO guilddata."tblMessageAttachment" ("fk_msaMessageId", "msaData") VALUES
                    (?,?);
                    """);

            msgTablePrepStmt.setLong(1, msg.getIdLong());
            msgTablePrepStmt.setLong(2, mbrId);
            msgTablePrepStmt.setString(3, msg.getContentRaw());
            msgTablePrepStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.from(msg.getTimeCreated())));
            msgTablePrepStmt.execute();

            msgAttachmentTablePrepStmt.setLong(1, msg.getIdLong());

            for (Message.Attachment msgAttachment : msg.getAttachments()) {
                File directory = FileHandler.getDirectoryInUserDirectory("attachments");
                FileHandler.createFile(directory, msgAttachment.getFileName());
                File attachmentFile = FileHandler.getFileInDirectory(directory, msgAttachment.getFileName());
                msgAttachment.getProxy().downloadToFile(attachmentFile).handleAsync((file, throwable) -> {
                    try {
                        msgAttachmentTablePrepStmt.setBytes(2, Files.readAllBytes(Path.of(file.getAbsolutePath())));
                        msgAttachmentTablePrepStmt.execute();
                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (file.delete()) {
                        return null;
                    }
                    throw new RuntimeException("Attachment File was not deleted!");
                });
            }
            conn.close();
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
        }
    }

    public void updateMessageDataInMessageTable(Message msg) {
        if (isPostgresDisabled) return;
        if (!msg.isFromGuild()) return;
        try {
            Connection conn = connection();

            if (!isMessageInMessageTable(conn, msg.getIdLong())) {
                putMessageDataAndAttachmentsInTables(msg);
                return;
            }

            PreparedStatement prepStmt = conn.prepareStatement("""
                    UPDATE guilddata."tblMessage" SET "msgContent" = ?
                    WHERE "msgId" = ?;
                    """);
            prepStmt.setString(1, msg.getContentRaw());
            prepStmt.setLong(2, msg.getIdLong());
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
        }
    }

    private boolean isMessageInMessageTable(@NotNull Connection conn, long msgId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblMessage" WHERE "msgId" = ?;
                """);
        prepStmt.setLong(1, msgId);
        return checkResultSetForARow(prepStmt);
    }

    public void deleteMessagesFromMessageTable(List<Long> msgIds) {
        if (isPostgresDisabled) return;
        try {
            Connection conn = connection();
            conn.setAutoCommit(false);
            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM guilddata."tblMessage" WHERE "msgId" = ?;
                    """);


            for (Long msgId : msgIds) {
                if (isMessageInMessageTable(conn, msgId)) {
                    prepStmt.setLong(1, msgId);
                    prepStmt.execute();
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
        }
    }

    public void deleteMessageAttachments(List<Long> msaIds) {
        if (isPostgresDisabled) return;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM guilddata."tblMessageAttachment" WHERE "msaId" = ?;
                    """);
            conn.setAutoCommit(false);

            for (Long msaId : msaIds) {
                prepStmt.setLong(1, msaId);
                prepStmt.execute();
            }
            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
        }
    }

    public void putDataInPeriodicCleanTable(GuildChannel channel, long days, User user) {
        if (isPostgresDisabled) return;
        try {
            Connection conn = connection();

            if (isChannelInPeriodicCleanTable(conn, channel.getIdLong())) return;

            PreparedStatement prepStmt = conn.prepareStatement("""
                    INSERT INTO guilddata."tblPeriodicCleanData" ("fk_pcdGuildId", "pcdChannelId", "pcdNextExecution", "pcdActive","fk_pcdMemberId","pcdDays")
                    VALUES (?,?,?,?,?,?);
                    """);

            Timestamp nextExecution = Timestamp.valueOf(LocalDateTime.now().plusDays(days));
            long guildId = channel.getGuild().getIdLong();
            long mbrId = getMemberIdFromMemberTable(conn, user.getIdLong(), guildId);

            prepStmt.setLong(1, guildId);
            prepStmt.setLong(2, channel.getIdLong());
            prepStmt.setTimestamp(3, nextExecution);
            prepStmt.setBoolean(4, true);
            prepStmt.setLong(5, mbrId);
            prepStmt.setLong(6, days);
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
        }
    }

    private boolean isChannelInPeriodicCleanTable(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblPeriodicCleanData" WHERE "pcdChannelId" = ?;
                """);
        prepStmt.setLong(1, channelId);

        return checkResultSetForARow(prepStmt);
    }

    public void editDataInPeriodicCleanTable(long channelId, boolean status, long days) {
        if (isPostgresDisabled) return;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    UPDATE guilddata."tblPeriodicCleanData" SET "pcdActive" = ?,"pcdNextExecution" = ?, "pcdDays" = ? WHERE "pcdChannelId" = ?;
                    """);
            prepStmt.setBoolean(1, status);
            prepStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now().plusDays(days)));
            prepStmt.setLong(3, days);
            prepStmt.setLong(4, channelId);
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
        }
    }

    public JSONObject getDataFromPeriodicCleanTable(long guildId) {
        if (isPostgresDisabled) return new JSONObject();
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM guilddata."tblPeriodicCleanData" WHERE "fk_pcdGuildId" = ? ORDER BY "pcdId";
                    """);
            prepStmt.setLong(1, guildId);
            ResultSet rs = prepStmt.executeQuery();
            JSONObject results = new JSONObject();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("channelId", rs.getLong(3));
                entry.put("nextExecution", rs.getTimestamp(5));
                entry.put("lastExecution", rs.getTimestamp(4) != null ? rs.getTimestamp(4) : "not Executed");
                entry.put("active", rs.getBoolean(6));
                entry.put("creator", getUserIdFromMemberTableByMemberId(conn, rs.getLong(7)));
                entry.put("days", rs.getLong(8));
                results.put(String.valueOf(rs.getLong(1)), entry);
            }
            conn.close();
            return results;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return new JSONObject();
        }
    }

    public boolean deleteDataFromPeriodicCleanTable(long pcdId) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM guilddata."tblPeriodicCleanData" WHERE "pcdId" = ?;
                    """);
            prepStmt.setLong(1, pcdId);
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return false;
        }
    }

    public long writeInLogchannelTable(long guildId, int logchannelType, long channelId) {
        if (isPostgresDisabled) return -1;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt;

            long dbChannelId = getChannelIdFromLogchannelTable(logchannelType, guildId, conn);


            if (dbChannelId == 0) { // Check if dbChannelId a discord-channel-id - is 0 create new row
                prepStmt = conn.prepareStatement("""
                        INSERT INTO guidconfigs."tblLogchannel" ("fk_lgcGuildId", "lgcType", "lgcChannelId") VALUES
                        (?,?,?);
                        """);
                prepStmt.setLong(1, guildId);
                prepStmt.setInt(2, logchannelType);
                prepStmt.setLong(3, channelId);

            } else if (dbChannelId > 0) { // Check if dbChannelId a discord-channel-id - is valid update
                prepStmt = conn.prepareStatement("""
                        UPDATE guidconfigs."tblLogchannel" SET "lgcChannelId" = ? WHERE "lgcType" = ? AND "fk_lgcGuildId" = ?;
                        """);
                prepStmt.setLong(1, channelId);
                prepStmt.setInt(2, logchannelType);
                prepStmt.setLong(3, guildId);
            } else return -5; // Exit with -5

            return getUpdatedRows(prepStmt, conn);
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return -3;
        }


    }

    private long getChannelIdFromLogchannelTable(int logchannelType, long guildId, @NotNull Connection conn) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "lgcChannelId" FROM guidconfigs."tblLogchannel" WHERE "fk_lgcGuildId" = ? AND "lgcType" = ?;
                """);
        prepStmt.setLong(1, guildId);
        prepStmt.setInt(2, logchannelType);
        return getPrivateReturnValue(prepStmt);
    }

    public long getChannelIdFromLogchannelTable(int logchannelType, long guildId) {
        if (isPostgresDisabled) return -1;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT "lgcChannelId" FROM guidconfigs."tblLogchannel" WHERE "fk_lgcGuildId" = ? AND "lgcType" = ?;
                    """);
            prepStmt.setLong(1, guildId);
            prepStmt.setInt(2, logchannelType);
            return getReturnValue(prepStmt, conn);
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return -3;
        }
    }

    public long writeInArchiveCategoryTable(long categoryId, long guildId) {
        if (isPostgresDisabled) return -1;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt;

            long dbCategoryId = getCategoryIdFromArchiveCategoryTable(guildId, conn);

            if (dbCategoryId == 0) {
                prepStmt = conn.prepareStatement("""
                        INSERT INTO guidconfigs."tblArchiveCategorie" ("avcChannelId", "fk_avcGuildId") VALUES (?,?);
                        """);
            } else if (dbCategoryId > 1) {
                prepStmt = conn.prepareStatement("""
                        UPDATE guidconfigs."tblArchiveCategorie" SET "avcChannelId" = ? WHERE "fk_avcGuildId" = ?;
                        """);
            } else return -5;

            prepStmt.setLong(1, categoryId);
            prepStmt.setLong(2, guildId);

            return getUpdatedRows(prepStmt, conn);
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return -3;
        }
    }

    private long getCategoryIdFromArchiveCategoryTable(long guildId, @NotNull Connection conn) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "avcChannelId" FROM guidconfigs."tblArchiveCategorie" WHERE "fk_avcGuildId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        return getPrivateReturnValue(prepStmt);
    }

    public long getCategoryIdFromArchiveCategoryTable(long guildId) {
        if (isPostgresDisabled) return -1;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT "avcChannelId" FROM guidconfigs."tblArchiveCategorie" WHERE "fk_avcGuildId" = ?;
                    """);
            prepStmt.setLong(1, guildId);
            return getReturnValue(prepStmt, conn);
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return -3;
        }
    }

    public long clearGuildArchiveFromArchiveCategoryTable(long guildId) {
        if (isPostgresDisabled) return -1;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM guidconfigs."tblArchiveCategorie" WHERE "fk_avcGuildId" = ?;
                    """);
            prepStmt.setLong(1, guildId);
            return getUpdatedRows(prepStmt, conn);
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return -3;
        }
    }

    public long writeInMediaOnlyChannelTable(long channelId, long guildId, boolean textPerms, boolean attachmentPerms, boolean filePerms, boolean linkPerms) {
        if (isPostgresDisabled) return -1;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt;

            if (isChannelIdInMediaOnlyChannelTable(channelId, conn)) {
                prepStmt = conn.prepareStatement("""
                        UPDATE guidconfigs."tblMediaOnlyChannel" SET
                        "mocPermissionForAttachments" = ?,
                        "mocPermissionForFiles" = ?,
                        "mocPermissionForLinks" = ?,
                        "mocPermissionForText" = ?
                        WHERE "mocChannelId" = ?;
                        """);
                prepStmt.setBoolean(1, attachmentPerms);
                prepStmt.setBoolean(2, filePerms);
                prepStmt.setBoolean(3, linkPerms);
                prepStmt.setBoolean(4, textPerms);
                prepStmt.setLong(5, channelId);
            } else {
                prepStmt = conn.prepareStatement("""
                        INSERT INTO guidconfigs."tblMediaOnlyChannel"
                        ("mocChannelId", "fk_mocGuildId", "mocPermissionForText", "mocPermissionForAttachments",
                         "mocPermissionForFiles", "mocPermissionForLinks")
                        VALUES (?,?,?,?,?,?);
                        """);
                prepStmt.setLong(1, channelId);
                prepStmt.setLong(2, guildId);
                prepStmt.setBoolean(3, textPerms);
                prepStmt.setBoolean(4, attachmentPerms);
                prepStmt.setBoolean(5, filePerms);
                prepStmt.setBoolean(6, linkPerms);
            }

            return getUpdatedRows(prepStmt, conn);
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return -3;
        }
    }

    public JSONObject getChannelIdsFromMediaOnlyChannelTable(long guildId) {
        if (isPostgresDisabled) new JSONObject();
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * FROM guidconfigs."tblMediaOnlyChannel" WHERE "fk_mocGuildId" = ?;
                    """);
            prepStmt.setLong(1, guildId);

            ResultSet rs = prepStmt.executeQuery();
            JSONObject results = new JSONObject();
            while (rs.next()) {
                JSONObject entry = new JSONObject();
                entry.put("permText", rs.getBoolean(3));
                entry.put("permAttachment", rs.getBoolean(4));
                entry.put("permFiles", rs.getBoolean(5));
                entry.put("permLinks", rs.getBoolean(6));
                results.put(String.valueOf(rs.getLong(1)), entry);
            }
            conn.close();
            return results;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return new JSONObject();
        }
    }

    public JSONObject getChannelPermissionsByChannelIdFromMediaOnlyChannelTable(long channelId) {
        if (isPostgresDisabled) return new JSONObject();
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT * from guidconfigs."tblMediaOnlyChannel" WHERE "mocChannelId" = ?;
                    """);
            prepStmt.setLong(1, channelId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return new JSONObject();
            JSONObject values = new JSONObject();
            values.put("permText", rs.getBoolean(3));
            values.put("permAttachment", rs.getBoolean(4));
            values.put("permFiles", rs.getBoolean(5));
            values.put("permLinks", rs.getBoolean(6));
            conn.close();
            return values;
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return new JSONObject();
        }
    }

    private boolean isChannelIdInMediaOnlyChannelTable(long channelId, @NotNull Connection conn) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblMediaOnlyChannel" WHERE "mocChannelId" = ?;
                """);
        prepStmt.setLong(1, channelId);
        return checkResultSetForARow(prepStmt);
    }

    public long removeChannelFromMediaOnlyChannelTable(long channelId) {
        if (isPostgresDisabled) return -1;
        try {
            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM guidconfigs."tblMediaOnlyChannel" WHERE "mocChannelId" = ?;
                    """);
            prepStmt.setLong(1, channelId);
            return getUpdatedRows(prepStmt, conn);
        } catch (SQLException e) {
            Util.handleSQLExceptions(e);
            return -3;
        }
    }

    private synchronized int getUpdatedRows(@NotNull PreparedStatement prepStmt, @NotNull Connection conn) throws SQLException {
        int updatedRows = prepStmt.executeUpdate();
        conn.close();
        if (updatedRows > 0) return 0;
        else return -4;
    }

    private synchronized long getReturnValue(@NotNull PreparedStatement preparedStatement, Connection conn) throws SQLException {
        long returnValue = 0;
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) returnValue = rs.getLong(1);
        conn.close();
        return returnValue;
    }

    private synchronized long getPrivateReturnValue(@NotNull PreparedStatement preparedStatement) throws SQLException {
        long returnValue = 0;
        ResultSet rs = preparedStatement.executeQuery();
        if (rs.next()) returnValue = rs.getLong(1);
        return returnValue;
    }


    private synchronized boolean checkResultSetForARow(@NotNull PreparedStatement prepStmt) throws SQLException {
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
        return rs.getLong(1) > 0;
    }

    private synchronized void checkResultSetForARow(@NotNull ResultSet rs) throws SQLException {
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
    }

    public boolean getIsPostgresEnabled() {
        return !isPostgresDisabled;
    }
}
