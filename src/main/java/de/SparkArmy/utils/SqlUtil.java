package de.SparkArmy.utils;

import de.SparkArmy.utils.punishmentUtils.PunishmentType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.sql.*;
import java.time.OffsetDateTime;

@SuppressWarnings("unused")
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
    private static Statement statement(@NotNull Guild guild) throws SQLException,NullPointerException {
        return connection("D" + guild.getId()).createStatement();
    }

    private static String getSqlTimeStringFromDatetime(@NotNull OffsetDateTime time){
        String timeString = time.toString().replace("T", " ").replace("Z","");
        String formatString = "'%Y-%m-%d %H:%i:%s.%f'";
        return String.format("STR_TO_DATE('%s',%s)",timeString,formatString);
    }

    private static boolean createDatabase(String databaseName){
        try {
            Statement stmt = statement();
            String sql = "CREATE DATABASE IF NOT EXISTS D" + databaseName;
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
         return false;
        }
    }


    public static void createDatabaseAndTablesForGuild(@NotNull Guild guild){
        if (!sqlEnabled) return;
        String guildId = guild.getId();
        if (!createDatabase(guildId)){
            logger.error("Faild to create Database for " + guildId);
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


        try {
            Statement stmt = statement(guild);
            stmt.executeUpdate(userTable);
            stmt.executeUpdate(moderatorTable);
            stmt.executeUpdate(punishmentTypeTable);
            stmt.executeUpdate(punishmentTable);

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

    public static boolean isUserNotInTable(Guild guild, @NotNull Member user){
        if (!sqlEnabled) return false;
        try {
            Statement stmt = statement(guild);
            ResultSet results;
            String statementString = String.format("SELECT COUNT (*) FROM tblUser WHERE usrId='%s';",user.getId());
            results = stmt.executeQuery(statementString);
            int n = 0;
            if (results.next()) n = results.getInt(1);
            stmt.close();
            return n == 0;
        }catch (SQLException e){
            logger.error(e.getMessage());
            return false;
        }
    }

    public static void putUserDataInUserTable(Guild guild, @NotNull Member member){
        if (!sqlEnabled) return;
        try {
            Statement stmt = statement(guild);
            String creationTime = getSqlTimeStringFromDatetime(member.getTimeCreated());
            String joinTime = getSqlTimeStringFromDatetime(member.getTimeJoined());
            String insertString = String.format("INSERT INTO tblUser (usrId,usrAccountCreated,usrMemberSince) VALUES ('%s',%s,%s)", member.getId(),creationTime,joinTime);
            stmt.executeUpdate(insertString);
            stmt.close();
        } catch (SQLException e) {
            logger.error("putUserTable: " + e.getMessage());
        }
    }

    public static void putDataInModeratorTable(Guild guild, @NotNull Member member){
        if (!sqlEnabled) return;
        try {
            Statement stmt = statement(guild);
            String insertString = String.format("INSERT INTO tblModerator (modUserId) VALUES ('%s',true)",member.getId());
            stmt.executeUpdate(insertString);
            stmt.close();
        } catch (SQLException e) {
            logger.error("putModeratorTable: " + e.getMessage());
        }
    }

    public static void putDataInPunishmentTable(Guild guild, @NotNull Member offender, @NotNull Member moderator, @NotNull PunishmentType type){
       if (!sqlEnabled) return;
       try {
            Statement stmt = statement(guild);
            String insertString = String.format(
                    "INSERT INTO tblPunishment (psmOffenderId,psmModeratorId,psmType,psmTimestamp) VALUES('%s'," +
                    "(SELECT modId FROM tblModerator WHERE modUserId = '%s')," +
                    "(SELECT pstId FROM tblPunishmentType WHERE pstName = '%s'),NOW());",
                    offender.getId(),
                    moderator.getId(),
                    type.getName());
            stmt.executeUpdate(insertString);
            stmt.close();
        } catch (SQLException e) {
            logger.error("putPunishmentTable: " + e.getMessage());
        }
    }

    public static @NotNull Integer getPunishmentCaseIdFromGuild(Guild guild){
        if (!sqlEnabled) return 1;
        try {
            Statement stmt = statement(guild);
            String sql = "SELECT COUNT (*) FROM tblPunishment;";
            ResultSet results = stmt.executeQuery(sql);
            results.last();
            stmt.close();
            return Integer.valueOf(results.getString(1));
        } catch (Exception e) {
            return 1;
        }
    }
}
