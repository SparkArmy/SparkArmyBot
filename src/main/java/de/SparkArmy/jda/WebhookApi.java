package de.SparkArmy.jda;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookCluster;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.utils.LogChannelType;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collection;

@SuppressWarnings("ALL")
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
        String searchUrl = controller.getGuildLoggingChannelUrl(channelType, guild);
        logger.info(searchUrl);
        logger.info(String.valueOf(webhookCluster.getWebhooks().size()));
        return webhookCluster.getWebhooks().stream().filter(x -> {
            String webhookUrl = x.getUrl();
            return webhookUrl.equals(searchUrl);
        }).toList().getFirst();
    }

    public void addWebhookClientToCluster(String url) {
        WebhookClient client = WebhookClient.withUrl(url);
        webhookCluster.addWebhooks(client);
    }
}
