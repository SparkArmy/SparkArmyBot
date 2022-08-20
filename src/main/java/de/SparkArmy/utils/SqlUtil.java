package de.SparkArmy.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.sql.*;

@SuppressWarnings("unused")
public class SqlUtil {


    private static java.sql.@Nullable Connection connection(String database){
        JSONObject mariaDbConfig = MainUtil.controller.getMainConfigFile().getJSONObject("mariaDbConnection");
        Connection c;
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            c = DriverManager.getConnection("jdbc:mariadb:" + mariaDbConfig.getString("url") + database,mariaDbConfig.getString("user"),mariaDbConfig.getString("password"));
        } catch (SQLException | ClassNotFoundException e) {
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
        return connection(guild.getId()).createStatement();
    }

    public static boolean createDatabase(String databaseName){
        try {
            Statement stmt = statement();
            String sql = SqlStrings.CREATE_DATABASE + SqlStrings.IF_NOT_EXIST + databaseName;
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
         return false;
        }
    }

    public static void createTablesInNewDatabase(String databaseName){
        String userTable = SqlStrings.CREATE_TABLE + SqlStrings.IF_NOT_EXIST + "tblUsers(usrId varchar NOT NULL,usrAccountCreated DATETIME,usrMemberSince DATETIME, PRIMARY KEY (usrId)";
        String moderatorTable = SqlStrings.CREATE_TABLE + SqlStrings.IF_NOT_EXIST + "tblModerators(";

    }

    public static @NotNull Integer getPunishmentCaseIdFromGuild(Guild guild){
        try {
            Statement stmt = statement(guild);
            String sql = SqlStrings.SELECT_COUNT + "(*) FROM tblPunishments;";
            ResultSet results = stmt.executeQuery(sql);
            results.last();
            stmt.close();
            return Integer.valueOf(results.getString(1));
        } catch (Exception e) {
            return 1;
        }
    }

    public static boolean writePunishmentDataInPunishmentsTable(int Type, Guild guild, User offender, User moderator, String reason){
        String sql = SqlStrings.INSERT_INTO + "tbl";
        return false;
    }

    private enum SqlStrings{
        ;
        // Table operations
            // Create a new table
            static final String CREATE_TABLE = "CREATE TABLE ";
            // Delete rows
            static final String DELETE = "DELETE ";
            // Delete a table
            static final String DROP_TABLE = "DROP TABLE ";
            // Change the tables name
            static final String RENAME_TABLE = "RENAME TABLE ";

        // Table data operations
            // SQL statement used primarily for retrieving data from a MariaDB database
            static final String SELECT = "SELECT ";
            //SQL statement used for count rows
            static final String SELECT_COUNT = SELECT + "COUNT";
            // SQL statement to add table data
            static final String INSERT_INTO = "INSERT INTO ";

        // Database operations
            // Create a database
            static final String CREATE_DATABASE = "CREATE DATABASE ";

        // Check if an object exist
        static final String IF_NOT_EXIST = "IF NOT EXIST ";

    }
}
