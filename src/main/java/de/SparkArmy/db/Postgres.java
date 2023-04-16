package de.SparkArmy.db;

import de.SparkArmy.Main;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Postgres {

    private final boolean isPostgresDisabled;
    private final Logger logger;

    private String url;

    private final Properties properties = new Properties();

    public Postgres(@NotNull Main main) {
        this.logger = main.getLogger();

        ConfigController controller = main.getController();
        JSONObject mainConfig = controller.getMainConfigFile();
        boolean disabled = true;
        if (mainConfig.isNull("postgres")) {
            this.logger.warn("postgres not exist in main-config.json");
        }
        JSONObject pstgsConfig = mainConfig.getJSONObject("postgres");
        if (pstgsConfig.getString("url").isEmpty()) {
            this.logger.warn("postgres-url is empty");
        } else if (pstgsConfig.getString("user").isEmpty()) {
            this.logger.warn("postgres-user is empty");
        } else if (pstgsConfig.getString("password").isEmpty()) {
            this.logger.warn("postgres-password is empty");
        } else {
            this.url = "jdbc:postgresql://" + pstgsConfig.getString("url");
            this.properties.setProperty("password", pstgsConfig.getString("password"));
            this.properties.setProperty("user", pstgsConfig.getString("user"));

            // try connection
            try {
                Connection conn = DriverManager.getConnection(this.url, this.properties);
                this.logger.info("postgres-connected");
                conn.close();
                // set config global and set postgresEnabled
                disabled = false;
            } catch (SQLException e) {
                this.logger.error(e.getMessage());
                this.logger.error("Please setup a PostgresDatabase and establish a connection");
                //                main.systemExit(40);
            }
        }
        this.isPostgresDisabled = disabled;
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(url, properties);
    }

    private boolean isGuildIdInDatabase(@NotNull Connection conn, long guildId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement("SELECT COUNT(*) FROM guilddata.\"tblGuild\" WHERE \"gldId\" = ?;");
        prepStmt.setLong(1, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
        return rs.getLong(1) > 0;
    }

    public boolean putGuildIdInGuildTable(long guildId) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            if (isGuildIdInDatabase(conn, guildId)) {
                conn.close();
                return true;
            }

            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO guilddata.\"tblGuild\" (\"gldId\")VALUES (?);");
            prepStmt.setLong(1, guildId);
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return false;
        }
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
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
        return rs.getLong(1) > 0;
    }

    public boolean putUserIdInUserTable(long userId) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            if (isUserIdInDatabase(conn, userId)) {
                conn.close();
                return true;
            }

            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO guilddata.\"tblUser\" (\"usrId\")VALUES (?);");
            prepStmt.setLong(1, userId);
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return false;
        }
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
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
        return rs.getLong(1) > 0;
    }

    public boolean putMemberInMemberTable(long userId, long guildId) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            if (isMemberInMemberTable(conn, userId, guildId)) {
                conn.close();
                return true;
            }
            putUserIdInUserTable(conn, userId);
            putGuildIdInGuildTable(conn, guildId);

            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO guilddata.\"tblMember\" (\"fk_mbrUserId\",\"fk_mbrGuildId\") VALUES (?,?);");
            prepStmt.setLong(1, userId);
            prepStmt.setLong(2, guildId);
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return false;
        }
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
        PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT \"mbrId\" FROM guilddata.\"tblMember\" WHERE \"fk_mbrUserId\" = ? AND \"fk_mbrGuildId\" = ?;");
        prepStmt.setLong(1, userId);
        prepStmt.setLong(2, guildId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) return -1;
        return rs.getLong(1);
    }

    private boolean isMemberInJoinLeaveMemberTable(@NotNull Connection conn, long databaseMemberId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM guilddata.\"tblJoinLeaveMember\" WHERE \"fk_jlmMemberId\" = ? AND \"jlmLeaveTime\" IS NULL;");
        prepStmt.setLong(1, databaseMemberId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
        return rs.getLong(1) > 0;
    }

    public boolean putDataInJoinLeaveMemberTable(Member member, boolean isJoin) {
        if (isPostgresDisabled) return false;
        try {
            // Get Connection
            Connection conn = connection();
            // Get Discord userId, Discord guildId and database memberId and returned when getMemberIdFromMemberTable failed
            long memberId = member.getIdLong(); // memberId equal userId
            long guildId = member.getGuild().getIdLong();
            long mbrId = getMemberIdFromMemberTable(conn, memberId, guildId);
            if (mbrId == -1) {
                conn.close();
                return false;
            }
            if (isJoin) {
                if (isMemberInJoinLeaveMemberTable(conn, mbrId)) {
                    conn.close();
                    return true;
                }
                PreparedStatement prepStmt = conn.prepareStatement(
                        "INSERT INTO guilddata.\"tblJoinLeaveMember\" (\"fk_jlmMemberId\",\"jlmJoinTime\") VALUES (?,?);");
                prepStmt.setLong(1, mbrId);
                prepStmt.setTimestamp(2, Timestamp.valueOf(member.getTimeJoined().toLocalDateTime()));
                prepStmt.execute();

            } else {
                if (isMemberInJoinLeaveMemberTable(conn, memberId)) {
                    PreparedStatement prepStmt = conn.prepareStatement(
                            "UPDATE guilddata.\"tblJoinLeaveMember\"SET \"jlmLeaveTime\" = now() WHERE \"fk_jlmMemberId\" = ?;");
                    prepStmt.setLong(1, mbrId);
                    prepStmt.execute();
                } else {
                    if (putDataInJoinLeaveMemberTable(member, true)) {
                        conn.close();
                        return false;
                    }
                    PreparedStatement prepStmt = conn.prepareStatement(
                            "UPDATE guilddata.\"tblJoinLeaveMember\"SET \"jlmLeaveTime\" = now() WHERE \"fk_jlmMemberId\" = ?;");
                    prepStmt.setLong(1, mbrId);
                    prepStmt.execute();
                }
            }
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return false;
        }
    }

    private boolean isModeratorInModeratorTable(@NotNull Connection conn, long databaseMemberId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM guilddata.\"tblModerator\" WHERE \"fk_modMemberId\" = ?;");
        prepStmt.setLong(1, databaseMemberId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
        return rs.getLong(1) > 0;
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

    public boolean putDataInModeratorTable(Member member) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();

            // Get User and Guild Id
            long userId = member.getIdLong();
            long guildId = member.getGuild().getIdLong();
            // Get DatabaseMemberId
            long mbrId = getMemberIdFromMemberTable(conn, userId, guildId);

            if (isModeratorInModeratorTable(conn, mbrId)) {
                conn.close();
                return true;
            }

            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO guilddata.\"tblModerator\" (\"fk_modMemberId\")VALUES (?);");
            prepStmt.setLong(1, mbrId);
            prepStmt.execute();
            conn.close();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return false;
        }
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
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
        return rs.getLong(1);
    }

    public long getPunishmentCountFromGuild(Guild guild) {
        if (isPostgresDisabled) return -1;
        try {
            long guildId = guild.getIdLong();

            Connection conn = connection();
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM guilddata.\"tblPunishment\" WHERE \"fk_psmGuildId\" = ?;");
            prepStmt.setLong(1, guildId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) {
                throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
            }
            conn.close();
            return rs.getLong(1);
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return -1;
        }
    }

    public boolean putPunishmentDataInPunishmentTable(User target, Member moderator, int punishmentType, String reason) {
        if (isPostgresDisabled) return false;
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
            return true;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return false;
        }
    }

    private boolean isMessageInMessageTable(@NotNull Connection conn, long messageId) throws SQLException {
        PreparedStatement prepStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM guilddata.\"tblMessage\" WHERE \"msgId\" = ?;");
        prepStmt.setLong(1, messageId);
        ResultSet rs = prepStmt.executeQuery();
        if (!rs.next()) {
            throw new IllegalArgumentException("ResultSet from \"SELECT COUNT(*)\" always have a first row");
        }
        return rs.getLong(1) > 0;
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
            Util.handleSQLExeptions(e);
            return false;
        }
    }

    public boolean updateDataFromNoteTable(String note, Member mbr, Member mod, Timestamp timestamp) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            long databaseMemberId = getMemberIdFromMemberTable(conn, mbr.getIdLong(), mbr.getGuild().getIdLong());
            long databaseModeratorId = getModeratorIdFromModeratorTable(conn, getMemberIdFromMemberTable(conn, mod.getIdLong(), mod.getGuild().getIdLong()));

            PreparedStatement prepStmt = conn.prepareStatement("""
                    UPDATE guilddata."tblNote" SET "notContent" = ? WHERE "fk_notMemberId" = ? AND "fk_notModeratorId" = ? AND "notTimestamp" = ?;
                    """);
            prepStmt.setString(1, note);
            prepStmt.setLong(2, databaseMemberId);
            prepStmt.setLong(3, databaseModeratorId);
            prepStmt.setTimestamp(4, timestamp);
            prepStmt.execute();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return false;
        }
    }

    public boolean deleteDataFromNoteTable(Member mbr, Member mod, Timestamp timestamp) {
        if (isPostgresDisabled) return false;
        try {
            Connection conn = connection();
            long databaseMemberId = getMemberIdFromMemberTable(conn, mbr.getIdLong(), mbr.getGuild().getIdLong());
            long databaseModeratorId = getModeratorIdFromModeratorTable(conn, getMemberIdFromMemberTable(conn, mod.getIdLong(), mod.getGuild().getIdLong()));

            PreparedStatement prepStmt = conn.prepareStatement("""
                    DELETE FROM guilddata."tblNote" WHERE "notTimestamp" = ? AND "fk_notModeratorId" = ?AND  "fk_notMemberId" = ?;
                    """);
            prepStmt.setTimestamp(1, timestamp);
            prepStmt.setLong(2, databaseModeratorId);
            prepStmt.setLong(3, databaseMemberId);
            prepStmt.execute();
            return true;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
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
            return results;
        } catch (SQLException e) {
            Util.handleSQLExeptions(e);
            return new JSONObject();
        }
    }

    public boolean getIsPostgresEnabled() {
        return !isPostgresDisabled;
    }
}
