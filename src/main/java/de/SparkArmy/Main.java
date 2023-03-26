package de.SparkArmy;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.LoggingController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jdaEvents.customCommands.CommandDispatcher;
import de.SparkArmy.jdaEvents.customCommands.CommandRegisterer;
import de.SparkArmy.springApplication.SpringApp;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
    private final CommandRegisterer commandRegisterer;
    private final Postgres postgres;


    public Main() {        // Initialize Logger variables
        this.logger = LoggingController.logger;
        Util.logger = this.logger;

        // Initialize ConfigController
        this.controller = new ConfigController(this);
        Util.controller = this.controller;
        JSONObject mainConfig = controller.getMainConfigFile();

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
            systemExit(1);
        }

        this.postgres = new Postgres(this);

        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            this.logger.error(e.getMessage());
            this.systemExit(1);
        }

        // Add a EventWaiter
        this.waiter = new EventWaiter();
        jda.addEventListener(waiter);

        // Add Command Handler
        this.jda.addEventListener(new CommandDispatcher(this));

        // Add a static JDA
        Util.jda = this.jda;

        // Start spring
        this.controller.preCreateSpringConfig();
        SpringApplication.run(SpringApp.class, "");

        this.commandRegisterer = new CommandRegisterer(this);

        Util.logger.info("I`m ready.");


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


    public CommandRegisterer getCommandRegisterer() {
        return commandRegisterer;
    }

    public Postgres getPostgres() {
        return postgres;
    }
}
