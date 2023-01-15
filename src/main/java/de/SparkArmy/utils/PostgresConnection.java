package de.SparkArmy.utils;

import de.SparkArmy.utils.jda.MessageUtil;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PostgresConnection {

    private static final Logger logger = MainUtil.logger;
    private static boolean postgresDisabled = true;

    // Connection url and properties for connection
    private static String url;
    private static final Properties properties = new Properties();

    public static void checkPreconditions() {
        JSONObject mainConfig = MainUtil.mainConfig;

        // check config and set properties
        if (mainConfig.isNull("postgres")) {
            logger.warn("postgres not exist in main-config.json");
            return;
        }
        JSONObject pstgsConfig = mainConfig.getJSONObject("postgres");
        if (pstgsConfig.getString("url").isEmpty()) {
            logger.warn("postgres-url is empty");
            return;
        }
        url = "jdbc:postgresql://" + pstgsConfig.getString("url");

        if (pstgsConfig.getString("user").isEmpty()) {
            logger.warn("postgres-user is empty");
            return;
        }
        properties.setProperty("user", pstgsConfig.getString("user"));

        if (pstgsConfig.getString("password").isEmpty()) {
            logger.warn("postgres-password is empty");
            return;
        }
        properties.setProperty("password", pstgsConfig.getString("password"));

        // try connection
        try {
            Connection conn = DriverManager.getConnection(url, properties);
            logger.info("postgres-connected");
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return;
        }

        // set config global and set postgresEnabled
        postgresDisabled = false;
    }

    private static @Nullable Connection connection() {
        try {
            return DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    // Check if guild in tblGuild
    private static boolean isGuildInGuildTable(@NotNull Connection conn, @NotNull Guild g) {
        if (postgresDisabled) return true;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM public.\"tblGuild\" WHERE \"gldId\" = ?;");
            prepStmt.setLong(1, g.getIdLong());
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return false;
            return rs.getLong(1) > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    // Put data (guildId and time) in tblGuild
    public static void putDataInGuildTable(Guild g) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            if (isGuildInGuildTable(conn, g)) {
                conn.close();
                return;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblGuild\"(" +
                            "\"gldId\", \"gldTimeCreated\")" +
                            "VALUES (?, ?);");
            prepStmt.setLong(1, g.getIdLong());
            prepStmt.setTimestamp(2, Timestamp.valueOf(g.getTimeCreated().toLocalDateTime()));
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void putDataInGuildTable(Connection conn, Guild g) {
        if (postgresDisabled) return;
        try {
            if (isGuildInGuildTable(conn, g)) return;
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblGuild\"(" +
                            "\"gldId\", \"gldTimeCreated\")" +
                            "VALUES (?, ?);");
            prepStmt.setLong(1, g.getIdLong());
            prepStmt.setTimestamp(2, Timestamp.valueOf(g.getTimeCreated().toLocalDateTime()));
            prepStmt.execute();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // tblUser related methods

    // Check if user in tblUser
    private static boolean isUserInUserTable(Connection conn, User u) {
        if (postgresDisabled) return true;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM public.\"tblUser\" WHERE \"usrId\" = ?;");
            prepStmt.setLong(1, u.getIdLong());
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return false;
            return rs.getLong(1) > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    // Get user-id by database-memberId
    private static long getDiscordUserIdByDatabaseMemberId(Connection conn, long mbrId) {
        if (postgresDisabled) return 0;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"fk_mbrUserId\" FROM public.\"tblMember\" WHERE \"mbrId\" = ?;"
            );
            prepStmt.setLong(1, mbrId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return 0;
            return rs.getLong(1);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Put data (userId, user-name and account-created) in tblUser
    private static void putDataInUserTable(Connection conn, User u) {
        if (postgresDisabled) return;
        try {
            if (isUserInUserTable(conn, u)) return;
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblUser\"(" +
                            "\"usrId\",\"usrName\",\"usrAccountCreated\")" +
                            "VALUES (?,?,?);");
            prepStmt.setLong(1, u.getIdLong());
            prepStmt.setString(2, u.getName());
            prepStmt.setTimestamp(3, Timestamp.valueOf(u.getTimeCreated().toLocalDateTime()));
            prepStmt.execute();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // Update user-name from user in tblUser
    public static void updateUserInUserTable(User u) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            if (!isUserInUserTable(conn, u)) {
                putDataInUserTable(conn, u);
                conn.close();
            } else {
                PreparedStatement prepStmt = conn.prepareStatement(
                        "UPDATE public.\"tblUser\"" +
                                "SET \"usrName\" = ? " +
                                "WHERE \"usrId\" = ?");
                prepStmt.setString(1, u.getName());
                prepStmt.setLong(2, u.getIdLong());
                prepStmt.execute();
                conn.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // tblMember related Methods

    // Check if member-data in tblMember
    private static boolean isMemberDataInMemberTable(Connection conn, Member m) {
        if (postgresDisabled) return true;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM public.\"tblMember\" " +
                            "WHERE \"fk_mbrUserId\" = ? " +
                            "AND \"fk_mbrGuildId\" = ? " +
                            "AND \"mbrLeaveTime\" IS NULL;"
            );
            prepStmt.setLong(1, m.getIdLong());
            prepStmt.setLong(2, m.getGuild().getIdLong());
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return false;
            return rs.getLong(1) > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    // get member ids from different providers
    private static long getDatabaseMemberIdByDiscordUserId(Connection conn, Member m) {
        if (postgresDisabled) return 0;
        try {
            putDataInMemberTable(conn, m);
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"mbrId\" FROM public.\"tblMember\"" +
                            "WHERE \"fk_mbrUserId\" = ? " +
                            "AND \"fk_mbrGuildId\" = ? " +
                            "AND \"mbrLeaveTime\" IS NULL;"
            );
            prepStmt.setLong(1, m.getIdLong());
            prepStmt.setLong(2, m.getGuild().getIdLong());
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return 0;
            return rs.getLong(1);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private static long getDatabaseMemberIdByDatabaseModeratorId(Connection conn, long modId) {
        if (postgresDisabled) return 0;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"fk_modMemberId\" FROM public.\"tblModerator\" WHERE \"modId\" = ?;"
            );
            prepStmt.setLong(1, modId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return 0;
            return rs.getLong(1);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private static long getDatabaseMemberIdByDiscordMessageId(Connection conn, long msgId) {
        if (postgresDisabled) return 0;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"fk_msgMemberId\" FROM public.\"tblMessage\" WHERE \"msgId\" = ?;"
            );
            prepStmt.setLong(1, msgId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return 0;
            return rs.getLong(1);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Put data in tblMember
    public static void putDataInMemberTable(Member m) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            putDataInUserTable(conn, m.getUser());
            putDataInGuildTable(conn, m.getGuild());
            if (isMemberDataInMemberTable(conn, m)) {
                conn.close();
                return;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblMember\"(" +
                            "\"fk_mbrUserId\",\"fk_mbrGuildId\",\"mbrJoinTime\") VALUES" +
                            "(?,?,?);"
            );
            prepStmt.setLong(1, m.getIdLong());
            prepStmt.setLong(2, m.getGuild().getIdLong());
            prepStmt.setTimestamp(3, Timestamp.valueOf(m.getTimeJoined().toLocalDateTime()));
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void putDataInMemberTable(Connection conn, Member m) {
        if (postgresDisabled) return;
        try {
            putDataInUserTable(conn, m.getUser());
            putDataInGuildTable(conn, m.getGuild());
            if (isMemberDataInMemberTable(conn, m)) return;
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblMember\"(" +
                            "\"fk_mbrUserId\",\"fk_mbrGuildId\",\"mbrJoinTime\") VALUES" +
                            "(?,?,?);"
            );
            prepStmt.setLong(1, m.getIdLong());
            prepStmt.setLong(2, m.getGuild().getIdLong());
            prepStmt.setTimestamp(3, Timestamp.valueOf(m.getTimeJoined().toLocalDateTime()));
            prepStmt.execute();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // Add leave timestamp to the specific row in tblMember
    public static void addLeaveTimestampInMemberTable(Member m) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            long memberId = getDatabaseMemberIdByDiscordUserId(conn, m);
            if (memberId == 0) {
                conn.close();
                return;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "UPDATE public.\"tblMember\" " +
                            "SET \"mbrLeaveTime\" = ? " +
                            "WHERE \"mbrId\" = ?;"
            );
            OffsetDateTime t = OffsetDateTime.now();
            prepStmt.setTimestamp(1, Timestamp.valueOf(t.toLocalDateTime()));
            prepStmt.setLong(2, memberId);
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // tblModerator related methods

    private static boolean isModeratorInModeratorTable(Connection conn, long memberId) {
        if (postgresDisabled) return true;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(

                    "SELECT COUNT(*) FROM public.\"tblModerator\"" +
                            "WHERE \"fk_modMemberId\" = ? " +
                            "AND \"modDissapointmentTime\" IS NULL;"
            );
            prepStmt.setLong(1, memberId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return false;
            return rs.getLong(1) > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    // Get moderator-id from database by discord-user-id
    private static long getDatabaseModeratorIdByDiscordUserId(Connection conn, Member m) {
        if (postgresDisabled) return 0;
        try {
            long memberId = getDatabaseMemberIdByDiscordUserId(conn, m);
            if (!isModeratorInModeratorTable(conn, memberId)) return 0;
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"modId\" FROM public.\"tblModerator\" " +
                            "WHERE \"fk_modMemberId\" = ? " +
                            "AND \"modDissapointmentTime\" IS NULL;"
            );
            prepStmt.setLong(1, memberId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) {
                return 0;
            }
            return rs.getLong(1);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // Add member in tblModerator
    public static void putDataInModeratorTable(Member m) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            long memberId = getDatabaseMemberIdByDiscordUserId(conn, m);
            if (isModeratorInModeratorTable(conn, memberId)) {
                conn.close();
                return;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblModerator\"(" +
                            "\"fk_modMemberId\",\"modAppointmentTime\")" +
                            "VALUES (?,?);"
            );
            OffsetDateTime t = OffsetDateTime.now();
            prepStmt.setLong(1, memberId);
            prepStmt.setTimestamp(2, Timestamp.valueOf(t.toLocalDateTime()));
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // Add disappointment-time in tblModerator
    public static void addDisappointmentTimeInModeratorTable(Member m) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            long moderatorId = getDatabaseModeratorIdByDiscordUserId(conn, m);
            if (moderatorId == 0) {
                conn.close();
                return;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "UPDATE public.\"tblModerator\"" +
                            "SET \"modDissapointmentTime\" = ?" +
                            "WHERE \"modId\" = ?;"
            );
            OffsetDateTime t = OffsetDateTime.now();
            prepStmt.setTimestamp(1, Timestamp.valueOf(t.toLocalDateTime()));
            prepStmt.setLong(2, moderatorId);
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // tblPunishment related methods

    // count the punishments from specific guild, add one and return the result
    public static long getLatestPunishmentIdFromPunishmentTable(Guild g) {
        if (postgresDisabled) return 1;
        try {
            Connection conn = connection();
            if (conn == null) return 1;
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM public.\"tblPunishment\"" +
                            "INNER JOIN public.\"tblMember\"" +
                            "ON public.\"tblPunishment\".\"fk_psmMemberId\" = public.\"tblMember\".\"mbrId\"" +
                            "WHERE public.\"tblMember\".\"fk_mbrGuildId\" = ?;"
            );
            prepStmt.setLong(1, g.getIdLong());
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) {
                conn.close();
                return 1;
            }
            long id = rs.getLong(1) + 1;
            conn.close();
            return id;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    // get punishment-type-name
    public static @Nullable String getDatabasePunishmentTypeNameByPunishmentTypeId(Connection conn, long id) {
        if (postgresDisabled) return null;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"pmtName\" FROM public.\"tblPunishmentType\" WHERE \"pmtId\" = ?;"
            );
            prepStmt.setLong(1, id);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return null;
            return rs.getString(1);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // get punishments from specific user
    public static @Nullable JSONArray getPunishmentDataByOffender(Member offender) {
        if (postgresDisabled) return null;
        try {
            Connection conn = connection();
            if (conn == null) return null;

            long mbrId = getDatabaseMemberIdByDiscordUserId(conn, offender);

            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT * FROM public.\"tblPunishment\" WHERE \"fk_psmMemberId\" = ? ORDER BY \"psmTimestamp\" ASC;"
            );
            prepStmt.setLong(1, mbrId);
            ResultSet rs = prepStmt.executeQuery();
            JSONArray results = new JSONArray();
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("id", rs.getLong(1));
                row.put("mbrId", offender.getId());
                long modMemberId = getDatabaseMemberIdByDatabaseModeratorId(conn, rs.getLong(3));
                if (modMemberId == 0) break;
                long modUserId = getDiscordUserIdByDatabaseMemberId(conn, modMemberId);
                row.put("modId", modUserId);
                String punishment = getDatabasePunishmentTypeNameByPunishmentTypeId(conn, rs.getLong(4));
                if (punishment == null) punishment = "Can't get the punishment";
                row.put("punishment", punishment);
                row.put("timestamp", rs.getTimestamp(5));
                row.put("reason", rs.getString(6));
                results.put(row);
            }
            return results;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static @Nullable JSONArray getPunishmentDataByOffender(Member offender, PunishmentType type) {
        if (postgresDisabled) return null;
        try {
            Connection conn = connection();
            if (conn == null) return null;

            long mbrId = getDatabaseMemberIdByDiscordUserId(conn, offender);

            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT * FROM public.\"tblPunishment\" WHERE \"fk_psmMemberId\" = ? AND \"fk_psmPunishmentTypeId\" = ? ORDER BY \"psmTimestamp\" ASC;"
            );
            prepStmt.setLong(1, mbrId);
            prepStmt.setLong(2, type.getId());
            ResultSet rs = prepStmt.executeQuery();
            JSONArray results = new JSONArray();
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("id", rs.getLong(1));
                row.put("mbrId", offender.getId());
                long modMemberId = getDatabaseMemberIdByDatabaseModeratorId(conn, rs.getLong(3));
                if (modMemberId == 0) break;
                long modUserId = getDiscordUserIdByDatabaseMemberId(conn, modMemberId);
                row.put("modId", modUserId);
                String punishment = getDatabasePunishmentTypeNameByPunishmentTypeId(conn, rs.getLong(4));
                if (punishment == null) punishment = "Can't get the punishment";
                row.put("punishment", punishment);
                row.put("timestamp", rs.getTimestamp(5));
                results.put(row);
            }
            return results;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // put data in tblPunishment
    public static void putDataInPunishmentTable(Member member, Member moderator, PunishmentType type, String reason) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            long modId = getDatabaseModeratorIdByDiscordUserId(conn, moderator);
            long mbrId = getDatabaseMemberIdByDiscordUserId(conn, member);
            int typId = type.getId();
            OffsetDateTime t = OffsetDateTime.now();

            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblPunishment\"(" +
                            "\"fk_psmMemberId\",\"fk_psmModeratorId\",\"fk_psmPunishmentTypeId\",\"psmTimestamp\",\"psmReason\")" +
                            "VALUES (?,?,?,?,?);"
            );
            prepStmt.setLong(1, mbrId);
            prepStmt.setLong(2, modId);
            prepStmt.setInt(3, typId);
            prepStmt.setTimestamp(4, Timestamp.valueOf(t.toLocalDateTime()));
            prepStmt.setString(5, reason);
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // tblMessage and tblMessageAttachment related methods

    // check if msgId in tblMessage
    private static boolean isMessageIdInMessageTable(Connection conn, long msgId) {
        if (postgresDisabled) return true;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM public.\"tblMessage\" WHERE \"msgId\" = ?;"
            );
            prepStmt.setLong(1, msgId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return false;
            return rs.getLong(1) > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    // get msgContent from the tblMessage by msgId
    public static @Nullable String getMessageContentByMessageId(long msgId) {
        if (postgresDisabled) return null;
        try {
            Connection conn = connection();
            if (conn == null) return null;
            if (!isMessageIdInMessageTable(conn, msgId)) {
                conn.close();
                return null;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"msgContent\" FROM public.\"tblMessage\" WHERE \"msgId\" = ?;"
            );
            prepStmt.setLong(1, msgId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) {
                conn.close();
                return null;
            }
            String content = rs.getString(1);
            conn.close();
            return content;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // get userId by messageId
    public static long getUserIdByMessageId(long msgId) {
        if (postgresDisabled) return 0;
        try {
            Connection conn = connection();
            if (conn == null) return 0;

            long mbrId = getDatabaseMemberIdByDiscordMessageId(conn, msgId);
            if (mbrId == 0) {
                conn.close();
                return 0;
            }
            long userId = getDiscordUserIdByDatabaseMemberId(conn, mbrId);
            if (userId == 0) {
                conn.close();
                return 0;
            }
            conn.close();
            return userId;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    // get attachment-urls from message
    public static @NotNull List<String> getMessageAttachmentUrlsByDiscordMessageID(long msgId) {
        if (postgresDisabled) return new ArrayList<>();
        try {
            Connection conn = connection();
            if (conn == null) return new ArrayList<>();
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"msaContentUrl\" FROM public.\"tblMessageAttachment\"" +
                            "WHERE \"fk_msaMessageId\" = ?;"
            );
            prepStmt.setLong(1, msgId);
            ResultSet rs = prepStmt.executeQuery();
            List<String> contentUrls = new ArrayList<>();
            while (rs.next()) {
                contentUrls.add(rs.getString(1));
            }
            conn.close();
            return contentUrls;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // put data in tblMessage
    public static void putDataInMessageTable(Message msg) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            long mbrId = getDatabaseMemberIdByDiscordUserId(conn, msg.getMember());
            if (mbrId == 0 || isMessageIdInMessageTable(conn, msg.getIdLong())) {
                conn.close();
                return;
            }
            PreparedStatement prepStmt;
            if (msg.getContentRaw().isEmpty()) {
                prepStmt = conn.prepareStatement(
                        "INSERT INTO public.\"tblMessage\"(" +
                                "\"msgId\",\"fk_msgMemberId\",\"msgTimestamp\")" +
                                "VALUES (?,?,?);"
                );
            } else {
                prepStmt = conn.prepareStatement(
                        "INSERT INTO public.\"tblMessage\"(" +
                                "\"msgId\",\"fk_msgMemberId\",\"msgTimestamp\",\"msgContent\")" +
                                "VALUES (?,?,?,?);"
                );
                prepStmt.setString(4, msg.getContentRaw());
            }
            prepStmt.setLong(1, msg.getIdLong());
            prepStmt.setLong(2, mbrId);
            prepStmt.setTimestamp(3, Timestamp.valueOf(msg.getTimeCreated().toLocalDateTime()));
            prepStmt.execute();
            conn.close();

            putDataInMessageAttachmentTable(msg);

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // put data in tblMessageAttachment
    private static void putDataInMessageAttachmentTable(@NotNull Message msg) {
        if (postgresDisabled) return;
        if (msg.getAttachments().isEmpty()) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblMessageAttachment\"(" +
                            "\"fk_msaMessageId\",\"msaContentUrl\")" +
                            "VALUES (?,?);"
            );
            prepStmt.setLong(1, msg.getIdLong());
            JSONArray urls = MessageUtil.storeDataOnStorageServer(msg);
            urls.forEach(x -> {
                try {
                    prepStmt.setString(2, x.toString());
                    prepStmt.execute();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            });
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }

    // update data (msgContent) in tblMessage
    public static void updateDataInMessageTable(Message msg) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;

            if (!isMessageIdInMessageTable(conn, msg.getIdLong())) {
                conn.close();
                putDataInMessageTable(msg);
                return;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "UPDATE public.\"tblMessage\" " +
                            "SET \"msgContent\" = ? " +
                            "WHERE \"msgId\" = ?;"
            );
            prepStmt.setString(1, msg.getContentRaw());
            prepStmt.setLong(2, msg.getIdLong());
            prepStmt.execute();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // tblNickname related methods

    // check if database-mbrId in tblNickname
    private static boolean isDatabaseMemberIdNotInNicknameTable(Connection conn, long mbrId) {
        if (postgresDisabled) return false;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM public.\"tblNickname\" WHERE \"fk_nickMemberId\" = ?;"
            );
            prepStmt.setLong(1, mbrId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return true;
            return rs.getLong(1) == 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // get the latest nickname from member
    public static @Nullable String getLatestNicknameByDiscordUserId(Member mbr) {
        if (postgresDisabled) return null;
        try {
            Connection conn = connection();
            if (conn == null) return null;
            long mbrId = getDatabaseMemberIdByDiscordUserId(conn, mbr);
            if (mbrId == 0) {
                conn.close();
                return null;
            }
            if (isDatabaseMemberIdNotInNicknameTable(conn, mbrId)) {
                conn.close();
                putDataInNicknameTable(mbr);
                return null;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"nickValue\" FROM public.\"tblNickname\" WHERE \"fk_nickMemberId\" = ?" +
                            "ORDER BY \"nickChangeTime\" DESC LIMIT 1;"
            );
            prepStmt.setLong(1, mbrId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) {
                conn.close();
                return null;
            }
            return rs.getString(1);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // get nicknames from tblNickname by userId
    public static @NotNull JSONArray getNicknamesFromMemberByDiscordUserId(Member mbr) {
        if (postgresDisabled) return new JSONArray();
        try {
            Connection conn = connection();
            if (conn == null) return new JSONArray();

            // get database mbrId
            long mbrId = getDatabaseMemberIdByDiscordUserId(conn, mbr);
            if (mbrId == 0) return new JSONArray();

            // check exist a nickname in tblNickname
            if (isDatabaseMemberIdNotInNicknameTable(conn, mbrId)) return new JSONArray();

            // create a statement and execute this
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT \"nickValue\" FROM public.\"tblNickname\" WHERE \"fk_nickMemberId\" = ? ORDER BY \"nickChangeTime\""
            );
            prepStmt.setLong(1, mbrId);
            ResultSet rs = prepStmt.executeQuery();

            // write the results in a JsonArray and return this
            JSONArray results = new JSONArray();
            while (rs.next()) {
                results.put(rs.getString(1));
            }
            return results;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return new JSONArray();
        }
    }

    // put data in tblNickname
    public static void putDataInNicknameTable(Member mbr) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;

            // get database member id
            long mbrId = getDatabaseMemberIdByDiscordUserId(conn, mbr);
            if (mbrId == 0) return;

            OffsetDateTime t = OffsetDateTime.now();

            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblNickname\"(" +
                            "\"fk_nickMemberId\",\"nickChangeTime\",\"nickValue\")" +
                            "VALUES (?,?,?);"
            );
            // set values in prepared statement
            prepStmt.setLong(1, mbrId);
            prepStmt.setTimestamp(2, Timestamp.valueOf(t.toLocalDateTime()));
            prepStmt.setString(3, mbr.getEffectiveName());

            // execute the sql string and close the connection
            prepStmt.execute();
            conn.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // tblRole and tblRoleActions related methods

    // check if the role in tblRole
    private static boolean isRoleInRoleTable(Connection conn, long roleId) {
        if (postgresDisabled) return true;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM public.\"tblRole\" WHERE \"rolId\" = ?;"
            );
            prepStmt.setLong(1, roleId);
            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return false;
            return rs.getLong(1) > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    // put data in tblRole
    private static void putDataInRoleTable(Connection conn, Role role) {
        if (postgresDisabled) return;
        try {
            if (isRoleInRoleTable(conn, role.getIdLong())) {
                return;
            }
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblRole\" (" +
                            "\"rolId\",\"fk_rolGuildId\") VALUES" +
                            "(?,?);"
            );

            prepStmt.setLong(1, role.getIdLong());
            prepStmt.setLong(2, role.getGuild().getIdLong());

            prepStmt.execute();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    // check if a row in tblRoleActions that contains the mbrId, roleId and a Null Value of racRemoveTime
    private static boolean isRoleActionFromMemberAndRoleRemoveTimeInRoleActionsTable(Connection conn, long roleId, long memberId) {
        if (postgresDisabled) return true;
        try {
            PreparedStatement prepStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM public.\"tblRoleActions\" " +
                            "WHERE \"fk_racMemberId\" = ? " +
                            "AND \"fk_racRoleId\" = ? " +
                            "AND \"racRemoveTime\" IS NULL;"
            );
            prepStmt.setLong(1, memberId);
            prepStmt.setLong(2, roleId);

            ResultSet rs = prepStmt.executeQuery();
            if (!rs.next()) return false;
            return rs.getLong(1) > 0;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    // put data for each added role in tblRoleActions
    public static void putDataInRoleActionsTable(GuildMemberRoleAddEvent e) {
        if (postgresDisabled) return;
        try {
            Member m = e.getMember();
            List<Role> roles = e.getRoles();
            // get connection
            Connection conn = connection();
            if (conn == null) return;
            // get database member id
            long mbrId = getDatabaseMemberIdByDiscordUserId(conn, m);
            // prepare a statement, to insert in table
            PreparedStatement prepStmt = conn.prepareStatement(
                    "INSERT INTO public.\"tblRoleActions\" (" +
                            "\"fk_racMemberId\",\"fk_racRoleId\",\"racAddTime\") " +
                            "VALUES (?,?,?);"
            );
            // Set "racAddTime"
            OffsetDateTime t = OffsetDateTime.now();
            prepStmt.setLong(1, mbrId);
            prepStmt.setTimestamp(3, Timestamp.valueOf(t.toLocalDateTime()));
            // check for each added role if in table and add if not exist
            roles.forEach(r -> {
                try {
                    putDataInRoleTable(conn, r);
                    if (isRoleActionFromMemberAndRoleRemoveTimeInRoleActionsTable(conn, r.getIdLong(), mbrId)) return;

                    prepStmt.setLong(2, r.getIdLong());
                    prepStmt.execute();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                    ex.printStackTrace();
                }
            });
            conn.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    // add a remove timestamp to the roles
    public static void addRemoveTimeInRoleActionsTable(GuildMemberRoleRemoveEvent e) {
        if (postgresDisabled) return;
        try {
            Connection conn = connection();
            if (conn == null) return;
            OffsetDateTime t = OffsetDateTime.now();
            long mbrId = getDatabaseMemberIdByDiscordUserId(conn,e.getMember());

            PreparedStatement prepStmt = conn.prepareStatement(
                    "UPDATE public.\"tblRoleActions\" " +
                            "SET \"racRemoveTime\" = ? " +
                            "WHERE \"racRemoveTime\" IS NULL " +
                            "AND \"fk_racMemberId\" = ? " +
                            "AND \"fk_racRoleId\" = ?;"
            );


            prepStmt.setTimestamp(1, Timestamp.valueOf(t.toLocalDateTime()));
            prepStmt.setLong(2,mbrId);
            e.getRoles().forEach(r->{
                try {
                    putDataInRoleTable(conn,r);
                    long rolId = r.getIdLong();
                    if (!isRoleActionFromMemberAndRoleRemoveTimeInRoleActionsTable(conn,rolId,mbrId)) return;
                    prepStmt.setLong(3,rolId);
                    prepStmt.execute();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
            conn.close();
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
