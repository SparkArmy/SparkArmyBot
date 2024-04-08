package de.SparkArmy.jda;

import de.SparkArmy.Main;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.EventManager;
import de.SparkArmy.jda.utils.CommandRegisterer;
import de.SparkArmy.jda.utils.ConfigureUtils;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

public class JdaApi extends ListenerAdapter {


    private final ShardManager shardManager;
    private final Logger logger;
    private final ConfigController controller;
    private final CommandRegisterer commandRegisterer;

    private final WebhookApi webhookApi;
    private final ConfigureUtils configureUtils;

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
        builder.setBulkDeleteSplittingEnabled(false);
//        builder.setEventManagerProvider(value -> new AnnotatedEventManager());
        builder.setEventPassthrough(true);
        logger.info("Shard-Builder was successful initialized");

        this.shardManager = builder.build();

        this.shardManager.addEventListener(/*new EventDispatcher(this),*/new EventManager(this));

        this.commandRegisterer = new CommandRegisterer(this);

        this.webhookApi = new WebhookApi(this);

        this.configureUtils = new ConfigureUtils();
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

    public WebhookApi getWebhookApi() {
        return webhookApi;
    }

    public ConfigureUtils getConfigureUtils() {
        return configureUtils;
    }
}
