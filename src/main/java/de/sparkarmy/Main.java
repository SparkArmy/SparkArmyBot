package de.sparkarmy;

import de.sparkarmy.config.Config;
import de.sparkarmy.config.ConfigKt;
import de.sparkarmy.config.DatabaseSource;
import de.sparkarmy.data.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static Main instance;

    private final Config config = ConfigKt.readConfig(true);
    private final DatabaseSource databaseSource;
    private final Repository repository;

    private Main() {
        // Create and get Database
        this.databaseSource = new DatabaseSource(config);
        this.repository = new Repository(databaseSource);


        // Get static instance
        instance = this;
    }

    private static Main getInstance() {
        return instance;
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

    public Repository getRepository() {
        return repository;
    }
}
