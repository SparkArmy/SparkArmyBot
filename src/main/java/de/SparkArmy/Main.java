package de.SparkArmy;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.LoggingController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.JdaFramework;
import de.SparkArmy.springApplication.SpringApp;
import de.SparkArmy.utils.Util;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;

public class Main {

    private final Logger logger;
    private final ConfigController controller;

    private final JdaFramework jdaFramework;
    private final Postgres postgres;


    public Main() {        // Initialize Logger variables
        this.logger = LoggingController.logger;
        Util.logger = this.logger;

        // Initialize ConfigController
        this.controller = new ConfigController(this);
        Util.controller = this.controller;

        this.postgres = new Postgres(this);
        this.jdaFramework = new JdaFramework(this);

        // Start spring
        this.controller.preCreateSpringConfig();
        SpringApplication.run(SpringApp.class, "");


        Util.logger.info("I`m ready.");


    }

    public static void main(String[] args) {
        new Main();
    }

    public void systemExit(Integer code) {
        if (null != this.jdaFramework) {
            this.jdaFramework.getJda().shutdown();
            try {
                this.jdaFramework.getJda().awaitShutdown();
            } catch (InterruptedException ignored) {
            }
        }
        System.exit(code);
    }


    // Getter
    public JdaFramework getJdaFramework() {
        return this.jdaFramework;
    }

    public ConfigController getController() {
        return controller;
    }
    public Logger getLogger() {
        return logger;
    }
    public Postgres getPostgres() {
        return postgres;
    }
}
