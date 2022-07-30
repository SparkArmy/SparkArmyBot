package de.SparkArmy;

import de.SparkArmy.commandListener.CommandListenerRegisterer;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.LoggerController;
import de.SparkArmy.eventListener.EventListenerRegisterer;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.JSONObject;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class Main {


    @SuppressWarnings("FieldCanBeLocal")
    private final LoggerController loggerController;
    @SuppressWarnings("FieldCanBeLocal")
    private final ConfigController controller;

    private JDA jda;

    public Main() {
        // Initialize Logger variables
        this.loggerController = new LoggerController();
        Logger logger = loggerController.getLogger();
        MainUtil.logger = logger;

        // Initialize ConfigController variables and the mainConfig
        this.controller = new ConfigController(this);
        MainUtil.controller = controller;
        JSONObject mainConfig = controller.getMainConfigFile();
        MainUtil.mainConfig = mainConfig;

        // Start building JDA
        JDABuilder builder = JDABuilder.createDefault(mainConfig.getString("discord-token"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.enableIntents(GatewayIntent.GUILD_PRESENCES);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        builder.enableCache(CacheFlag.getPrivileged());
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        logger.info("JDA-Builder was successful initialized");

        try {
            jda = builder.build();
            logger.info("JDA successful build");
        } catch (Exception e) {
            logger.severe("Failed to build  - " + e.getMessage());
            System.exit(1);
        }

        // Add a static JDA
        MainUtil.jda = jda;


        // Add CommandListener to JDA
        new CommandListenerRegisterer();
        // Add EventListener to JDA
        new EventListenerRegisterer();

    }

    public static void main(String[] args) {
        new Main();
        MainUtil.logger.info("I`m ready.");
    }

    public static void systemExit(Integer code) {
        for (Handler f : MainUtil.logger.getHandlers()) {
            f.flush();
            f.close();
        }
        if (MainUtil.jda != null) MainUtil.jda.cancelRequests();
        System.exit(code);
    }
}
