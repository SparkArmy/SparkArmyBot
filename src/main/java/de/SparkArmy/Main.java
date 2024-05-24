package de.SparkArmy;

import de.SparkArmy.config.Config;
import de.SparkArmy.config.ConfigController;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.log.WebhookAppender;
import de.SparkArmy.twitch.TwitchApi;
import de.SparkArmy.utils.Util;
import de.SparkArmy.webserver.SpringApp;
import de.SparkArmy.youtube.YouTubeApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

public class Main {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConfigController controller;

    private final JdaApi jdaApi;

    private final TwitchApi twitchApi;
    private final YouTubeApi youTubeApi;

    private final Config config = Config.getConfig();


    public Main() {
        // Initialize Logger variables
        Util.logger = this.logger;
        new WebhookAppender(config.discord());

        // Initialize ConfigController
        this.controller = new ConfigController(this);
        Util.controller = this.controller;


        //Register Apis
        this.jdaApi = new JdaApi(this);
        this.twitchApi = new TwitchApi(this);
        this.youTubeApi = new YouTubeApi(this);

        // Start web server
        SpringApplication.run(SpringApp.class,"");
    }

    public static void main(String[] args) {
        new Main();
    }

    public void systemExit(Integer code) {
        if (this.twitchApi != null) {
            twitchApi.closeClient();
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

    public TwitchApi getTwitchApi() {
        return twitchApi;
    }

    public YouTubeApi getYouTubeApi() {
        return youTubeApi;
    }

    public Config getConfig() {
        return config;
    }
}
