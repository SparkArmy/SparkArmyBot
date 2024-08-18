package de.sparkarmy;

import de.sparkarmy.config.Config;
import de.sparkarmy.config.ConfigController;
import de.sparkarmy.config.ConfigKt;
import de.sparkarmy.jda.JdaApi;
import de.sparkarmy.log.WebhookAppenderKt;
import de.sparkarmy.twitch.TwitchApi;
import de.sparkarmy.utils.Util;
import de.sparkarmy.webserver.SpringApp;
import de.sparkarmy.youtube.YouTubeApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

public class Main {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConfigController controller;

    private final JdaApi jdaApi;

    private final TwitchApi twitchApi;
    private final YouTubeApi youTubeApi;

    private final Config config = ConfigKt.readConfig(true);


    public Main() {
        logger.info("Main started");
        // Initialize Logger variables
        Util.logger = this.logger;
        WebhookAppenderKt.initWebhookLogger(config.getDiscord().getLog());

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
