package de.SparkArmy;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.twitch.TwitchApi;
import de.SparkArmy.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private final Logger logger;
    private final ConfigController controller;

    private final JdaApi jdaApi;

    private final TwitchApi twitchApi;
    private final Postgres postgres;


    public Main() {        // Initialize Logger variables
        this.logger = LoggerFactory.getLogger(this.getClass());
        Util.logger = this.logger;

        // Initialize ConfigController
        this.controller = new ConfigController(this);
        Util.controller = this.controller;

        this.postgres = new Postgres(this);

        //Register Apis
        this.jdaApi = new JdaApi(this);
        this.twitchApi = new TwitchApi(this);

        logger.info("I`m ready.");

    }

    public static void main(String[] args) {
        new Main();
    }

    public void systemExit(Integer code) {
        if (null != this.jdaApi.getJda()) {
            this.jdaApi.getJda().shutdown();
            try {
                this.jdaApi.getJda().awaitShutdown();
            } catch (InterruptedException ignored) {
            }
        }
        System.exit(code);
    }


    // Getter
    public JdaApi getJdaApi() {
        return this.jdaApi;
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

    public TwitchApi getTwitchApi() {
        return twitchApi;
    }
}
