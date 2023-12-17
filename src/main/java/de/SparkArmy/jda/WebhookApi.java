package de.SparkArmy.jda;

import club.minnced.discord.webhook.LibraryInfo;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookCluster;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.utils.LogChannelType;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class WebhookApi {

    private final WebhookCluster webhookCluster;
    private final ConfigController controller;
    private final Logger logger;

    public WebhookApi(@NotNull JdaApi jdaApi) {
        this.controller = jdaApi.getController();

        this.webhookCluster = new WebhookCluster();
        this.logger = controller.getMain().getLogger();
        getWebhookUrlsFromDatabase();
    }

    private void getWebhookUrlsFromDatabase() {
        Collection<WebhookClient> clients = controller.getLoggingChannelWebhookUrls().stream().map(x -> {
            logger.info(x);
            return WebhookClient.withUrl(x);
        }).toList();
        webhookCluster.addWebhooks(clients);
    }


    public WebhookCluster getWebhookCluster() {
        return webhookCluster;
    }

    public WebhookClient getSpecificWebhookClient(Guild guild, LogChannelType channelType) {
        String searchUrl = controller.getGuildLoggingChannelUrl(channelType, guild).replace(String.format("/v%s/", JDAInfo.DISCORD_REST_VERSION), String.format("/v%s/", LibraryInfo.DISCORD_API_VERSION));
        List<WebhookClient> clients = webhookCluster.getWebhooks().stream().filter(x -> {
            String webhookUrl = x.getUrl();
            return webhookUrl.equals(searchUrl);
        }).toList();
        logger.info(String.valueOf(clients));
        if (clients.isEmpty()) return null;
        return clients.getFirst();
    }

    public void addWebhookClientToCluster(String url) {
        WebhookClient client = WebhookClient.withUrl(url);
        webhookCluster.addWebhooks(client);
    }
}
