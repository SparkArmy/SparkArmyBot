package de.sparkarmy.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.sparkarmy.Main;
import de.sparkarmy.config.Database;
import de.sparkarmy.utils.Util;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseSource {
    private static final Main main = Util.controller.main();
    private static final Logger logger = main.getLogger();
    private static final Database databaseConfig = main.getController().getConfig().database();

    private static final HikariConfig hikariConfig = new HikariConfig();
    private static final HikariDataSource hikariDataSource;

    static {

        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        if (databaseConfig.url().isEmpty()) {
            logger.error("postgres-url is empty");
            main.systemExit(110);
        } else if (databaseConfig.user().isEmpty()) {
            logger.error("postgres-user is empty");
            main.systemExit(111);
        } else if (databaseConfig.password().isEmpty()) {
            logger.error("postgres-password is empty");
            main.systemExit(112);
        }

        pgSimpleDataSource.setUrl("jdbc:postgresql://" + databaseConfig.url());
        pgSimpleDataSource.setUser(databaseConfig.user());
        pgSimpleDataSource.setPassword(databaseConfig.password());
        pgSimpleDataSource.setLoginTimeout(2);

        hikariConfig.setDataSource(pgSimpleDataSource);
        hikariConfig.setInitializationFailTimeout(0);
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


