package de.SparkArmy.jda;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookCluster;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.utils.LogChannelType;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class WebhookApi {

    private final WebhookCluster webhookCluster;
    private final ConfigController controller;

    public WebhookApi(@NotNull JdaApi jdaApi) {
        this.controller = jdaApi.getController();

        this.webhookCluster = new WebhookCluster();
        getWebhookUrlsFromDatabase();
    }

    private void getWebhookUrlsFromDatabase() {
        Collection<WebhookClient> clients = controller.getLoggingChannelWebhookUrls().stream().map(WebhookClient::withUrl).toList();
        webhookCluster.addWebhooks(clients);
    }


    public WebhookCluster getWebhookCluster() {
        return webhookCluster;
    }

    public WebhookClient getSpecificWebhookClient(Guild guild, LogChannelType channelType) {
        String url = controller.getGuildLoggingChannelUrl(channelType, guild);
        return webhookCluster.getWebhooks().stream().filter(x -> x.getUrl().equals(url)).toList().get(0);
    }

    public void addWebhookClientToCluster(String url) {
        WebhookClient client = WebhookClient.withUrl(url);
        webhookCluster.addWebhooks(client);
    }
}
