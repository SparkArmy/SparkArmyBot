package de.sparkarmy;

import de.sparkarmy.config.Config;
import de.sparkarmy.config.ConfigKt;
import de.sparkarmy.config.DatabaseSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final Config config = ConfigKt.readConfig(true);
    private final DatabaseSource databaseSource = new DatabaseSource(config);

    private Main() {

    }

    public static void main(String[] args) {
        LOGGER.info("Main start requested");
        new Main();
    }

    public Config getConfig() {
        return config;
    }

    public DatabaseSource getDatabaseSource() {
        return databaseSource;
    }
}
