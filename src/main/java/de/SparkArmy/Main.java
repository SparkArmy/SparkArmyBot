package de.SparkArmy;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import de.SparkArmy.commandListener.CommandListenerRegisterer;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.LoggerController;
import de.SparkArmy.eventListener.EventListenerRegisterer;
import de.SparkArmy.springBoot.SpringApp;
import de.SparkArmy.timedOperations.TimedOperationsExecutor;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class Main {


    @SuppressWarnings("FieldCanBeLocal")
    private final LoggerController loggerController;
    @SuppressWarnings("FieldCanBeLocal")
    private final ConfigController controller;
    @SuppressWarnings("FieldCanBeLocal")
    private final EventWaiter waiter;

    @SuppressWarnings("FieldCanBeLocal")
    private final TimedOperationsExecutor timedOperations;

    private JDA jda;

    public Main() {
        // Initialize Logger variables
        loggerController = new LoggerController();
        Logger logger = this.loggerController.getLogger();
        MainUtil.logger = logger;

        // Initialize ConfigController variables and the mainConfig
        controller = new ConfigController(this);
        MainUtil.controller = this.controller;
        JSONObject mainConfig = this.controller.getMainConfigFile();
        MainUtil.mainConfig = mainConfig;

        // Start building JDA
        JDABuilder builder = JDABuilder.createDefault(mainConfig.getJSONObject("discord").getString("discord-token"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.enableIntents(GatewayIntent.GUILD_PRESENCES);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        builder.enableCache(CacheFlag.getPrivileged());
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        logger.info("JDA-Builder was successful initialized");

        try {
            this.jda = builder.build();
            logger.info("JDA successful build");
        } catch (Exception e) {
            logger.severe("Failed to build  - " + e.getMessage());
            System.exit(1);
        }

        // Add a EventWaiter
        this.waiter = new EventWaiter();
        MainUtil.waiter = waiter;
        jda.addEventListener(waiter);

        try{
            jda.awaitReady();
        } catch (InterruptedException e) {
            logger.severe(e.getMessage());
            Main.systemExit(1);
        }

        // Add a static JDA
        MainUtil.jda = this.jda;

        // Initialize TimedOperations
        this.timedOperations = new TimedOperationsExecutor();
        MainUtil.timedOperations = timedOperations;

        // Get StorageServer
        MainUtil.storageServer = jda.getGuildById(controller.getMainConfigFile().getJSONObject("otherKeys").getString("storage-server"));
        if (MainUtil.storageServer == null){
            logger.warning("No storage-server registered or The bot is not on storage-server");
        }

//        CommandRegisterer.registerGuildSlashCommands(jda.getGuildById("890674837461278730"));

        // Add CommandListener to JDA
        new CommandListenerRegisterer();
        // Add EventListener to JDA
        new EventListenerRegisterer();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringApp.class,"");
        new Main();
        MainUtil.logger.info("I`m ready.");
    }

    public static void systemExit(Integer code) {
        for (Handler f : MainUtil.logger.getHandlers()) {
            f.flush();
            f.close();
        }
        if (null != MainUtil.jda) MainUtil.jda.cancelRequests();
        System.exit(code);
    }

}
