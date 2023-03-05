package de.SparkArmy;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import de.SparkArmy.commandListener.CommandDispatcher;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.eventListener.EventListenerRegisterer;
import de.SparkArmy.springBoot.LoggingController;
import de.SparkArmy.springBoot.SpringApp;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.PostgresConnection;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;

public class Main {


    public JDA jda;

    public final Logger logger;
    public final ConfigController controller;
    public final EventWaiter waiter;

    public Main() {
        // Initialize Logger variables
        this.logger = LoggingController.logger;
        MainUtil.logger = logger;

        // Initialize ConfigController variables and the mainConfig
        this.controller = new ConfigController(this);
        MainUtil.controller = controller;
        JSONObject mainConfig = controller.getMainConfigFile();
        MainUtil.mainConfig = mainConfig;

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
        MainUtil.waiter = waiter;
        jda.addEventListener(waiter);

        try{
            jda.awaitReady();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Main.systemExit(1);
        }

        // Add a static JDA
        MainUtil.jda = this.jda;


        // Get StorageServer
        MainUtil.storageServer = jda.getGuildById(controller.getMainConfigFile().getJSONObject("otherKeys").getString("storage-server"));
        if (MainUtil.storageServer == null) {
            logger.warn("No storage-server registered or the bot is not on storage-server");
        }

//        CommandRegisterer.registerCommands();

        jda.addEventListener(new CommandDispatcher(this));

        // Add EventListener to JDA
        new EventListenerRegisterer();

    }

    public static void main(String[] args) {
        ConfigController.preCreateSpringConfig();
        SpringApplication.run(SpringApp.class,"");
        new Main();
        PostgresConnection.checkPreconditions();
        MainUtil.logger.info("I`m ready.");
    }

    public static void systemExit(Integer code) {
        if (null != MainUtil.jda){
            MainUtil.jda.shutdown();
            try {
                MainUtil.jda.awaitStatus(JDA.Status.SHUTDOWN);
            } catch (InterruptedException ignored) {}
        }
        System.exit(code);
    }

}
