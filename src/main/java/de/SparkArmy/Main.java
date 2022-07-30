package de.SparkArmy;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.LoggerController;
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


    private final LoggerController loggerController;
    private final ConfigController controller;

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
            JDA jda = builder.build();
            logger.info("JDA successful build");
        }catch (Exception e){
            logger.severe("Failed to build  - " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        new Main();
        MainUtil.logger.info("I`m ready.");
    }

    public LoggerController getLoggerController() {
        return loggerController;
    }

    public ConfigController getController() {
        return controller;
    }

    public static void systemExit(Integer code){
        for (Handler f : MainUtil.logger.getHandlers()){
            f.close();
        }
        System.exit(code);
    }
}
