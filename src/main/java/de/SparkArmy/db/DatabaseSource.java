package de.SparkArmy.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.SparkArmy.Main;
import de.SparkArmy.utils.Util;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseSource {
    private static final Main main = Util.controller.getMain();
    private static final Logger logger = main.getLogger();
    private static final JSONObject mainConfig = main.getController().getMainConfigFile();

    private static final HikariConfig hikariConfig = new HikariConfig();
    private static final HikariDataSource hikariDataSource;

    static {

        JSONObject postgresConfig = mainConfig.getJSONObject("postgres");
        if (postgresConfig.getString("url").isEmpty()) {
            logger.error("postgres-url is empty");
            main.systemExit(110);
        } else if (postgresConfig.getString("user").isEmpty()) {
            logger.error("postgres-user is empty");
            main.systemExit(111);
        } else if (postgresConfig.getString("password").isEmpty()) {
            logger.error("postgres-password is empty");
            main.systemExit(112);
        } else {
            hikariConfig.setJdbcUrl("jdbc:postgresql://" + postgresConfig.getString("url"));
            hikariConfig.setUsername(postgresConfig.getString("user"));
            hikariConfig.setPassword(postgresConfig.getString("password"));
        }
        hikariConfig.setConnectionTimeout(2000);
        hikariConfig.setValidationTimeout(2000);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariDataSource = new HikariDataSource(hikariConfig);
    }


    public static Connection connection() throws SQLException {
        return hikariDataSource.getConnection();
    }
}


