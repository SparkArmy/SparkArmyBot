package de.SparkArmy.jda;

import de.SparkArmy.Main;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.jda.utils.CommandRegisterer;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

public class JdaApi {


    private final ShardManager shardManager;
    private final Logger logger;
    private final ConfigController controller;
    private final CommandRegisterer commandRegisterer;

    public JdaApi(@NotNull Main main) {
        this.controller = main.getController();
        this.logger = main.getLogger();

        JSONObject mainConfig = controller.getMainConfigFile();

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createLight(mainConfig.getJSONObject("discord").getString("discord-token"));
        builder.enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MODERATION,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.GUILD_WEBHOOKS,
                GatewayIntent.GUILD_INVITES,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.SCHEDULED_EVENTS);
        builder.setMemberCachePolicy(MemberCachePolicy.NONE);
        builder.setEventManagerProvider(value -> new AnnotatedEventManager());
        logger.info("Shard-Builder was successful initialized");

        this.shardManager = builder.build();

        shardManager.addEventListener(new EventDispatcher(this));

        this.commandRegisterer = new CommandRegisterer(this);
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

    public ShardManager getShardManager() {
        return shardManager;
    }
}
