package de.SparkArmy.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import de.SparkArmy.config.Discord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public class WebhookAppender extends AppenderBase<ILoggingEvent> {

    private final WebhookClient client;

    public WebhookAppender(@NotNull Discord discord) {
        this.client = new WebhookClientBuilder(discord.logWebhookUrl()).build();
        Logger logger = (Logger) LoggerFactory.getLogger("ROOT");
        logger.addAppender(this);
        super.start();
    }

    @Override
    protected void append(@NotNull ILoggingEvent eventObject) {
        client.send(eventObject.getMessage()); // TODO Prettier formating
    }
}
