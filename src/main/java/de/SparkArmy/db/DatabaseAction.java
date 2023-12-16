package de.SparkArmy.db;

import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.utils.ErrorCodes;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.NotificationService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.SparkArmy.utils.Util.logger;

@SuppressWarnings("unused")
public class DatabaseAction {

    public DatabaseAction() {
    }

    /*
     * Return Codes:
     *  000: No update executed / Query return 0
     * -001: SQL Exception
     * -100: For Updates: Precondition failed
     * -200: For Queries: Precondition failed
     * -201: For Queries: Select Count() has no Row
     */

    private long putChannelIdInChannelTable(@NotNull Connection conn, long channelId, long guildId) throws SQLException {
        // Check if channel exist
        long isIdInChannelTable = isIdInChannelTable(conn, channelId);
        if (isIdInChannelTable > 0) return 0;
        if (isIdInChannelTable < 0) return isIdInChannelTable;

        // Put Guild in database
        long putIdInGuildTable = putGuildIdInGuildTable(conn, guildId);
        if (putIdInGuildTable < 0) return putIdInGuildTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guilddata."tblChannel" ("cnlChannelId","fk_cnlGuildId") VALUES (?,?);
                """);
        prepStmt.setLong(1, channelId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long putCategoryIdInCategoryTable(@NotNull Connection conn, long categoryId, long guildId) throws SQLException {
        // Check if channel exist
        long isIdInChannelTable = isIdInChannelTable(conn, categoryId);
        if (isIdInChannelTable > 0) return 0;
        if (isIdInChannelTable < 0) return isIdInChannelTable;

        // Put Guild in database
        long putIdInGuildTable = putGuildIdInGuildTable(conn, guildId);
        if (putIdInGuildTable < 0) return putIdInGuildTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guilddata."tblChannel" ("cnlChannelId","fk_cnlGuildId","cnlIsCategory") VALUES (?,?,true);
                """);
        prepStmt.setLong(1, categoryId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long isIdInChannelTable(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblChannel" WHERE "cnlChannelId" = ?;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long setChannelStateForArchiveCategory(@NotNull Connection conn, long categoryId, boolean state) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblChannel" SET "cnlIsArchiveCategory" = ? WHERE "cnlChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, categoryId);
        return prepStmt.executeUpdate();
    }

    private long getGuildArchiveCategoryCount(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblChannel" WHERE "fk_cnlGuildId" = ? AND "cnlIsArchiveCategory" = true;
                """);
        prepStmt.setLong(1, guildId);
        return getSelectCountValue(prepStmt);
    }

    private long getArchiveChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "cnlChannelId" FROM guilddata."tblChannel" WHERE "fk_cnlGuildId" = ? AND "cnlIsArchiveCategory" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("cnlChannelId");
    }

    private long setChannelStateForFeedbackChannel(@NotNull Connection conn, long channelId, boolean state) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblChannel" SET "cnlIsFeedbackChannel" = ? WHERE "cnlChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long getGuildFeedbackChannelCount(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblChannel" WHERE "fk_cnlGuildId" = ? AND "cnlIsFeedbackChannel" = true;
                """);
        prepStmt.setLong(1, guildId);
        return getSelectCountValue(prepStmt);
    }

