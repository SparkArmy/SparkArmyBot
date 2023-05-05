package de.SparkArmy.jda;

import de.SparkArmy.Main;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.jda.utils.CommandRegisterer;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

public class JdaApi {


    private JDA jda;
    private final Logger logger;
    private final ConfigController controller;
    private final CommandRegisterer commandRegisterer;

    public JdaApi(@NotNull Main main) {
        this.controller = main.getController();
        this.logger = main.getLogger();

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
            main.systemExit(1);
        }

        try {
            this.jda.awaitReady();
        } catch (InterruptedException e) {
            this.logger.error(e.getMessage());
            main.systemExit(1);
        }

        // Add Command Handler and EventHandler
        this.jda.setEventManager(new AnnotatedEventManager());
        this.jda.addEventListener(new EventDispatcher(this));


        this.commandRegisterer = new CommandRegisterer(this);

        // Add a static JDA
        Util.jda = this.jda;
    }

    public JDA getJda() {
        return jda;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigController getController() {
        return controller;
    }

    public CommandRegisterer getCommandRegisterer() {
        return commandRegisterer;
    }
}
