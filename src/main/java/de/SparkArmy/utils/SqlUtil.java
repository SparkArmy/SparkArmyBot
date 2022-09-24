package de.SparkArmy.utils;

import de.SparkArmy.utils.jda.MessageUtil;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil {
   private static final Logger logger = MainUtil.logger;

    private static boolean sqlEnabled = false;

   public static void setSqlEnabled(){
       new Thread(()->{
           //noinspection resource
           boolean state = connection() != null;
           if (!state){
               logger.warn("SQL is not available");
           }
           sqlEnabled = state;
       }).start();
   }


    private static java.sql.@Nullable Connection connection(String database){
        JSONObject mariaDbConfig = MainUtil.controller.getMainConfigFile().getJSONObject("mariaDbConnection");
        Connection c;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            c = DriverManager.getConnection("jdbc:mariadb:" + mariaDbConfig.getString("url") + database,mariaDbConfig.getString("user"),mariaDbConfig.getString("password"));
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e.getMessage());
            return null;
        }

        return c;
    }

    private static @Nullable Connection connection(){
        JSONObject mariaDbConfig = MainUtil.controller.getMainConfigFile().getJSONObject("mariaDbConnection");
        Connection c;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            c = DriverManager.getConnection("jdbc:mariadb:" + mariaDbConfig.getString("url"), mariaDbConfig.getString("user"),mariaDbConfig.getString("password"));
        } catch (SQLException | ClassNotFoundException e) {
            return null;
        }

        return c;
    }

    @SuppressWarnings({"ConstantConditions", "resource"})
    private static Statement statement() throws SQLException,NullPointerException {
        return connection().createStatement();
    }

    @SuppressWarnings({"ConstantConditions", "resource"})
    private static Statement guildStatement(@NotNull Guild guild) throws SQLException,NullPointerException {
        return connection("G" + guild.getId()).createStatement();
    }

    private static String getSqlTimeStringFromDatetime(@NotNull OffsetDateTime time){
        String timeString = time.toString().replace("T", " ").replace("Z","");
        String formatString = "'%Y-%m-%d %H:%i:%s.%f'";
        return String.format("STR_TO_DATE('%s',%s)",timeString,formatString);
    }

    private static boolean createGuildDatabase(String databaseName){
        if (!sqlEnabled) return false;
        try {
            Statement stmt = statement();
            String sql = "CREATE DATABASE IF NOT EXISTS G" + databaseName;
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void dropGuildDatabase(String databaseName) {
        if (!sqlEnabled) return;
        try {
            Statement stmt = statement();
            String sql = "DROP DATABASE G" + databaseName;
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static void insertIn(Guild guild, String sql){
       if (!sqlEnabled) return;
       try {
           Statement stmt = guildStatement(guild);
           stmt.executeUpdate(sql);
           stmt.close();
       } catch (SQLException e) {
           logger.error(e.getMessage());
       }
    }


    public static void createDatabaseAndTablesForGuild(@NotNull Guild guild){
        if (!sqlEnabled) return;
        String guildId = guild.getId();
        if (!createGuildDatabase(guildId)){
            logger.error("Failed to create Database for " + guildId);
            return;
        }

        String userTable =
                "CREATE TABLE IF NOT EXISTS tblUser(" +
                "usrId VARCHAR(50) NOT NULL," +
                "usrAccountCreated DATETIME," +
                "usrMemberSince DATETIME," +
                "PRIMARY KEY (usrId));";

        String moderatorTable =
                "CREATE TABLE IF NOT EXISTS tblModerator(" +
                "modId BIGINT UNSIGNED NOT NULL AUTO_INCREMENT," +
                "modUserId  VARCHAR(50) NOT NULL," +
                "modActive boolean," +
                "CONSTRAINT `fk_moderator` FOREIGN KEY (modUserId) REFERENCES tblUser (usrId) ON DELETE CASCADE ON UPDATE RESTRICT," +
                "PRIMARY KEY (modId));";

        String punishmentTable =
                "CREATE TABLE IF NOT EXISTS tblPunishment(" +
                "    psmId BIGINT UNSIGNED NOT NULL AUTO_INCREMENT," +
                "    psmOffenderId VARCHAR(50) NOT NULL," +
                "    psmModeratorId BIGINT UNSIGNED NOT NULL," +
                "    psmType SMALLINT UNSIGNED NOT NULL," +
                "    psmTimestamp DATETIME," +
                "    CONSTRAINT `fk_offenders` FOREIGN KEY (psmOffenderId) REFERENCES tblUser (usrId) ON DELETE CASCADE ON UPDATE RESTRICT," +
                "    CONSTRAINT `fk_moderators` FOREIGN KEY (psmModeratorId) REFERENCES tblModerator (modId) ON DELETE CASCADE ON UPDATE RESTRICT," +
                "    CONSTRAINT `fk_types` FOREIGN KEY (psmType) REFERENCES tblPunishmentType (pstId) ON DELETE CASCADE ON UPDATE RESTRICT," +
                "    PRIMARY KEY (psmId));";

        String punishmentTypeTable =
                "CREATE TABLE IF NOT EXISTS tblPunishmentType(" +
                        "    pstId SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT," +
                        "    pstName VARCHAR(50) NOT NULL," +
                        "    Primary KEY (pstId));";

        String messageTable =
                "CREATE TABLE IF NOT EXISTS tblMessage(" +
                        "    msgId VARCHAR(100) NOT NULL," +
                        "    msgUserId VARCHAR(50) NOT NULL," +
                        "    msgContent VARCHAR(5000)," +
                        "    msgChannel VARCHAR(100) NOT NULL," +
                        "    CONSTRAINT `fk_user` FOREIGN KEY (msgUserId) REFERENCES tblUser (usrId) ON DELETE CASCADE ON UPDATE RESTRICT," +
                        "    PRIMARY KEY (msgId)" +
                        ");";

        String messageAttachmentsTable =
                "CREATE TABLE IF NOT EXISTS tblMessageAttachments(" +
                        "    msaId BIGINT UNSIGNED NOT NULL AUTO_INCREMENT," +
                        "    msaMsgId VARCHAR(100) Not NULL," +
                        "    msaLink VARCHAR(1000)," +
                        "    CONSTRAINT `fk_message-id` FOREIGN KEY (msaMsgId) REFERENCES tblMessage (msgId) ON DELETE CASCADE ON UPDATE RESTRICT," +
                        "    PRIMARY KEY (msaId)" +
                        ");";

        String nicknamesTable =
                "CREATE TABLE IF NOT EXISTS tblUserNicknames(" +
                        "    usnId BIGINT UNSIGNED NOT NULL AUTO_INCREMENT," +
                        "    usnUserId VARCHAR(50) NOT NULL," +
                        "    usnValue VARCHAR(1000) NOT NULL," +
                        "    CONSTRAINT `fk_user-nicknames` FOREIGN KEY (usnUserId) REFERENCES tblUser (usrId) ON DELETE CASCADE ON UPDATE RESTRICT," +
                        "    PRIMARY KEY (usnId)" +
                        ");";

        String roleUpdateTable =
                "CREATE TABLE IF NOT EXISTS tblRoleUpdates(" +
                        "    rluUserId VARCHAR(50) NOT NULL," +
                        "    rluRoleId VARCHAR(50) NOT NULL," +
                        "    CONSTRAINT `fk_user-roles` FOREIGN KEY (rluUserId) REFERENCES tblUser (usrId) ON DELETE CASCADE ON UPDATE RESTRICT," +
                        "    PRIMARY KEY (rluRoleId,rluUserId)" +
                        ");";


        try {
            Statement stmt = guildStatement(guild);
            stmt.executeUpdate(userTable);
            stmt.executeUpdate(moderatorTable);
            stmt.executeUpdate(punishmentTypeTable);
            stmt.executeUpdate(punishmentTable);
            stmt.executeUpdate(messageTable);
            stmt.executeUpdate(messageAttachmentsTable);
            stmt.executeUpdate(nicknamesTable);
            stmt.executeUpdate(roleUpdateTable);

            String punishmentTypes = "INSERT INTO tblPunishmentType (pstName) VALUES ('%s');";
            PunishmentType.getAllTypes().forEach(x->{
                try {
                    stmt.executeUpdate(String.format(punishmentTypes,x));
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            });
            stmt.close();

        }catch (SQLException e){
            logger.error(e.getMessage());
        }
    }


    public static boolean isUserNotInUserTable(Guild guild, @NotNull Member user){
        if (!sqlEnabled) return false;
        String statementString = String.format("SELECT COUNT(*) FROM tblUser WHERE usrId='%s';",user.getId());
        Integer count = countData(guild, statementString);
        if (count == null) return false;
        return count == 0;
    }

    public static boolean isUserNotInModeratorTable(Guild guild,Member member){
        if (!sqlEnabled) return false;
        String statementString = String.format("SELECT COUNT(*) FROM tblModerator WHERE modUserId='%s';",member.getId());
        Integer count = countData(guild, statementString);
        if (count == null) return false;
        return count == 0;
    }

    public static void putUserDataInUserTable(Guild guild, @NotNull Member member){
        if (!sqlEnabled) return;
        if (!isUserNotInUserTable(guild,member)) return;
        String creationTime = getSqlTimeStringFromDatetime(member.getTimeCreated());
        String joinTime = getSqlTimeStringFromDatetime(member.getTimeJoined());
        String insertString = String.format("INSERT INTO tblUser (usrId,usrAccountCreated,usrMemberSince) VALUES ('%s',%s,%s)", member.getId(),creationTime,joinTime);
        insertIn(guild,insertString);
    }

    public static void putDataInModeratorTable(Guild guild, @NotNull Member member){
        if (!sqlEnabled) return;
        String insertString = String.format("INSERT INTO tblModerator (modUserId,modActive) VALUES ('%s',true)",member.getId());
        insertIn(guild,insertString);
    }

    public static void putDataInPunishmentTable(Guild guild, @NotNull Member offender, @NotNull Member moderator, @NotNull PunishmentType type){
       if (!sqlEnabled) return;
       String insertString = String.format(
                "INSERT INTO tblPunishment (psmOffenderId,psmModeratorId,psmType,psmTimestamp) VALUES('%s'," +
                "(SELECT modId FROM tblModerator WHERE modUserId = '%s')," +
                "(SELECT pstId FROM tblPunishmentType WHERE pstName = '%s'),NOW());",
                offender.getId(),
                moderator.getId(),
                type.getName());
       insertIn(guild,insertString);
    }

    public static @Nullable JSONArray getPunishmentDataFromUser(Guild guild, User user, PunishmentType type){
       if (!sqlEnabled) return null;

       StringBuilder where = new StringBuilder().append(" WHERE psmOffenderId = '%s'");
       if (type != null) where.append(" AND psmType = (SELECT pstId FROM tblPunishmentType WHERE pstName = '").append(type.getName()).append("')");

       String sql = String.format("SELECT psmId,modUserId,pstName,psmTimestamp FROM tblPunishment " +
               "INNER JOIN tblPunishmentType ON tblPunishment.psmType = tblPunishmentType.pstId " +
               "INNER JOIN tblModerator ON tblPunishment.psmModeratorId = tblModerator.modId " + where +
               " ORDER BY psmId;"
               ,user.getId());

       ResultSet results;
       JSONArray result = new JSONArray();
       try {
           Statement stmt = guildStatement(guild);
           results = stmt.executeQuery(sql);
           while (results.next()){
               JSONObject obj = new JSONObject();
               obj.put("id",results.getString(1));
               obj.put("moderatorId",results.getString(2));
               obj.put("punishment",results.getString(3));
               obj.put("time",results.getString(4));
               result.put(obj);
           }
           stmt.close();
           return result;
       } catch (SQLException e) {
           logger.error(e.getMessage());
           return null;
       }
    }

    public static @NotNull Integer getPunishmentCaseIdFromGuild(Guild guild){
        if (!sqlEnabled) return 1;
        try {
            Statement stmt = guildStatement(guild);
            String sql = "SELECT COUNT (*) FROM tblPunishment;";
            ResultSet results = stmt.executeQuery(sql);
            results.last();
            stmt.close();
            return Integer.valueOf(results.getString(1));
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static void putDataInMessageTable(@NotNull Message message){
       if (!sqlEnabled) return;
       String insertString = String.format("INSERT INTO tblMessage (msgId,msgUserId,msgContent,msgChannel) VALUES ('%s','%s','%s','%s');",
               message.getId(),
               message.getAuthor().getId(),
               message.getContentRaw(),
               message.getChannel().getId());

       insertIn(message.getGuild(),insertString);
    }

    public static void putDataInMessageAttachmentsTable(@NotNull Message message){
       if (!sqlEnabled) return;
       if (message.getAttachments().isEmpty()) return;

       String insertString = "INSERT INTO tblMessageAttachments (msaMsgId,msaLink) VALUES ('" + message.getId() + "','%s');";


       message.getAttachments().forEach(x-> insertIn(message.getGuild(),String.format(insertString, MessageUtil.logAttachmentsOnStorageServer(x,message.getGuild()))));
    }

    public static void updateDataInMessageTable(Message message){
       if (!sqlEnabled) return;
       String insertString = String.format("UPDATE tblMessage SET msgContent = '%s' WHERE msgId = '%s';",
               message.getContentRaw(),
               message.getId());

       insertIn(message.getGuild(),insertString);
    }

    public static String getMessageContentFromMessageTable(Guild guild, String messageId){
        if (!sqlEnabled) return "";
        if (isMessageIdNotInMessageTable(guild,messageId)) return "";
        try {
            Statement stmt = guildStatement(guild);
            String sql = String.format("SELECT msgContent FROM tblMessage WHERE msgId ='%s';",messageId);
            ResultSet results = stmt.executeQuery(sql);
            results.last();
            stmt.close();
            return results.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getUserIdFromMessageTable(Guild guild, String messageId) {
        if (!sqlEnabled) return "";
        if (isMessageIdNotInMessageTable(guild,messageId)) return "";
        try {
            Statement stmt = guildStatement(guild);
            String sql = String.format("SELECT msgUserId FROM tblMessage WHERE msgId ='%s';",messageId);
            ResultSet results = stmt.executeQuery(sql);
            results.last();
            stmt.close();
            return results.getString(1);
        } catch (Exception e) {
            return "";
        }
    }

    public static @NotNull List<String> getAttachmentsFromMessage(Guild guild, String messageId) {
       if (!sqlEnabled) return new ArrayList<>();
       try {
           Statement stmt = guildStatement(guild);
           ResultSet results;
           String queryString = String.format("SELECT msaLink FROM tblMessageAttachments WHERE msaMsgId = '%s'",messageId);
           results = stmt.executeQuery(queryString);
           stmt.close();
           return new ArrayList<>(){{
               while (results.next()){
                   add(results.getString(1));
               }
           }};
       } catch (SQLException e) {
           logger.error(e.getMessage());
           return new ArrayList<>();
       }
    }

    public static boolean isMessageIdNotInMessageTable(Guild guild,String messageId) {
        if (!sqlEnabled) return false;
        String statementString = String.format("SELECT COUNT(*) FROM tblMessage WHERE msgId='%s';",messageId);
        Integer count = countData(guild, statementString);
        if (count == null) return false;
        return count == 0;
    }

    public static void putNicknameInNicknameTable(GuildMemberUpdateNicknameEvent event){
       if (!sqlEnabled) return;

       if (isUserNotInUserTable(event.getGuild(),event.getMember())) putUserDataInUserTable(event.getGuild(),event.getMember());

       String insertString = String.format("INSERT INTO tblUserNicknames (usnUserId,usnValue) VALUES ('%s','%s');",event.getUser().getId(),event.getMember().getNickname());
        insertIn(event.getGuild(),insertString);
   }

   public static @Nullable String getLatestNickname(GuildMemberJoinEvent event){
       if (!sqlEnabled) return null;

       if (isUserNotInUserTable(event.getGuild(),event.getMember())) putUserDataInUserTable(event.getGuild(),event.getMember());

       if (isUserIdNotInNicknameTable(event)) return null;

       try {
           Statement stmt = guildStatement(event.getGuild());
           ResultSet results;
           String queryString =String.format("SELECT usnValue FROM tblUserNicknames WHERE usnUserId = '%s' ORDER BY usnId;",event.getUser().getId());
           results = stmt.executeQuery(queryString);
           results.last();
           String last = results.getString(1);
           stmt.close();
           return last;
       } catch (SQLException e) {
           logger.error(e.getMessage());
           return null;
       }

   }

   private static boolean isUserIdNotInNicknameTable(GuildMemberJoinEvent event){
       if (!sqlEnabled) return false;
       String statementString = String.format("SELECT COUNT(*) FROM tblUserNicknames WHERE usnUserId='%s';",event.getUser().getId());
       Integer count = countData(event.getGuild(), statementString);
       if (count == null) return false;
       return count == 0;
   }

   public static void putDataInRoleUpdateTable(Guild guild, Member member, List<Role> roles){
       if (!sqlEnabled) return;
       String userId = member.getId();
       putUserDataInUserTable(guild,member);
       roles.forEach(role->{
           if (isDataInRoleUpdateTable(guild,member,role)) return;
           String insertString = String.format("INSERT INTO tblRoleUpdates (rluUserId,rluRoleId) VALUES ('%s','%s');",userId,role.getId());
           insertIn(guild, insertString);
       });
   }

   public static void removeDataFromRoleUpdateTable(GuildMemberRoleRemoveEvent event){
       if (!sqlEnabled) return;
       String userId = event.getUser().getId();
       putUserDataInUserTable(event.getGuild(), event.getMember());

       event.getRoles().forEach(role->{
           String insertString = String.format("DELETE FROM tblRoleUpdates WHERE  rluUserId='%s' AND rluRoleId='%s';",userId,role.getId());
           insertIn(event.getGuild(), insertString);
       });
   }

   public static List<String> getRolesFromUser(User user,Guild guild){
       if (!sqlEnabled) return new ArrayList<>();
       List<String> roles = new ArrayList<>();
       try {
           Statement stmt = guildStatement(guild);
           String query = String.format("SELECT rluRoleId FROM tblRoleUpdates WHERE rluUserId='%s';",user.getId());
           ResultSet results;
           results = stmt.executeQuery(query);
           while (results.next()){
               roles.add(results.getString(1));
           }
           stmt.close();
           return roles;
       } catch (SQLException e) {
           logger.error(e.getMessage());
           return new ArrayList<>();
       }
   }

   private static boolean isDataInRoleUpdateTable(Guild guild,Member member,Role role){
       if (!sqlEnabled) return true;
       String query = String.format("SELECT COUNT (*) FROM tblRoleUpdates WHERE rluUserId='%s' AND rluRoleId='%s';",member.getId(),role.getId());
       Integer count = countData(guild,query);
       if (count == null) return true;
       return count > 0;
   }

   private static @Nullable Integer countData(Guild guild, @NotNull String query){
       if (!query.startsWith("SELECT COUNT")){
           logger.error("Wrong query-string for count-data");
           return null;
       }
       try {
           Statement stmt = guildStatement(guild);
           ResultSet result = stmt.executeQuery(query);
           result.next();
           int i = result.getInt(1);
           stmt.close();
           return i;
       } catch (SQLException e) {
           logger.error(e.getMessage());
           return null;
       }
   }

}
