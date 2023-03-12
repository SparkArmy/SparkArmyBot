package de.SparkArmy;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.LoggingController;
import de.SparkArmy.springApplication.SpringApp;
import de.SparkArmy.util.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;

public class Main {

    private JDA jda;

    private final Logger logger;
    private final ConfigController controller;
    private final EventWaiter waiter;
    private final Guild storageServer;


    public Main() {        // Initialize Logger variables
        this.logger = LoggingController.logger;
        Utils.logger = this.logger;

        // Initialize ConfigController
        this.controller = new ConfigController(this);
        Utils.controller = this.controller;
        JSONObject mainConfig = controller.getMainConfigFile();
        Utils.mainConfig = mainConfig;

        // Start building JDA
        JDABuilder jdaBuilder = JDABuilder.createDefault(mainConfig.getJSONObject("discord").getString("discord-token"));
        jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdaBuilder.enableIntents(GatewayIntent.GUILD_PRESENCES);
        jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        jdaBuilder.enableCache(CacheFlag.getPrivileged());
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
        logger.info("JDA-Builder was successful initialized");

        try {
            this.jda = jdaBuilder.build();
            logger.info("JDA successful build");
        } catch (Exception e) {
            logger.error("JDA Failed to build  - " + e.getMessage());
            System.exit(1);
        }

        // Add a EventWaiter
        this.waiter = new EventWaiter();
        Utils.waiter = waiter;
        jda.addEventListener(waiter);

        try{
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            this.logger.error(e.getMessage());
            this.systemExit(1);
        }

        // Add a static JDA
        Utils.jda = this.jda;


        // Get StorageServer
        this.storageServer = jda.getGuildById(controller.getMainConfigFile().getJSONObject("otherKeys").getString("storage-server"));
        Utils.storageServer = this.storageServer;
        if (this.storageServer == null) {
            logger.warn("No storage-server registered or the bot is not on storage-server");
        }

        // Start spring
        this.controller.preCreateSpringConfig();
        SpringApplication.run(SpringApp.class, "");


        Utils.logger.info("I`m ready.");

    }

    public static void main(String[] args) {
        new Main();
    }

    public void systemExit(Integer code) {
        if (null != this.jda) {
            this.jda.shutdown();
            try {
                this.jda.awaitShutdown();
            } catch (InterruptedException ignored) {
            }
        }
        System.exit(code);
    }


    // Getter
    public JDA getJda() {
        return this.jda;
    }

    public ConfigController getController() {
        return controller;
    }

    public Logger getLogger() {
        return logger;
    }

    public EventWaiter getWaiter() {
        return waiter;
    }

    public Guild getStorageServer() {
        return storageServer;
    }
}