    private long getFeedbackChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "cnlChannelId" FROM guilddata."tblChannel" WHERE "fk_cnlGuildId" = ? AND "cnlIsFeedbackChannel" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("cnlChannelId");
    }

    private long isCategory(@NotNull Connection conn, long categoryID, long guildId) throws SQLException {
        if (putCategoryIdInCategoryTable(conn, categoryID, guildId) >= 0) {
            PreparedStatement prepStmt = conn.prepareStatement("""
                    SELECT COUNT(*) FROM guilddata."tblChannel" WHERE "cnlChannelId" = ? AND "cnlIsCategory" = true;
                    """);
            prepStmt.setLong(1, categoryID);
            return getSelectCountValue(prepStmt);
        } else {
            return ErrorCodes.SQL_QUERY_PRECONDITION_FAILED.getId();
        }
    }

    private long putGuildIdInGuildTable(@NotNull Connection conn, long guildId) throws SQLException {
        long isGuildIdInGuildTable = isGuildIdInGuildTable(conn, guildId);
        if (isGuildIdInGuildTable > 0) return 0;
        if (isGuildIdInGuildTable < 0) return isGuildIdInGuildTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guilddata."tblGuild" ("gldId") VALUES (?);
                """);
        prepStmt.setLong(1, guildId);
        return prepStmt.executeUpdate();
    }

    private long removeGuildIdFromGuildTable(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guilddata."tblGuild" WHERE "gldId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        return prepStmt.executeUpdate();
    }

    private long isGuildIdInGuildTable(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblGuild" WHERE "gldId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        return getSelectCountValue(prepStmt);
    }

    private long putUserIdInUserTable(Connection conn, long userId) throws SQLException {
        long isUserIdInUserTable = isUserIdInUserTable(conn, userId);
        if (isUserIdInUserTable > 0) return 0;
        if (isUserIdInUserTable < 0) return isUserIdInUserTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guilddata."tblUser" ("usrId") VALUES (?);
                """);
        prepStmt.setLong(1, userId);
        return prepStmt.executeUpdate();
    }

    private long isUserIdInUserTable(@NotNull Connection conn, long userId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblUser" WHERE "usrId" = ?;
                """);
        prepStmt.setLong(1, userId);
        return getSelectCountValue(prepStmt);
    }

    @SuppressWarnings("DuplicatedCode")
    private long addMemberInMemberTable(Connection conn, long userId, long guildId) throws SQLException {
        long isMemberInMemberTable = isMemberInMemberTable(conn, userId, guildId);
        if (isMemberInMemberTable > 0) return 0;
        if (isMemberInMemberTable < 0) return isMemberInMemberTable;

        long putUserInUserTable = putUserIdInUserTable(conn, userId);
        if (putUserInUserTable < 0) return putUserInUserTable;

        long putGuildInGuildTable = putGuildIdInGuildTable(conn, guildId);
        if (putGuildInGuildTable < 0) return putGuildInGuildTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guilddata."tblMember" ("fk_mbrUserId","fk_mbrGuildId") VALUES (?,?);
                """);
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long isMemberInMemberTable(@NotNull Connection conn, long userId, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblMember" WHERE "fk_mbrUserId" = ? AND "fk_mbrGuildId" = ?;
                """);
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        return getSelectCountValue(prepStmt);
    }

    private long getDatabaseMemberId(Connection conn, long userId, long guildId) throws SQLException {
        long addMemberInMemberTable = addMemberInMemberTable(conn, userId, guildId);
        if (addMemberInMemberTable < 0) return addMemberInMemberTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "mbrId" FROM guilddata."tblMember" WHERE "fk_mbrUserId" = ? AND "fk_mbrGuildId" = ?;
                """);
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return ErrorCodes.SQL_QUERY_SELECT_HAS_NO_ROW.getId();
        return rs.getLong("mbrId");
    }

    private long getUserIdByDatabaseMemberId(@NotNull Connection conn, long databaseId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_mbrUserId" FROM guilddata."tblMember" WHERE "mbrId" = ?;
                """);
        prepStmt.setLong(1, databaseId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("fk_mbrUserId");
    }

    private long setOnServerStateFromMember(Connection conn, long userId, long guildId, boolean state) throws SQLException {
        long databaseMemberId = getDatabaseMemberId(conn, userId, guildId);
        if (databaseMemberId < 0) return databaseMemberId;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblMember" SET "mbrOnServer" = ? WHERE "mbrId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, databaseMemberId);
        return prepStmt.executeUpdate();
    }

    @SuppressWarnings("SameParameterValue")
    private long setIsModeratorStateForMember(Connection conn, long userId, long guildId, boolean state) throws SQLException {
        long databaseMemberId = getDatabaseMemberId(conn, userId, guildId);
        if (databaseMemberId < 0) return databaseMemberId;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblMember" SET "mbrIsModerator" = ? WHERE "mbrId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, databaseMemberId);
        return prepStmt.executeUpdate();
    }

    @SuppressWarnings("SameParameterValue")
    private long setIsModeratorStateForMember(@NotNull Connection conn, long databaseMemberId, boolean state) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblMember" SET "mbrIsModerator" = ? WHERE "mbrId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, databaseMemberId);
        return prepStmt.executeUpdate();
    }

    private long putMessageContentInDatabase(Connection conn, @NotNull Message message) throws SQLException {
        if (!message.isFromGuild()) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();

        long messageId = message.getIdLong();
        long isMessageInDatabase = isMessageInDatabase(conn, messageId);
        if (isMessageInDatabase > 0) return 0;
        if (isMessageInDatabase < 0) return isMessageInDatabase;

        long guildId = message.getGuildIdLong();
        long userId = message.getAuthor().getIdLong();
        long channelId = message.getChannelIdLong();

        long databaseMemberId = getDatabaseMemberId(conn, userId, guildId);
        if (databaseMemberId < 0) return databaseMemberId;

        long putChannelIdInChannelTable = putChannelIdInChannelTable(conn, channelId, guildId);
        if (putChannelIdInChannelTable < 0) return putChannelIdInChannelTable;

        String messageContent = message.getContentRaw();
        Timestamp messageTimestamp = Timestamp.from(Instant.from(message.getTimeCreated()));

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guilddata."tblMessage" ("msgId", "fk_msgMemberId", "msgContent", "msgTimestamp", "fk_msgChannelId") VALUES (?,?,?,?,?);
                """);
        prepStmt.setLong(1, messageId);
        prepStmt.setLong(2, databaseMemberId);
        prepStmt.setString(3, messageContent);
        prepStmt.setTimestamp(4, messageTimestamp);
        prepStmt.setLong(5, channelId);

        return prepStmt.executeUpdate();
    }

    private void updateMessageContentInDatabase(Connection conn, @NotNull Message message) throws SQLException {
        long messageId = message.getIdLong();
        long isMessageInDatabase = isMessageInDatabase(conn, messageId);
        if (isMessageInDatabase == 0) {
            putMessageContentInDatabase(conn, message);
            putMessageAttachmentDataInDatabase(conn, message);
            return;
        }
        if (isMessageInDatabase < 0) return;

        String content = message.getContentRaw();

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblMessage" SET "msgContent" = ? WHERE "msgId" = ?;
                """);
        prepStmt.setString(1, content);
        prepStmt.setLong(2, messageId);
        prepStmt.executeUpdate();
    }

    private long isMessageInDatabase(@NotNull Connection conn, long messageId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblMessage" WHERE "msgId" = ?;
                """);
        prepStmt.setLong(1, messageId);
        return getSelectCountValue(prepStmt);
    }

    private void removeMessageFromDatabase(@NotNull Connection conn, long messageId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guilddata."tblMessage" WHERE "msgId" = ?;
                """);
        prepStmt.setLong(1, messageId);
        prepStmt.executeUpdate();
    }

    private void putMessageAttachmentDataInDatabase(Connection conn, @NotNull Message message) throws SQLException {
        long putMessageContentInDatabase = putMessageContentInDatabase(conn, message);
        if (putMessageContentInDatabase < 0) return;

        List<Message.Attachment> attachments = message.getAttachments();
        if (attachments.isEmpty()) return;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guilddata."tblMessageAttachment" ("fk_msaMessageId", "msaData") VALUES (?,?);
                """);

        long messageId = message.getIdLong();

        for (Message.Attachment attachment : attachments) {
            File directory = FileHandler.getDirectoryInUserDirectory("attachments");
            File attachmentFile = FileHandler.getFileInDirectory(directory, attachment.getFileName());
            attachment.getProxy().downloadToFile(attachmentFile).handleAsync((x, e) -> {
                try {
                    byte[] fileData = Files.readAllBytes(Path.of(x.getAbsolutePath()));
                    prepStmt.setLong(1, messageId);
                    prepStmt.setBytes(2, fileData);
                    prepStmt.executeUpdate();
                } catch (IOException | SQLException ex) {
                    throw new RuntimeException(ex);
                }

                if (x.delete()) {
                    return null;
                } else throw new RuntimeException("Attachment File was not deleted");
            });
        }
    }

    private void removeMessageAttachmentDataFromDatabase(@NotNull Connection conn, long messageId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guilddata."tblMessageAttachment" WHERE "fk_msaMessageId" = ?;
                """);
        prepStmt.setLong(1, messageId);
        prepStmt.executeUpdate();
    }

    private long putRoleInRoleTable(Connection conn, long roleId, long guildId) throws SQLException {
        long isRoleIdInRoleTable = isRoleIdInRoleTable(conn, roleId);
        if (isRoleIdInRoleTable > 0) return 0;
        if (isRoleIdInRoleTable < 0) return isRoleIdInRoleTable;
        long putGuildIdInGuildTable = putGuildIdInGuildTable(conn, guildId);
        if (putGuildIdInGuildTable < 0) return putGuildIdInGuildTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guilddata."tblRole" ("rolRoleId", "fk_rolGuildId") VALUES (?,?);
                """);
        prepStmt.setLong(1, roleId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long isRoleIdInRoleTable(@NotNull Connection conn, long roleId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblRole" WHERE "rolRoleId" = ?;
                """);
        prepStmt.setLong(1, roleId);
        return getSelectCountValue(prepStmt);
    }

    private long setIsMuteRoleState(Connection conn, long roleId, long guildId, boolean state) throws SQLException {
        long putRoleInRoleTable = putRoleInRoleTable(conn, roleId, guildId);
        if (putRoleInRoleTable < 0) return putRoleInRoleTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblRole" SET "rolIsMute" = ? WHERE "rolRoleId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, roleId);
        return prepStmt.executeUpdate();
    }

    private long setIsWarnRoleState(Connection conn, long roleId, long guildId, boolean state) throws SQLException {
        long putRoleInRoleTable = putRoleInRoleTable(conn, roleId, guildId);
        if (putRoleInRoleTable < 0) return putRoleInRoleTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblRole" SET "rolIsWarn" = ? WHERE "rolRoleId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, roleId);
        return prepStmt.executeUpdate();
    }

    private long setIsModmailPingRoleState(Connection conn, long roleId, long guildId, boolean state) throws SQLException {
        long putRoleInRoleTable = putRoleInRoleTable(conn, roleId, guildId);
        if (putRoleInRoleTable < 0) return putRoleInRoleTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblRole" SET "rolIsModmailPingRole" = ? WHERE "rolRoleId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, roleId);
        return prepStmt.executeUpdate();
    }

    private long setIsModRoleState(Connection conn, long roleId, long guildId, boolean state) throws SQLException {
        long putRoleInRoleTable = putRoleInRoleTable(conn, roleId, guildId);
        if (putRoleInRoleTable < 0) return putRoleInRoleTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guilddata."tblRole" SET "rolIsModRole" = ? WHERE "rolRoleId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, roleId);
        return prepStmt.executeUpdate();
    }

    private long getMuteRoleStateFromRole(Connection conn, long roleId, long guildId) throws SQLException {
        long putRoleInRoleTable = putRoleInRoleTable(conn, roleId, guildId);
        if (putRoleInRoleTable < 0) return putRoleInRoleTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                 SELECT "rolIsMute" FROM guilddata."tblRole" WHERE "rolRoleId" = ?;
                """);
        prepStmt.setLong(1, roleId);
        return getRoleState(prepStmt);
    }

    private long getWarnRoleStateFromRole(Connection conn, long roleId, long guildId) throws SQLException {
        long putRoleInRoleTable = putRoleInRoleTable(conn, roleId, guildId);
        if (putRoleInRoleTable < 0) return putRoleInRoleTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "rolIsWarn" FROM guilddata."tblRole" WHERE "rolRoleId" = ?;
                """);
        prepStmt.setLong(1, roleId);
        return getRoleState(prepStmt);
    }

    private long getModmailPingRoleStateFromRole(Connection conn, long roleId, long guildId) throws SQLException {
        long putRoleInRoleTable = putRoleInRoleTable(conn, roleId, guildId);
        if (putRoleInRoleTable < 0) return putRoleInRoleTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                 SELECT "rolIsModmailPingRole" FROM guilddata."tblRole" WHERE "rolRoleId" = ?;
                """);
        prepStmt.setLong(1, roleId);
        return getRoleState(prepStmt);
    }

    private long getModRoleStateFromRole(Connection conn, long roleId, long guildId) throws SQLException {
        long putRoleInRoleTable = putRoleInRoleTable(conn, roleId, guildId);
        if (putRoleInRoleTable < 0) return putRoleInRoleTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "rolIsModRole" FROM guilddata."tblRole" WHERE "rolRoleId" = ?;
                """);
        prepStmt.setLong(1, roleId);
        return getRoleState(prepStmt);
    }

    private long getRoleState(@NotNull PreparedStatement prepStmt) throws SQLException {
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return ErrorCodes.SQL_QUERY_SELECT_HAS_NO_ROW.getId();
        return rs.getBoolean(1) ? 1 : 0;
    }

    private long isGuildWarnRoleSet(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblRole" WHERE "fk_rolGuildId" = ? AND "rolIsWarn" = true;
                """);
        prepStmt.setLong(1, guildId);
        return getSelectCountValue(prepStmt);
    }

    private long isGuildMuteRoleSet(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guilddata."tblRole" WHERE "fk_rolGuildId" = ? AND "rolIsMute" = true;
                """);
        prepStmt.setLong(1, guildId);
        return getSelectCountValue(prepStmt);
    }

    private long getGuildWarnRole(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "rolRoleId" FROM guilddata."tblRole" WHERE "fk_rolGuildId" = ? AND "rolIsWarn" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("rolRoleId");
    }

    private long getGuildMuteRole(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "rolRoleId" FROM guilddata."tblRole" WHERE "fk_rolGuildId" = ? AND "rolIsMute" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("rolRoleId");
    }

    private long addContentCreatorInDatabase(Connection conn, String contentCreatorName, NotificationService service, String contentCreatorServiceId) throws SQLException {
        long isContentCreatorInDatabase = isContentCreatorInDatabase(conn, contentCreatorServiceId);
        if (isContentCreatorInDatabase > 0) return 0;
        if (isContentCreatorInDatabase < 0) return isContentCreatorInDatabase;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO notification."tblContentCreator" ("ctcName", "ctcServiceName", "ctcServiceId") VALUES (?,?,?);
                """);
        prepStmt.setString(1, contentCreatorName);
        prepStmt.setString(2, service.getServiceName());
        prepStmt.setString(3, contentCreatorServiceId);
        return prepStmt.executeUpdate();
    }

    private long isContentCreatorInDatabase(@NotNull Connection conn, String contentCreatorServiceId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM notification."tblContentCreator" WHERE "ctcServiceId" = ?;
                """);
        prepStmt.setString(1, contentCreatorServiceId);
        return getSelectCountValue(prepStmt);
    }

    private long isContentCreatorInDatabase(@NotNull Connection conn, long contentCreatorId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM notification."tblContentCreator" WHERE "ctcId" = ?;
                """);
        prepStmt.setLong(1, contentCreatorId);
        return getSelectCountValue(prepStmt);
    }

    private long getContentCreatorDatabaseIdByContentCreatorServiceId(@NotNull Connection conn, String contentCreatorServiceId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "ctcId" FROM notification."tblContentCreator" WHERE "ctcServiceId" = ?;
                """);
        prepStmt.setString(1, contentCreatorServiceId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("ctcId");
    }

    private long putVideoInReceivedVideoTable(Connection conn, String videoId) throws SQLException {
        long isVideoInVideoTable = isVideoInVideoTable(conn, videoId);
        if (isVideoInVideoTable > 0) return 0;
        if (isVideoInVideoTable < 0) return isVideoInVideoTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO notification."tblReceivedVideos" ("rcvId") VALUES (?);
                """);
        prepStmt.setString(1, videoId);
        return prepStmt.executeUpdate();
    }

    private long isVideoInVideoTable(@NotNull Connection conn, String videoId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM notification."tblReceivedVideos" WHERE "rcvId" = ?;
                """);
        prepStmt.setString(1, videoId);
        return getSelectCountValue(prepStmt);
    }

    private long putDataInSubscribedChannelTable(Connection conn, long channelId, long guildId, long contentCreatorId, String message) throws SQLException {
        long isDataInSubscribedChannelTable = isDataInSubscribedChannelTable(conn, channelId, contentCreatorId);
        if (isDataInSubscribedChannelTable > 0) return 0;
        if (isDataInSubscribedChannelTable < 0) return isDataInSubscribedChannelTable;

        long putChannelIdInChannelTable = putChannelIdInChannelTable(conn, channelId, guildId);
        if (putChannelIdInChannelTable < 0) return putChannelIdInChannelTable;
        long putGuildIdInChannelTable = putGuildIdInGuildTable(conn, guildId);
        if (putGuildIdInChannelTable < 0) return putGuildIdInChannelTable;
        long isContentCreatorInDatabase = isContentCreatorInDatabase(conn, contentCreatorId);
        if (isContentCreatorInDatabase < 0) return isContentCreatorInDatabase;
        else if (isContentCreatorInDatabase == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO "notification"."tblSubscribedChannel" ("fk_sbcChannelId", "fk_sbcGuildId", "fk_sbcContentCreatorId", "sctMessageText")  VALUES (?,?,?,?);
                """);
        prepStmt.setLong(1, channelId);
        prepStmt.setLong(2, guildId);
        prepStmt.setLong(3, contentCreatorId);
        prepStmt.setString(4, message);
        return prepStmt.executeUpdate();
    }

    private long isDataInSubscribedChannelTable(@NotNull Connection conn, long channelId, long contentCreatorId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM notification."tblSubscribedChannel" WHERE "fk_sbcChannelId" = ? AND "fk_sbcContentCreatorId" = ?;
                """);
        prepStmt.setLong(1, channelId);
        prepStmt.setLong(2, contentCreatorId);
        return getSelectCountValue(prepStmt);
    }

    private long updateDataInSubscribedChannelTable(@NotNull Connection conn, String messageString, long channelId, long databaseContentCreatorId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE notification."tblSubscribedChannel" SET "sctMessageText" = ? WHERE "fk_sbcChannelId" = ? AND "fk_sbcContentCreatorId" = ?;
                """);
        prepStmt.setString(1, messageString);
        prepStmt.setLong(2, channelId);
        prepStmt.setLong(3, databaseContentCreatorId);
        return prepStmt.executeUpdate();
    }

    private long removeDataFromSubscribedChannelTable(@NotNull Connection conn, long channelId, long databaseContentCreatorId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM notification."tblSubscribedChannel" WHERE "fk_sbcChannelId" = ? AND "fk_sbcContentCreatorId" = ?;
                """);
        prepStmt.setLong(1, channelId);
        prepStmt.setLong(2, databaseContentCreatorId);
        return prepStmt.executeUpdate();
    }

    private long putDataInLogChannelTable(
            Connection conn, long channelId, String webhookUrl, boolean messageLog, boolean memberLog,
            boolean commandLog, boolean leaveLog, boolean serverLog, boolean voiceLog,
            boolean modLog) throws SQLException {
        long isIdInChannelTable = isIdInChannelTable(conn, channelId);
        if (isIdInChannelTable < 0) return isIdInChannelTable;
        if (isIdInChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();

        long isDataInLogChannelTable = isDataInLogChannelTable(conn, channelId);
        if (isDataInLogChannelTable > 0) return 0;
        if (isDataInLogChannelTable < 0) return isDataInLogChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guidconfigs."tblLogChannel" ("fk_lgcChannelId", "lgcWebhookUrl", "lgcIsMessageLog", "lgcIsMemberLog",
                "lgcIsCommandLog", "lgcIsLeaveLog", "lgcIsServerLog", "lgcIsVoiceLog","lgcIsModLog") VALUES (?,?,?,?,?,?,?,?,?);
                """);

        if (!messageLog && !memberLog && !commandLog && !leaveLog && !serverLog && !voiceLog)
            return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();

        prepStmt.setLong(1, channelId);
        prepStmt.setString(2, webhookUrl);
        prepStmt.setBoolean(3, messageLog);
        prepStmt.setBoolean(4, memberLog);
        prepStmt.setBoolean(5, commandLog);
        prepStmt.setBoolean(6, leaveLog);
        prepStmt.setBoolean(7, serverLog);
        prepStmt.setBoolean(8, voiceLog);
        prepStmt.setBoolean(9, modLog);
        return prepStmt.executeUpdate();
    }

    private long getMessageLogChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_lgcChannelId" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsMessageLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("fk_lgcChannelId");
    }

    private long getMemberLogChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_lgcChannelId" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsMemberLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("fk_lgcChannelId");
    }

    private long getCommandLogChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_lgcChannelId" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsCommandLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("fk_lgcChannelId");
    }

    private long getLeaveLogChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_lgcChannelId" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsLeaveLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("fk_lgcChannelId");
    }

    private long getServerLogChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_lgcChannelId" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsServerLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("fk_lgcChannelId");
    }

    private long getVoiceLogChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_lgcChannelId" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsVoiceLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("fk_lgcChannelId");
    }

    private long getModLogChannelId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_lgcChannelId" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsModLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return 0;
        return rs.getLong("fk_lgcChannelId");
    }

    private @Nullable String getMessageLogWebhookUrl(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "lgcWebhookUrl" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsMessageLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return null;
        return rs.getString("lgcWebhookUrl");
    }

    private @Nullable String getMemberLogWebhookUrl(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "lgcWebhookUrl" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsMemberLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return null;
        return rs.getString("lgcWebhookUrl");
    }

    private @Nullable String getCommandLogWebhookUrl(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "lgcWebhookUrl" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsCommandLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return null;
        return rs.getString("lgcWebhookUrl");
    }

    private @Nullable String getLeaveLogWebhookUrl(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "lgcWebhookUrl" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsLeaveLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return null;
        return rs.getString("lgcWebhookUrl");
    }

    private @Nullable String getServerLogWebhookUrl(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "lgcWebhookUrl" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsServerLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return null;
        return rs.getString("lgcWebhookUrl");
    }

    private @Nullable String getVoiceLogWebhookUrl(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "lgcWebhookUrl" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsVoiceLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return null;
        return rs.getString("lgcWebhookUrl");
    }

    private @Nullable String getModLogChannelWebhookUrl(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "lgcWebhookUrl" FROM guidconfigs."tblLogChannel"
                INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblLogChannel"."fk_lgcChannelId"
                WHERE tC."fk_cnlGuildId" = ? AND "lgcIsModLog" = true;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return null;
        return rs.getString("lgcWebhookUrl");
    }

    private long isDataInLogChannelTable(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long setIsMessageLogState(Connection conn, long channelId, boolean state) throws SQLException {
        long isDataInLogChannelTable = isDataInLogChannelTable(conn, channelId);
        if (isDataInLogChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInLogChannelTable < 0) return isDataInLogChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblLogChannel" SET "lgcIsMessageLog" = ? WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setIsMemberLogState(Connection conn, long channelId, boolean state) throws SQLException {
        long isDataInLogChannelTable = isDataInLogChannelTable(conn, channelId);
        if (isDataInLogChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInLogChannelTable < 0) return isDataInLogChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblLogChannel" SET "lgcIsMemberLog" = ? WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setIsCommandLogState(Connection conn, long channelId, boolean state) throws SQLException {
        long isDataInLogChannelTable = isDataInLogChannelTable(conn, channelId);
        if (isDataInLogChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInLogChannelTable < 0) return isDataInLogChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblLogChannel" SET "lgcIsCommandLog" = ? WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setIsLeaveLogState(Connection conn, long channelId, boolean state) throws SQLException {
        long isDataInLogChannelTable = isDataInLogChannelTable(conn, channelId);
        if (isDataInLogChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInLogChannelTable < 0) return isDataInLogChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblLogChannel" SET "lgcIsLeaveLog" = ? WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setIsServerLogState(Connection conn, long channelId, boolean state) throws SQLException {
        long isDataInLogChannelTable = isDataInLogChannelTable(conn, channelId);
        if (isDataInLogChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInLogChannelTable < 0) return isDataInLogChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblLogChannel" SET "lgcIsServerLog" = ? WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setIsVoiceLogState(Connection conn, long channelId, boolean state) throws SQLException {
        long isDataInLogChannelTable = isDataInLogChannelTable(conn, channelId);
        if (isDataInLogChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInLogChannelTable < 0) return isDataInLogChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblLogChannel" SET "lgcIsVoiceLog" = ? WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setIsModLogState(Connection conn, long channelId, boolean state) throws SQLException {
        long isDataInLogChannelTable = isDataInLogChannelTable(conn, channelId);
        if (isDataInLogChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInLogChannelTable < 0) return isDataInLogChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblLogChannel" SET "lgcIsModLog" = ? WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long isOneLogStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?
                AND "lgcIsMessageLog" = false AND "lgcIsMemberLog" = false AND "lgcIsCommandLog" = false
                AND "lgcIsLeaveLog" = false AND "lgcIsServerLog" = false AND "lgcIsVoiceLog" = false;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isMessageLogStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?
                AND "lgcIsMessageLog" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isMemberLogStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?
                AND "lgcIsMemberLog" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isCommandLogStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?
                AND "lgcIsCommandLog" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isLeaveLogStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?
                AND "lgcIsLeaveLog" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isServerLogStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?
                AND "lgcIsServerLog" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isVoiceLogStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?
                AND "lgcIsVoiceLog" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isModLogStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?
                AND "lgcIsModLog" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long removeLogChannelFromLogChannelTable(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guidconfigs."tblLogChannel" WHERE "fk_lgcChannelId" = ?;
                """);
        prepStmt.setLong(1, channelId);
        return prepStmt.executeUpdate();
    }

    private long putDataInMediaOnlyChannelTable(Connection conn, long channelId) throws SQLException {
        long isChannelInMediaOnlyChannelTable = isChannelInMediaOnlyChannelTable(conn, channelId);
        if (isChannelInMediaOnlyChannelTable > 0) return 0;
        if (isChannelInMediaOnlyChannelTable < 0) return isChannelInMediaOnlyChannelTable;
        long isIdInChannelTable = isIdInChannelTable(conn, channelId);
        if (isIdInChannelTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isIdInChannelTable < 0) return isIdInChannelTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guidconfigs."tblMediaOnlyChannel" ("fk_mocChannelId") VALUES (?);
                """);
        prepStmt.setLong(1, channelId);
        return prepStmt.executeUpdate();
    }

    private long isChannelInMediaOnlyChannelTable(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblMediaOnlyChannel" WHERE "fk_mocChannelId" = ?;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long removeChannelFromMediaOnlyChannelTable(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guidconfigs."tblMediaOnlyChannel" WHERE "fk_mocChannelId" = ?;
                """);
        prepStmt.setLong(1, channelId);
        return prepStmt.executeUpdate();
    }

    private long setTextAllowedState(Connection conn, long channelId, boolean state) throws SQLException {
        long putDataInMediaOnlyChannelTable = putDataInMediaOnlyChannelTable(conn, channelId);
        if (putDataInMediaOnlyChannelTable < 0) return putDataInMediaOnlyChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblMediaOnlyChannel" SET "mocIsTextAllowed" = ? WHERE "fk_mocChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setLinkAllowedState(Connection conn, long channelId, boolean state) throws SQLException {
        long putDataInMediaOnlyChannelTable = putDataInMediaOnlyChannelTable(conn, channelId);
        if (putDataInMediaOnlyChannelTable < 0) return putDataInMediaOnlyChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblMediaOnlyChannel" SET "mocIsLinkAllowed" = ? WHERE "fk_mocChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setFileState(Connection conn, long channelId, boolean state) throws SQLException {
        long putDataInMediaOnlyChannelTable = putDataInMediaOnlyChannelTable(conn, channelId);
        if (putDataInMediaOnlyChannelTable < 0) return putDataInMediaOnlyChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblMediaOnlyChannel" SET "mocIsFileAllowed" = ? WHERE "fk_mocChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long setAttachmentAllowedState(Connection conn, long channelId, boolean state) throws SQLException {
        long putDataInMediaOnlyChannelTable = putDataInMediaOnlyChannelTable(conn, channelId);
        if (putDataInMediaOnlyChannelTable < 0) return putDataInMediaOnlyChannelTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblMediaOnlyChannel" SET "mocIsAttachmentAllowed" = ? WHERE "fk_mocChannelId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, channelId);
        return prepStmt.executeUpdate();
    }

    private long isTextAllowedStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblMediaOnlyChannel" WHERE "fk_mocChannelId" = ? AND "mocIsTextAllowed" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isLinkAllowedStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblMediaOnlyChannel" WHERE "fk_mocChannelId" = ? AND "mocIsLinkAllowed" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isFileAllowedStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblMediaOnlyChannel" WHERE "fk_mocChannelId" = ? AND "mocIsFileAllowed" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    private long isAttachmentAllowedStateSet(@NotNull Connection conn, long channelId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblMediaOnlyChannel" WHERE "fk_mocChannelId" = ? AND "mocIsAttachmentAllowed" = true;
                """);
        prepStmt.setLong(1, channelId);
        return getSelectCountValue(prepStmt);
    }

    @SuppressWarnings("DuplicatedCode")
    private long putDataInModmailBlacklistTable(Connection conn, long userId, long guildId) throws SQLException {
        long isDataInModmialBlacklistTable = isDataInModmailBlacklistTable(conn, userId, guildId);
        if (isDataInModmialBlacklistTable > 0) return 0;
        if (isDataInModmialBlacklistTable < 0) return isDataInModmialBlacklistTable;
        long putUserIdInUserTable = putUserIdInUserTable(conn, userId);
        if (putUserIdInUserTable < 0) return putUserIdInUserTable;
        long putGuildIdInGuildTable = putGuildIdInGuildTable(conn, guildId);
        if (putGuildIdInGuildTable < 0) return putGuildIdInGuildTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guidconfigs."tblModmailBlacklist" ("fk_mblUserId", "fk_mblGuildId") VALUES (?,?);
                """);
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long isDataInModmailBlacklistTable(@NotNull Connection conn, long userId, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblModmailBlacklist" WHERE "fk_mblUserId" = ? AND "fk_mblGuildId" = ?;
                """);
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        return getSelectCountValue(prepStmt);
    }

    private long removeDataFromModmailBlacklistTable(@NotNull Connection conn, long userId, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guidconfigs."tblModmailBlacklist" WHERE "fk_mblUserId" = ? AND "fk_mblGuildId" = ?;
                """);
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long putDataInModmailChannelTable(Connection conn, long guildId, long modmailCategoryId) throws SQLException {
        long isDataInModmailChannelTable = isDataInModmailChannelTable(conn, guildId);
        if (isDataInModmailChannelTable > 0) return 0;
        if (isDataInModmailChannelTable < 0) return isDataInModmailChannelTable;
        long putGuildINGuildTable = putGuildIdInGuildTable(conn, guildId);
        if (putGuildINGuildTable < 0) return putGuildINGuildTable;
        long putChanelIdInChannelTable = putCategoryIdInCategoryTable(conn, modmailCategoryId, guildId);
        if (putChanelIdInChannelTable < 0) return putChanelIdInChannelTable;
        long isCategory = isCategory(conn, modmailCategoryId, guildId);
        if (isCategory == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isCategory < 0) return isCategory;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guidconfigs."tblModmailChannel" ("fk_mmcGuildId","fk_mmcModmailCategory") VALUES (?,?);
                """);
        prepStmt.setLong(1, guildId);
        prepStmt.setLong(2, modmailCategoryId);
        return prepStmt.executeUpdate();
    }

    private long isDataInModmailChannelTable(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblModmailChannel" WHERE "fk_mmcGuildId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        return getSelectCountValue(prepStmt);
    }

    private long updateCategoryInModmailChannelTable(Connection conn, long guildId, long categoryId) throws SQLException {
        long putChanelIdInChannelTable = putCategoryIdInCategoryTable(conn, categoryId, guildId);
        if (putChanelIdInChannelTable < 0) return putChanelIdInChannelTable;
        long isCategory = isCategory(conn, categoryId, guildId);
        if (isCategory == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isCategory < 0) return isCategory;
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblModmailChannel" SET "fk_mmcModmailCategory" = ? WHERE "fk_mmcGuildId" = ?
                """);
        prepStmt.setLong(1, categoryId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long removeDataFromModmailChannelTable(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guidconfigs."tblModmailChannel" WHERE "fk_mmcGuildId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        return prepStmt.executeUpdate();
    }

    @SuppressWarnings("DuplicatedCode")
    private long setArchiveChannelInModmailChannelTable(@NotNull Connection conn, long guildId, Long archiveChannelId) throws SQLException {
        long putChannelIdInChannelTable = putChannelIdInChannelTable(conn, archiveChannelId, guildId);
        if (putChannelIdInChannelTable < 0) return putChannelIdInChannelTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblModmailChannel" SET "fk_mmcModmailArchive" = ? WHERE "fk_mmcGuildId" = ?;
                """);
        prepStmt.setLong(1, archiveChannelId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    @SuppressWarnings("DuplicatedCode")
    private long setLogChannelInModmailChannelTable(@NotNull Connection conn, long guildId, Long logChannelId) throws SQLException {
        long putChannelIdInChannelTable = putChannelIdInChannelTable(conn, logChannelId, guildId);
        if (putChannelIdInChannelTable < 0) return putChannelIdInChannelTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblModmailChannel" SET "fk_mmcModmailLog" = ? WHERE "fk_mmcGuildId" = ?;
                """);
        prepStmt.setLong(1, logChannelId);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long getModmailCategoryId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_mmcModmailCategory" FROM guidconfigs."tblModmailChannel" WHERE "fk_mmcGuildId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return ErrorCodes.SQL_QUERY_SELECT_HAS_NO_ROW.getId();
        return rs.getLong("fk_mmcModmailCategory");
    }

    private Long getModmailArchiveId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_mmcModmailArchive" FROM guidconfigs."tblModmailChannel" WHERE "fk_mmcGuildId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return (long) ErrorCodes.SQL_QUERY_SELECT_HAS_NO_ROW.getId();
        return rs.getLong("fk_mmcModmailArchive");
    }

    private Long getModmailLogId(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "fk_mmcModmailLog" FROM guidconfigs."tblModmailChannel" WHERE "fk_mmcGuildId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return (long) ErrorCodes.SQL_QUERY_SELECT_HAS_NO_ROW.getId();
        return rs.getLong("fk_mmcModmailLog");
    }

    private long putDataInRegexTable(Connection conn, long guildId, String regexString, String regexName) throws SQLException {
        long existRegexNameInGuild = existRegexNameInGuild(conn, guildId, regexName);
        if (existRegexNameInGuild > 0) return 0;
        if (existRegexNameInGuild < 0) return existRegexNameInGuild;
        long putGuildIdInGuildTable = putGuildIdInGuildTable(conn, guildId);
        if (putGuildIdInGuildTable != 0) return putGuildIdInGuildTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guidconfigs."tblRegex" ("fk_rgxGuildId", "rgxRegex", "rgxName") VALUES (?,?,?);
                """);
        prepStmt.setLong(1, guildId);
        prepStmt.setString(2, regexString);
        prepStmt.setString(3, regexName);
        return prepStmt.executeUpdate();
    }

    private long existRegexNameInGuild(@NotNull Connection conn, long guildId, String regexName) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblRegex" WHERE "fk_rgxGuildId" = ? AND "rgxName" = ?;
                """);
        prepStmt.setLong(1, guildId);
        prepStmt.setString(2, regexName);
        return getSelectCountValue(prepStmt);
    }

    private long updateRegexName(Connection conn, long guildId, String regexName, long databaseId) throws SQLException {
        long existRegexNameInGuild = existRegexNameInGuild(conn, guildId, regexName);
        if (existRegexNameInGuild == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (existRegexNameInGuild < 0) return existRegexNameInGuild;
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblRegex" SET "rgxName" = ? WHERE "rgxId" = ?;
                """);
        prepStmt.setString(1, regexName);
        prepStmt.setLong(2, databaseId);
        return prepStmt.executeUpdate();
    }

    private long updateRegex(Connection conn, long guildId, String regexName, String regex) throws SQLException {
        long putDataInRegexTable = putDataInRegexTable(conn, guildId, regex, regexName);
        if (putDataInRegexTable != 0) return putDataInRegexTable;

        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblRegex" SET "rgxRegex" = ? WHERE "fk_rgxGuildId" = ? AND "rgxName" = ?;
                """);
        prepStmt.setString(1, regex);
        prepStmt.setLong(2, guildId);
        prepStmt.setString(3, regexName);
        return prepStmt.executeUpdate();
    }

    private long removeRegexFromRegexTable(@NotNull Connection conn, long databaseId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guidconfigs."tblRegex" WHERE "rgxId" = ?;
                """);
        prepStmt.setLong(1, databaseId);
        return prepStmt.executeUpdate();
    }

    private long putDataInTextBlacklistTable(Connection conn, long guildId, String phrase) throws SQLException {
        long isDataInTextBlacklistTable = isDataInTextBlacklistTable(conn, guildId, phrase);
        if (isDataInTextBlacklistTable > 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInTextBlacklistTable < 0) return isDataInTextBlacklistTable;
        long putGuildIdInGuildTable = putGuildIdInGuildTable(conn, guildId);
        if (putGuildIdInGuildTable < 0) return putGuildIdInGuildTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO guidconfigs."tblTextBlacklist" ("txbString", "fk_txbGuildId") VALUES (?,?);
                """);
        prepStmt.setString(1, phrase);
        prepStmt.setLong(2, guildId);
        return prepStmt.executeUpdate();
    }

    private long isDataInTextBlacklistTable(@NotNull Connection conn, long guildId, String phrase) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblTextBlacklist" WHERE "fk_txbGuildId" = ? AND "txbString" = ?;
                """);
        prepStmt.setLong(1, guildId);
        prepStmt.setString(2, phrase);
        return getSelectCountValue(prepStmt);
    }

    private long isDataInTextBlacklistTable(@NotNull Connection conn, long databaseId, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM guidconfigs."tblTextBlacklist" WHERE "fk_txbGuildId" = ? AND "txbId" = ?;
                """);
        prepStmt.setLong(1, guildId);
        prepStmt.setLong(2, databaseId);
        return getSelectCountValue(prepStmt);
    }

    private @Nullable String getPhraseFromTextBacklistTable(@NotNull Connection conn, long databaseId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT "txbString" FROM guidconfigs."tblTextBlacklist" WHERE "txbId" = ?;
                """);
        prepStmt.setLong(1, databaseId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return null;
        return rs.getString("txbString");
    }

    private long updateDataInTextBlacklistTable(Connection conn, long databaseId, long guildId, String phrase) throws SQLException {
        long isDataInTextBlacklistTable = isDataInTextBlacklistTable(conn, databaseId, guildId);
        if (isDataInTextBlacklistTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInTextBlacklistTable < 0) return isDataInTextBlacklistTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE guidconfigs."tblTextBlacklist" SET "txbString" = ? WHERE "txbId" = ?;
                """);
        prepStmt.setString(1, phrase);
        prepStmt.setLong(2, databaseId);
        return prepStmt.executeUpdate();
    }

    private long removeDataFromTextBlacklistTable(@NotNull Connection conn, long databaseId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM guidconfigs."tblTextBlacklist" WHERE "txbId" = ?;
                """);
        prepStmt.setLong(1, databaseId);
        return prepStmt.executeUpdate();
    }

    @SuppressWarnings("DuplicatedCode")
    private long putDataInNoteTable(Connection conn, long guildId, long targetUserId, long moderatorUserId, String content, LocalDateTime timestamp) throws SQLException {
        long setIsModeratorStateForMember = setIsModeratorStateForMember(conn, moderatorUserId, guildId, true);
        if (setIsModeratorStateForMember < 0) return setIsModeratorStateForMember;
        long getDatabaseIdFromTargetMemberId = getDatabaseMemberId(conn, targetUserId, guildId);
        if (getDatabaseIdFromTargetMemberId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (getDatabaseIdFromTargetMemberId < 0) return getDatabaseIdFromTargetMemberId;
        long getDatabaseIdFromModeratorId = getDatabaseMemberId(conn, moderatorUserId, guildId);
        if (getDatabaseIdFromModeratorId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (getDatabaseIdFromModeratorId < 0) return getDatabaseIdFromModeratorId;
        long isDataInNoteTable = isDataInNoteTable(conn, getDatabaseIdFromTargetMemberId, timestamp);
        if (isDataInNoteTable > 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInNoteTable < 0) return isDataInNoteTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO botfunctiondata."tblNote" ("fk_notMemberId", "notContent", "fk_notModeratorId", "notTimestamp") VALUES (?,?,?,?);
                """);
        prepStmt.setLong(1, getDatabaseIdFromTargetMemberId);
        prepStmt.setString(2, content);
        prepStmt.setLong(3, getDatabaseIdFromModeratorId);
        prepStmt.setTimestamp(4, Timestamp.valueOf(timestamp));
        return prepStmt.executeUpdate();
    }

    private long isDataInNoteTable(@NotNull Connection conn, long databaseMemberId, LocalDateTime timestamp) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM botfunctiondata."tblNote" WHERE "fk_notMemberId" = ? AND "notTimestamp" = ?;
                """);
        prepStmt.setLong(1, databaseMemberId);
        prepStmt.setTimestamp(2, Timestamp.valueOf(timestamp));
        return getSelectCountValue(prepStmt);
    }

    @SuppressWarnings("DuplicatedCode")
    private long updateDataInNoteTable(Connection conn, long guildId, long targetUserId, LocalDateTime timestamp, String content) throws SQLException {
        long getDatabaseIdFromTargetMemberId = getDatabaseMemberId(conn, targetUserId, guildId);
        if (getDatabaseIdFromTargetMemberId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (getDatabaseIdFromTargetMemberId < 0) return getDatabaseIdFromTargetMemberId;
        long isDataInNoteTable = isDataInNoteTable(conn, getDatabaseIdFromTargetMemberId, timestamp);
        if (isDataInNoteTable == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (isDataInNoteTable < 0) return isDataInNoteTable;
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE botfunctiondata."tblNote" SET "notContent" = ? WHERE "fk_notMemberId" = ? AND "notTimestamp" = ?;
                """);
        prepStmt.setString(1, content);
        prepStmt.setLong(2, getDatabaseIdFromTargetMemberId);
        prepStmt.setTimestamp(3, Timestamp.valueOf(timestamp));
        return prepStmt.executeUpdate();
    }

    private long removeDataFromNoteTable(Connection conn, long guildId, long targetUserId, LocalDateTime timestamp) throws SQLException {
        long getDatabaseIdFromTargetMemberId = getDatabaseMemberId(conn, targetUserId, guildId);
        if (getDatabaseIdFromTargetMemberId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (getDatabaseIdFromTargetMemberId < 0) return getDatabaseIdFromTargetMemberId;
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM botfunctiondata."tblNote" WHERE "fk_notMemberId" = ? AND "notTimestamp" = ?;
                """);

        Timestamp timestamp1 = Timestamp.valueOf(timestamp);
        prepStmt.setLong(1, getDatabaseIdFromTargetMemberId);
        prepStmt.setTimestamp(2, timestamp1);
        return prepStmt.executeUpdate();
    }

    @SuppressWarnings("DuplicatedCode")
    private long putDataInPeriodicCleanTable(Connection conn, long channelId, long guildId, long userId, long interval) throws SQLException {
        long putChannelIdInChannelTable = putChannelIdInChannelTable(conn, channelId, guildId);
        if (putChannelIdInChannelTable < 0) return putChannelIdInChannelTable;
        long setIsModeratorStateForMember = setIsModeratorStateForMember(conn, userId, guildId, true);
        if (setIsModeratorStateForMember < 0) return setIsModeratorStateForMember;
        long databaseMemberId = getDatabaseMemberId(conn, userId, guildId);
        if (databaseMemberId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (databaseMemberId < 0) return databaseMemberId;
        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO botfunctiondata."tblPeriodicCleanData" ("fk_pcdChannelId","pcdNextExecution","fk_pcdMemberId","pcdDays") VALUES (?,?,?,?);
                """);
        LocalDateTime nextExecution = LocalDateTime.now().plusDays(interval);
        prepStmt.setLong(1, channelId);
        prepStmt.setTimestamp(2, Timestamp.valueOf(nextExecution));
        prepStmt.setLong(3, databaseMemberId);
        prepStmt.setLong(4, interval);
        return prepStmt.executeUpdate();
    }

    private long setActiveStateInPeriodicCleanTable(@NotNull Connection conn, long databaseId, boolean state) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE botfunctiondata."tblPeriodicCleanData" SET "pcdActive" = ? WHERE "pcdId" = ?;
                """);
        prepStmt.setBoolean(1, state);
        prepStmt.setLong(2, databaseId);
        return prepStmt.executeUpdate();
    }

    private long updateIntervalInPeriodicCleanTable(@NotNull Connection conn, long databaseId, long interval) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE botfunctiondata."tblPeriodicCleanData" SET "pcdNextExecution" = ?,"pcdDays" = ? WHERE "pcdId" = ?;
                """);
        LocalDateTime timestamp = LocalDateTime.now().plusDays(interval);
        prepStmt.setTimestamp(1, Timestamp.valueOf(timestamp));
        prepStmt.setLong(2, interval);
        prepStmt.setLong(3, databaseId);
        return prepStmt.executeUpdate();
    }

    private long setLastExecutionTimestampInPeriodicCleanTable(@NotNull Connection conn, long databaseId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE botfunctiondata."tblPeriodicCleanData" SET "pcdLastExecution" = now() WHERE "pcdId" = ?;
                """);
        prepStmt.setLong(1, databaseId);
        return prepStmt.executeUpdate();
    }

    private long removeDataFromPeriodicCleanTable(@NotNull Connection conn, long databaseId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                DELETE FROM botfunctiondata."tblPeriodicCleanData" WHERE "pcdId" = ?;
                """);
        prepStmt.setLong(1, databaseId);
        return prepStmt.executeUpdate();
    }

    @SuppressWarnings("DuplicatedCode")
    private long putDataInPunishmentTable(Connection conn, long guildId, long targetUserId, long moderatorUserId, int punishmentType, String reason) throws SQLException {
        long getTargetUserDatabaseMemberId = getDatabaseMemberId(conn, targetUserId, guildId);
        if (getTargetUserDatabaseMemberId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (getTargetUserDatabaseMemberId < 0) return getTargetUserDatabaseMemberId;
        long getModeratorDatabaseMemberId = getDatabaseMemberId(conn, moderatorUserId, guildId);
        if (getModeratorDatabaseMemberId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
        if (getModeratorDatabaseMemberId < 0) return getModeratorDatabaseMemberId;
        long setModeratorState = setIsModeratorStateForMember(conn, getModeratorDatabaseMemberId, true);
        if (setModeratorState < 0) return setModeratorState;

        PreparedStatement prepStmt = conn.prepareStatement("""
                INSERT INTO botfunctiondata."tblPunishment" ("fk_psmMemberId", "fk_psmModeratorId", "fk_psmPunishmentTypeId", "psmReason", "psmTimestamp") VALUES (?,?,?,?,now());
                """);
        prepStmt.setLong(1, getTargetUserDatabaseMemberId);
        prepStmt.setLong(2, getModeratorDatabaseMemberId);
        prepStmt.setInt(3, punishmentType);
        prepStmt.setString(4, reason);
        return prepStmt.executeUpdate();
    }

    private long setPunishmentWithdrawn(@NotNull Connection conn, long punishmentId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE botfunctiondata."tblPunishment" SET "psmIsWithdrawn" = true WHERE "psmId" = ?;
                """);
        prepStmt.setLong(1, punishmentId);
        return prepStmt.executeUpdate();
    }

    private long updatePunishmentReason(@NotNull Connection conn, long punishmentId, String reason) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("""
                UPDATE botfunctiondata."tblPunishment" SET "psmReason" = ? WHERE "psmId" = ?;
                """);
        prepStmt.setString(1, reason);
        prepStmt.setLong(2, punishmentId);
        return prepStmt.executeUpdate();
    }

    private synchronized long getSelectCountValue(@NotNull PreparedStatement prepStmt) throws SQLException {
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return ErrorCodes.SQL_QUERY_SELECT_COUNT_NO_ROW.getId();
        return rs.getLong(1);
    }

    private synchronized void handleSQLException(SQLException e) {
        logger.error("Error in DatabasAction", e);
    }

    // Public Methods to interact with database
    public long writeInLogchannelTable(long guildId, LogChannelType logChannelType, long channelId, String webhookUrl) {
        try {
            Connection connection = DatabaseSource.connection();
            long putChannelIdInChannelTable = putChannelIdInChannelTable(connection, channelId, guildId);
            if (putChannelIdInChannelTable < 0) return putChannelIdInChannelTable;
            long putDataInLogChannelTable = putDataInLogChannelTable(connection, channelId, webhookUrl,
                    logChannelType.equals(LogChannelType.MESSAGE),
                    logChannelType.equals(LogChannelType.MEMBER),
                    logChannelType.equals(LogChannelType.COMMAND),
                    logChannelType.equals(LogChannelType.LEAVE),
                    logChannelType.equals(LogChannelType.SERVER),
                    logChannelType.equals(LogChannelType.VOICE),
                    logChannelType.equals(LogChannelType.MOD));
            if (putDataInLogChannelTable != 0) return putDataInLogChannelTable;
            else {
                long returnCode;
                switch (logChannelType) {
                    case MESSAGE -> {
                        long isMessageLogStateSet = isMessageLogStateSet(connection, channelId);
                        if (isMessageLogStateSet > 0) {
                            returnCode = setIsMessageLogState(connection, channelId, false);
                        } else if (isMessageLogStateSet == 0) {
                            returnCode = setIsMessageLogState(connection, channelId, true);
                        } else {
                            connection.close();
                            return isMessageLogStateSet;
                        }
                    }
                    case MEMBER -> {
                        long isMemberLogStateSet = isMemberLogStateSet(connection, channelId);
                        if (isMemberLogStateSet > 0) {
                            returnCode = setIsMemberLogState(connection, channelId, false);
                        } else if (isMemberLogStateSet == 0) {
                            returnCode = setIsMemberLogState(connection, channelId, true);
                        } else {
                            connection.close();
                            return isMemberLogStateSet;
                        }
                    }
                    case COMMAND -> {
                        long isCommandLogStateSet = isCommandLogStateSet(connection, channelId);
                        if (isCommandLogStateSet > 0) {
                            returnCode = setIsCommandLogState(connection, channelId, false);
                        } else if (isCommandLogStateSet == 0) {
                            returnCode = setIsCommandLogState(connection, channelId, true);
                        } else {
                            connection.close();
                            return isCommandLogStateSet;
                        }
                    }
                    case LEAVE -> {
                        long isLeaveLogStateSet = isLeaveLogStateSet(connection, channelId);
                        if (isLeaveLogStateSet > 0) {
                            returnCode = setIsLeaveLogState(connection, channelId, false);
                        } else if (isLeaveLogStateSet == 0) {
                            returnCode = setIsLeaveLogState(connection, channelId, true);
                        } else {
                            connection.close();
                            return isLeaveLogStateSet;
                        }
                    }
                    case MOD -> {
                        long isModLogStateSet = isModLogStateSet(connection, channelId);
                        if (isModLogStateSet > 0) {
                            returnCode = setIsModLogState(connection, channelId, false);
                        } else if (isModLogStateSet == 0) {
                            returnCode = setIsModLogState(connection, channelId, true);
                        } else {
                            connection.close();
                            return isModLogStateSet;
                        }
                    }
                    case SERVER -> {
                        long isServerLogState = isServerLogStateSet(connection, channelId);
                        if (isServerLogState > 0) {
                            returnCode = setIsServerLogState(connection, channelId, false);
                        } else if (isServerLogState == 0) {
                            returnCode = setIsServerLogState(connection, channelId, true);
                        } else {
                            connection.close();
                            return isServerLogState;
                        }
                    }
                    case VOICE -> {
                        long isVoiceLogState = isVoiceLogStateSet(connection, channelId);
                        if (isVoiceLogState > 0) {
                            returnCode = setIsVoiceLogState(connection, channelId, false);
                        } else if (isVoiceLogState == 0) {
                            returnCode = setIsVoiceLogState(connection, channelId, true);
                        } else {
                            connection.close();
                            return isVoiceLogState;
                        }
                    }
                    default -> {
                        connection.close();
                        return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
                    }
                }
                long isOneLogStateSet = isOneLogStateSet(connection, channelId);
                if (isOneLogStateSet == 0) {
                    connection.close();
                    return returnCode;
                } else if (isOneLogStateSet > 0) {
                    returnCode = removeLogChannelFromLogChannelTable(connection, channelId);
                    connection.close();
                    return returnCode;
                } else {
                    connection.close();
                    return isOneLogStateSet;
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removeLogChannelStateFromLogChannelTable(@NotNull LogChannelType type, long channelId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = 0;
            switch (type) {
                case MESSAGE -> value = setIsMessageLogState(connection, channelId, false);
                case MEMBER -> value = setIsMemberLogState(connection, channelId, false);
                case COMMAND -> value = setIsCommandLogState(connection, channelId, false);
                case LEAVE -> value = setIsLeaveLogState(connection, channelId, false);
                case MOD -> value = setIsModLogState(connection, channelId, false);
                case SERVER -> value = setIsServerLogState(connection, channelId, false);
                case VOICE -> value = setIsVoiceLogState(connection, channelId, false);
            }
            long isOneLogStateSet = isOneLogStateSet(connection, channelId);
            if (isOneLogStateSet == 0) {
                connection.close();
                return value;
            } else if (isOneLogStateSet > 0) {
                value = removeLogChannelFromLogChannelTable(connection, channelId);
                connection.close();
                return value;
            } else {
                connection.close();
                return isOneLogStateSet;
            }
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long getLogChannelIdByLogChannelTypeAndGuildId(@NotNull LogChannelType logChannelType, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long channelId;
            switch (logChannelType) {
                case MESSAGE -> channelId = getMessageLogChannelId(connection, guildId);
                case MEMBER -> channelId = getMemberLogChannelId(connection, guildId);
                case COMMAND -> channelId = getCommandLogChannelId(connection, guildId);
                case LEAVE -> channelId = getLeaveLogChannelId(connection, guildId);
                case MOD -> channelId = getModLogChannelId(connection, guildId);
                case SERVER -> channelId = getServerLogChannelId(connection, guildId);
                case VOICE -> channelId = getVoiceLogChannelId(connection, guildId);
                default -> channelId = ErrorCodes.SQL_QUERY_PRECONDITION_FAILED.getId();
            }
            connection.close();
            return channelId;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public String getLogChannelWebhookUrlByLogChannelTypeAndGuildId(@NotNull LogChannelType logChannelType, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            String url;
            switch (logChannelType) {
                case MESSAGE -> url = getMessageLogWebhookUrl(connection, guildId);
                case MEMBER -> url = getMemberLogWebhookUrl(connection, guildId);
                case COMMAND -> url = getCommandLogWebhookUrl(connection, guildId);
                case LEAVE -> url = getLeaveLogWebhookUrl(connection, guildId);
                case MOD -> url = getModLogChannelWebhookUrl(connection, guildId);
                case SERVER -> url = getServerLogWebhookUrl(connection, guildId);
                case VOICE -> url = getVoiceLogWebhookUrl(connection, guildId);
                default -> url = null;
            }
            connection.close();
            return url;
        } catch (SQLException e) {
            handleSQLException(e);
            return String.valueOf(ErrorCodes.SQL_EXCEPTION.getId());
        }
    }

    public List<String> getWebhookUrlsFromGuild() {
        List<String> urls = new ArrayList<>();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "lgcWebhookUrl" FROM guidconfigs."tblLogChannel";
                    """);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                urls.add(resultSet.getString("lgcWebhookUrl"));
            }
            connection.close();
            return urls;
        } catch (SQLException e) {
            handleSQLException(e);
            return urls;
        }
    }

    public long setArchiveCategory(Long categoryId, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long putCategoryIdInCategoryTable = putCategoryIdInCategoryTable(connection, categoryId, guildId);
            if (putCategoryIdInCategoryTable < 0) return putCategoryIdInCategoryTable;
            long archiveCategoryCount = getGuildArchiveCategoryCount(connection, guildId);
            long returnValue;
            if (archiveCategoryCount == 0) {
                returnValue = setChannelStateForArchiveCategory(connection, categoryId, true);
            } else if (archiveCategoryCount > 0) {
                long oldCategoryId = getArchiveChannelId(connection, guildId);
                if (oldCategoryId <= 0) return oldCategoryId;
                long oldChannelExecution = setChannelStateForArchiveCategory(connection, oldCategoryId, false);
                if (oldChannelExecution < 0) return oldChannelExecution;
                returnValue = setChannelStateForArchiveCategory(connection, categoryId, true);
            } else {
                return archiveCategoryCount;
            }
            connection.close();
            return returnValue;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long getArchiveCategoryByGuildId(long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long id = getArchiveChannelId(connection, guildId);
            connection.close();
            return id;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removeArchiveCategoryByGuildId(long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long category = getArchiveChannelId(connection, guildId);
            long value = setChannelStateForArchiveCategory(connection, category, false);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long addMediaOnlyChannel(long channelId, long guildId, boolean text, boolean attachment, boolean file, boolean link) {
        try {
            Connection connection = DatabaseSource.connection();
            long putChannelIdInChannelTable = putChannelIdInChannelTable(connection, channelId, guildId);
            if (putChannelIdInChannelTable < 0) return putChannelIdInChannelTable;
            long textStateSet = setTextAllowedState(connection, channelId, text);
            long attachmentSet = setAttachmentAllowedState(connection, channelId, attachment);
            long fileSet = setFileState(connection, channelId, file);
            long linkSet = setLinkAllowedState(connection, channelId, link);
            connection.close();
            if (textStateSet < 0) return textStateSet;
            if (attachmentSet < 0) return attachmentSet;
            if (fileSet < 0) return fileSet;
            if (linkSet < 0) return linkSet;
            return 1;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public JSONObject getMediaOnlyChannelDataByGuildId(long guildId) {
        JSONObject results = new JSONObject();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "fk_mocChannelId","mocIsTextAllowed","mocIsLinkAllowed","mocIsFileAllowed","mocIsAttachmentAllowed"
                    FROM guidconfigs."tblMediaOnlyChannel"
                    INNER JOIN guilddata."tblChannel" tC ON tc."cnlChannelId" = "tblMediaOnlyChannel"."fk_mocChannelId"
                    WHERE tC."fk_cnlGuildId" = ?;
                    """);
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                JSONObject entry = new JSONObject();
                entry.put("permText", resultSet.getBoolean("mocIsTextAllowed"));
                entry.put("permLinks", resultSet.getBoolean("mocIsLinkAllowed"));
                entry.put("permFiles", resultSet.getBoolean("mocIsFileAllowed"));
                entry.put("permAttachment", resultSet.getBoolean("mocIsAttachmentAllowed"));
                results.put(String.valueOf(resultSet.getLong("fk_mocChannelId")), entry);
            }
            connection.close();
            return results;
        } catch (SQLException e) {
            handleSQLException(e);
            return results;
        }
    }

    public JSONObject getMediaOnlyChannelDataByChannelId(long channelId) {
        JSONObject entry = new JSONObject();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "mocIsTextAllowed", "mocIsLinkAllowed", "mocIsFileAllowed", "mocIsAttachmentAllowed" FROM guidconfigs."tblMediaOnlyChannel" WHERE "fk_mocChannelId" = ?;
                    """);
            preparedStatement.setLong(1, channelId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                connection.close();
                return entry;
            }
            entry.put("permText", resultSet.getBoolean("mocIsTextAllowed"));
            entry.put("permLinks", resultSet.getBoolean("mocIsLinkAllowed"));
            entry.put("permFiles", resultSet.getBoolean("mocIsFileAllowed"));
            entry.put("permAttachment", resultSet.getBoolean("mocIsAttachmentAllowed"));
            connection.close();
            logger.info("returned");
            return entry;
        } catch (SQLException e) {
            handleSQLException(e);
            return entry;
        }
    }

    public long removeMediaOnlyChannel(long channelId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = removeChannelFromMediaOnlyChannelTable(connection, channelId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long addModerationRole(long roleId, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long setIsModRoleState = setIsModRoleState(connection, roleId, guildId, true);
            connection.close();
            return setIsModRoleState;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removeModerationRole(long roleId, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long setIsModRoleState = setIsModRoleState(connection, roleId, guildId, false);
            connection.close();
            return setIsModRoleState;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public List<Long> getModerationRolesByGuildId(long guildId) {
        List<Long> roleIds = new ArrayList<>();
        try {
            Connection connection = DatabaseSource.connection();
            logger.info(String.valueOf(LocalDateTime.now()));
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "rolRoleId" FROM guilddata."tblRole" WHERE "fk_rolGuildId" = ? AND "rolIsModRole" = true;
                    """);
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                roleIds.add(resultSet.getLong("rolRoleId"));
            }
            connection.close();
            logger.info(String.valueOf(LocalDateTime.now()));
            return roleIds;
        } catch (SQLException e) {
            handleSQLException(e);
            return roleIds;
        }
    }

    public List<Long> getModerationRolesByGuildId() {
        List<Long> roleIds = new ArrayList<>();
        try {
            Connection connection = DatabaseSource.connection();
            logger.info(String.valueOf(LocalDateTime.now()));
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "rolRoleId" FROM guilddata."tblRole" WHERE "rolIsModRole" = true;
                    """);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                roleIds.add(resultSet.getLong("rolRoleId"));
            }
            connection.close();
            logger.info(String.valueOf(LocalDateTime.now()));
            return roleIds;
        } catch (SQLException e) {
            handleSQLException(e);
            return roleIds;
        }
    }

    public long setWarnRole(long guildId, long roleId) {
        try {
            Connection connection = DatabaseSource.connection();
            long isGuildWarnRoleSet = isGuildWarnRoleSet(connection, guildId);
            long value;
            if (isGuildWarnRoleSet == 0) value = setIsWarnRoleState(connection, roleId, guildId, true);
            else if (isGuildWarnRoleSet > 0) {
                long oldRole = getGuildWarnRole(connection, guildId);
                if (oldRole > 0) setIsWarnRoleState(connection, oldRole, guildId, false);
                value = setIsWarnRoleState(connection, roleId, guildId, true);
            } else {
                return isGuildWarnRoleSet;
            }
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long getWarnRoleByGuildId(long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long roleId = getGuildWarnRole(connection, guildId);
            connection.close();
            return roleId;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long setMuteRole(long guildId, long roleId) {
        try {
            Connection connection = DatabaseSource.connection();
            long isGuildMuteRoleSet = isGuildMuteRoleSet(connection, guildId);
            long value;
            if (isGuildMuteRoleSet == 0) value = setIsMuteRoleState(connection, roleId, guildId, true);
            else if (isGuildMuteRoleSet > 0) {
                long oldRole = getGuildMuteRole(connection, guildId);
                if (oldRole > 0) setIsMuteRoleState(connection, oldRole, guildId, false);
                value = setIsMuteRoleState(connection, roleId, guildId, true);
            } else {
                return isGuildMuteRoleSet;
            }
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long getMuteRoleByGuildId(long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long roleId = getGuildMuteRole(connection, guildId);
            connection.close();
            return roleId;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long addPhraseToBlacklistTable(String phrase, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = putDataInTextBlacklistTable(connection, guildId, phrase);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public JSONObject getPhrasesFromBlacklistTableByGuildId(long guildId) {
        JSONObject results = new JSONObject();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "txbId","txbString" FROM guidconfigs."tblTextBlacklist" WHERE "fk_txbGuildId" = ?;
                    """);
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.put(String.valueOf(resultSet.getLong("txbId")), resultSet.getString("txbString"));
            }
            connection.close();
            return results;
        } catch (SQLException e) {
            handleSQLException(e);
            return results;
        }
    }

    public String getPhraseByDatabaseId(long databaseId) {
        try {
            Connection connection = DatabaseSource.connection();
            String phrase = getPhraseFromTextBacklistTable(connection, databaseId);
            connection.close();
            return phrase;
        } catch (SQLException e) {
            handleSQLException(e);
            return null;
        }
    }

    public long updatePhraseInBlacklistTable(long databaseId, String phrase, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = updateDataInTextBlacklistTable(connection, databaseId, guildId, phrase);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removePhraseFromBlacklistTable(long databaseId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = removeDataFromTextBlacklistTable(connection, databaseId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long writeInRegexTable(long guildId, String regexString, String regexName, long databaseId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value;
            if (databaseId == 0) {
                value = putDataInRegexTable(connection, guildId, regexString, regexName);
            } else {
                value = updateRegex(connection, guildId, regexName, regexString);
            }
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public JSONObject getRegexEntriesByGuildId(long guildId) {
        JSONObject results = new JSONObject();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "rgxId", "rgxRegex", "rgxName" FROM guidconfigs."tblRegex" WHERE "fk_rgxGuildId" = ?;
                    """);
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                JSONObject entry = new JSONObject();
                entry.put("name", resultSet.getString("rgxName"));
                entry.put("regex", resultSet.getString("rgxRegex"));
                entry.put("id", resultSet.getLong("rgxId"));
                results.put(String.valueOf(resultSet.getLong("rgxId")), entry);
            }
            connection.close();
            return results;
        } catch (SQLException e) {
            handleSQLException(e);
            return results;
        }
    }

    public JSONObject getRegexEntryByDatabaseId(long databaseId) {
        JSONObject entry = new JSONObject();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "rgxId", "rgxRegex", "rgxName" FROM guidconfigs."tblRegex" WHERE "rgxId" = ?;
                    """);
            preparedStatement.setLong(1, databaseId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) return entry;
            entry.put("name", resultSet.getString("rgxName"));
            entry.put("regex", resultSet.getString("rgxRegex"));
            entry.put("id", resultSet.getLong("rgxId"));
            connection.close();
            return entry;
        } catch (SQLException e) {
            handleSQLException(e);
            return entry;
        }
    }

    public long removeRegexEntry(long databaseId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = removeRegexFromRegexTable(connection, databaseId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long setGuildFeedbackChannel(long channelId, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long putChannelIdInChannelTable = putChannelIdInChannelTable(connection, channelId, guildId);
            if (putChannelIdInChannelTable < 0) return putChannelIdInChannelTable;
            long feedbackChannelCount = getGuildFeedbackChannelCount(connection, guildId);
            long returnValue;
            if (feedbackChannelCount == 0) returnValue = setChannelStateForFeedbackChannel(connection, channelId, true);
            else if (feedbackChannelCount > 0) {
                long feedbackChannelId = getFeedbackChannelId(connection, guildId);
                if (feedbackChannelId > 0) setChannelStateForFeedbackChannel(connection, channelId, false);
                returnValue = setChannelStateForFeedbackChannel(connection, channelId, true);
            } else {
                return feedbackChannelCount;
            }
            connection.close();
            return returnValue;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removeGuildFeedbackChannel(long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long channelId = getFeedbackChannelId(connection, guildId);
            long value;
            if (channelId > 0) value = setChannelStateForFeedbackChannel(connection, channelId, false);
            else value = channelId;
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long getGuildFeedbackChannelByGuildId(long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = getFeedbackChannelId(connection, guildId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public List<Long> getUserIdsFromModmailBlacklistTableByGuildId(long guildId) {
        List<Long> userIds = new ArrayList<>();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "fk_mblUserId" FROM guidconfigs."tblModmailBlacklist" WHERE "fk_mblGuildId" = ?;
                    """);
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                userIds.add(resultSet.getLong("fk_mblUserId"));
            }
            connection.close();
            return userIds;
        } catch (SQLException e) {
            handleSQLException(e);
            return userIds;
        }
    }

    public long isUserOnModmailBlacklist(long guildId, long userId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = isDataInModmailBlacklistTable(connection, userId, guildId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removeUserFromModmailBlacklist(long guildId, long userId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = removeDataFromModmailBlacklistTable(connection, userId, guildId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long addUserToModmailBlacklist(long guildId, long userId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = putDataInModmailBlacklistTable(connection, userId, guildId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long writeCategegoryInModmailChannelTable(long categoryId, long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long isDataInModmailChannelTable = isDataInModmailChannelTable(connection, guildId);
            long value;
            if (isDataInModmailChannelTable == 0) {
                value = putDataInModmailChannelTable(connection, guildId, categoryId);
            } else if (isDataInModmailChannelTable > 0) {
                value = updateCategoryInModmailChannelTable(connection, guildId, categoryId);
            } else {
                return isDataInModmailChannelTable;
            }
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removeDataFromModmailChannelTable(long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = removeDataFromModmailChannelTable(connection, guildId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long setModmailArchiveChannel(long guildId, Long channelId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = setArchiveChannelInModmailChannelTable(connection, guildId, channelId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long setModmailLogChannel(long guildId, Long channelId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = setLogChannelInModmailChannelTable(connection, guildId, channelId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long isRoleModmailPingRole(long guildId, long roleId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = getModmailPingRoleStateFromRole(connection, roleId, guildId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long addModmailPingRole(long guildId, long roleId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = setIsModmailPingRoleState(connection, roleId, guildId, true);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removeModmailPingRole(long guildId, long roleId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = setIsModmailPingRoleState(connection, roleId, guildId, false);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public List<Long> getModmailPingRoleIdsByGuildId(long guildId) {
        List<Long> roleIds = new ArrayList<>();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "rolRoleId" FROM guilddata."tblRole" WHERE "fk_rolGuildId" = ? AND "rolIsModmailPingRole" = true;
                    """);
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                roleIds.add(resultSet.getLong("rolRoleId"));
            }
            connection.close();
            return roleIds;
        } catch (SQLException e) {
            handleSQLException(e);
            return roleIds;
        }
    }

    public long putDataInPeriodicCleanTable(long channelId, long guildId, long days, long userId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = putDataInPeriodicCleanTable(connection, channelId, guildId, userId, days);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public JSONObject getDataFromPeriodicCleanTable(long guildId) {
        JSONObject results = new JSONObject();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "pcdId","fk_pcdChannelId","pcdLastExecution","pcdNextExecution","pcdActive","fk_mbrUserId","pcdDays"
                    FROM botfunctiondata."tblPeriodicCleanData"
                    INNER JOIN guilddata."tblMember" tM ON "tblPeriodicCleanData"."fk_pcdMemberId" = tM."mbrId"
                    WHERE tM."fk_mbrGuildId" = ?;
                    """);
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                JSONObject entry = new JSONObject();
                entry.put("channelId", resultSet.getLong("fk_pcdChannelId"));
                Timestamp last = resultSet.getTimestamp("pcdLastExecution");
                entry.put("lastExecution", last != null ? last.toString() : "Null");
                entry.put("nextExecution", resultSet.getTimestamp("pcdNextExecution").toString());
                entry.put("active", resultSet.getBoolean("pcdActive"));
                entry.put("creator", resultSet.getLong("fk_mbrUserId"));
                entry.put("days", resultSet.getLong("pcdDays"));
                results.put(String.valueOf(resultSet.getLong("pcdId")), entry);
            }
            connection.close();
            return results;
        } catch (SQLException e) {
            handleSQLException(e);
            return results;
        }
    }

    public long editDataInPeriodicCleanTable(long databaseId, boolean activeState, long days) {
        try {
            Connection connection = DatabaseSource.connection();
            long dayUpdateValue = updateIntervalInPeriodicCleanTable(connection, databaseId, days);
            long activeUpdateValue = setActiveStateInPeriodicCleanTable(connection, databaseId, activeState);
            connection.close();
            if (dayUpdateValue < 0) return dayUpdateValue;
            if (activeUpdateValue < 0) return activeUpdateValue;
            return dayUpdateValue + activeUpdateValue;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long deleteDataFromPeriodicCleanTable(long databaseId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = removeDataFromPeriodicCleanTable(connection, databaseId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public JSONArray getDataFromSubscribedChannelTableByService(@NotNull NotificationService notificationService) {
        JSONArray results = new JSONArray();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "ctcName","ctcServiceId"
                    FROM notification."tblSubscribedChannel"
                    INNER JOIN notification."tblContentCreator" tCC on tCC."ctcId" = "tblSubscribedChannel"."fk_sbcContentCreatorId"
                    WHERE tCC."ctcServiceName" = ?;
                    """);
            preparedStatement.setString(1, notificationService.getServiceName());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                JSONObject entry = new JSONObject();
                entry.put("contentCreatorName", resultSet.getString("ctcName"));
                entry.put("contentCreatorId", resultSet.getString("ctcServiceId"));
                results.put(entry);
            }
            connection.close();
            return results;
        } catch (SQLException e) {
            handleSQLException(e);
            return results;
        }
    }

    public JSONArray getDataFromSubscribedChannelTableByContentCreatorId(String contentCreatorServiceId) {
        JSONArray results = new JSONArray();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "fk_sbcChannelId","sctMessageText","fk_cnlGuildId"
                    FROM notification."tblSubscribedChannel"
                    INNER JOIN notification."tblContentCreator" tCC on tCC."ctcId" = "tblSubscribedChannel"."fk_sbcContentCreatorId"
                    INNER JOIN guilddata."tblChannel" tC ON tC."cnlChannelId" = "tblSubscribedChannel"."fk_sbcChannelId"
                    WHERE tCC."ctcServiceId" = ?;
                    """);
            preparedStatement.setString(1, contentCreatorServiceId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                JSONObject entry = new JSONObject();
                entry.put("messageChannelId", resultSet.getLong("fk_sbcChannelId"));
                entry.put("messageText", resultSet.getString("sctMessageText"));
                entry.put("guildId", resultSet.getLong("fk_cnlGuildId"));
                results.put(entry);
            }
            connection.close();
            return results;
        } catch (SQLException e) {
            handleSQLException(e);
            return results;
        }
    }

    public long putIdInReceivedVideosTable(String videoId) {
        try {
            Connection connection = DatabaseSource.connection();
            long putVideoInReceivedVideoTable = putVideoInReceivedVideoTable(connection, videoId);
            connection.close();
            return putVideoInReceivedVideoTable;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public JSONObject getDataFromNoteTable(long targetUserId, long guildId) {
        JSONObject results = new JSONObject();
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "notTimestamp",tMod."fk_mbrUserId","notContent"
                    FROM botfunctiondata."tblNote"
                    INNER JOIN guilddata."tblMember" tTM ON "tblNote"."fk_notMemberId" = tTM."mbrId"
                    INNER JOIN guilddata."tblMember" tMod ON "tblNote"."fk_notModeratorId" = tMod."mbrId"
                    WHERE tTM."fk_mbrUserId" = ? AND tTm."fk_mbrGuildId" = ?;
                    """);
            preparedStatement.setLong(1, targetUserId);
            preparedStatement.setLong(2, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                JSONObject entry = new JSONObject();
                entry.put("moderatorId", resultSet.getLong("fk_mbrUserId"));
                entry.put("noteContent", resultSet.getString("notContent"));
                results.put(String.valueOf(resultSet.getTimestamp("notTimestamp")), entry);
            }
            connection.close();
            return results;
        } catch (SQLException e) {
            handleSQLException(e);
            return results;
        }
    }

    public long deleteDataFromNoteTable(long targetUserId, long guildId, LocalDateTime timestamp) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = removeDataFromNoteTable(connection, guildId, targetUserId, timestamp);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long updateDataFromNoteTable(long guildId, long targetUserId, LocalDateTime timestamp, String content) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = updateDataInNoteTable(connection, guildId, targetUserId, timestamp, content);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long putDataInNoteTable(long guildId, long targetUserId, long moderatorId, String note, LocalDateTime timestamp) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = putDataInNoteTable(connection, guildId, targetUserId, moderatorId, note, timestamp);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long putDataInContentCreatorTable(NotificationService notificationService, String serviceUserName, String serviceUserId) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = addContentCreatorInDatabase(connection, serviceUserName, notificationService, serviceUserId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long putDataInSubscribedChannelTable(Collection<GuildChannel> channels, String serviceUserId, String messageString) {
        try {
            Connection connection = DatabaseSource.connection();
            long databaseContentCreatorId = getContentCreatorDatabaseIdByContentCreatorServiceId(connection, serviceUserId);
            if (databaseContentCreatorId < 0) return databaseContentCreatorId;
            if (databaseContentCreatorId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
            long i = 0;
            for (GuildChannel channel : channels) {
                i = i + putDataInSubscribedChannelTable(connection, channel.getIdLong(), channel.getGuild().getIdLong(), databaseContentCreatorId, messageString);
            }
            connection.close();
            return i;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long updateDataInSubscribedChannelTable(String messageText, long channelId, String targetId) {
        try {
            Connection connection = DatabaseSource.connection();
            long databaseContentCreatorId = getContentCreatorDatabaseIdByContentCreatorServiceId(connection, targetId);
            if (databaseContentCreatorId < 0) return databaseContentCreatorId;
            if (databaseContentCreatorId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
            long value = updateDataInSubscribedChannelTable(connection, messageText, channelId, databaseContentCreatorId);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long removeDataFromSubscribedChannelTable(List<String> values, String contentCreatorServiceId) {
        try {
            Connection connection = DatabaseSource.connection();
            long databaseContentCreatorId = getContentCreatorDatabaseIdByContentCreatorServiceId(connection, contentCreatorServiceId);
            if (databaseContentCreatorId < 0) return databaseContentCreatorId;
            if (databaseContentCreatorId == 0) return ErrorCodes.SQL_UPDATE_PRECONDITION_FAILED.getId();
            long i = 0;
            for (String value : values) {
                i = i + removeDataFromSubscribedChannelTable(connection, Long.parseLong(value), databaseContentCreatorId);
            }
            connection.close();
            return i;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long existRowInSubscribedChannelTable(long channelId, String contentCreatorServiceId) {
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT COUNT(*) FROM notification."tblSubscribedChannel"
                    INNER JOIN notification."tblContentCreator" tCC on tCC."ctcId" = "tblSubscribedChannel"."fk_sbcContentCreatorId"
                    WHERE "ctcServiceId" = ? AND "fk_sbcChannelId" = ?;
                    """);
            preparedStatement.setString(1, contentCreatorServiceId);
            preparedStatement.setLong(2, channelId);
            long count = getSelectCountValue(preparedStatement);
            connection.close();
            return count;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long putPunishmentDataInPunishmentTable(long targetUserId, long moderatorUserId, long guildId, int punishmentType, String reason) {
        try {
            Connection connection = DatabaseSource.connection();
            long value = putDataInPunishmentTable(connection, guildId, targetUserId, moderatorUserId, punishmentType, reason);
            connection.close();
            return value;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public long getPunishmentCountFromGuild(long guildId) {
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT COUNT(*) FROM botfunctiondata."tblPunishment"
                    INNER JOIN guilddata."tblMember" tM ON "tblPunishment"."fk_psmMemberId" = tM."mbrId"
                    WHERE tM."fk_mbrGuildId" = ?;
                    """);
            preparedStatement.setLong(1, guildId);
            long count = getSelectCountValue(preparedStatement);
            connection.close();
            return count;
        } catch (SQLException e) {
            handleSQLException(e);
            return ErrorCodes.SQL_EXCEPTION.getId();
        }
    }

    public void deleteMessageAttachmentsBeforeSpecificTimestamp(LocalDateTime timestamp) {
        try {
            Connection connection = DatabaseSource.connection();
            PreparedStatement preparedStatement = connection.prepareStatement("""
                    SELECT "msgId" FROM guilddata."tblMessage" WHERE "msgTimestamp" < ?;
                    """);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(timestamp));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                removeMessageAttachmentDataFromDatabase(connection, resultSet.getLong("msgId"));
            }
            connection.close();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public void writeInMessageTable(Message message) {
        try {
            Connection connection = DatabaseSource.connection();
            updateMessageContentInDatabase(connection, message);
            connection.close();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    public void removeMessageFromDatabase(@NotNull List<Long> messageIds) {
        try {
            Connection connection = DatabaseSource.connection();
            for (long messageId : messageIds) {
                removeMessageFromDatabase(connection, messageId);
            }
            connection.close();
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }
}
