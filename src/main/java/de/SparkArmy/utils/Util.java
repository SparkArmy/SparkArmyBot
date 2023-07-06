package de.SparkArmy.utils;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.utils.LogChannelType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class Util {
    public static Logger logger;
    public static ConfigController controller;

    public static void handleSQLExceptions(@NotNull SQLException e) {
        logger.error(e.getMessage());
    }

    public static ResourceBundle getResourceBundle(String name, @NotNull DiscordLocale discordLocale) {
        // Get the resourceBundle-path
        String localizationBundle = String.format("LocalizationData/%s", name);
        Locale locale = Locale.UK;
        // Check if language german
        if (discordLocale.equals(DiscordLocale.GERMAN)) locale = Locale.GERMAN;
        return ResourceBundle.getBundle(localizationBundle, locale);
    }

    public static @NotNull ChannelAction<Category> createLogChannelCategory(@NotNull Guild guild) {
        return guild.createCategory("log-channel")
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(), 0, Permission.VIEW_CHANNEL.getRawValue());
    }

    public static @NotNull ChannelAction<TextChannel> createLogChannel(@NotNull Category c, @NotNull LogChannelType type) {
        return c.createTextChannel(type.getName()).syncPermissionOverrides();
    }

    public static void sendingModLogEmbed(WebhookEmbed embed, Guild guild) {
        String modLogName = LogChannelType.MOD.getName();
        // Create LogChannelConfig if none exist
        if (controller.getGuildMainConfig(guild).isNull("log-channel")) {
            controller.createLogChannelConfig(guild);
        }

        final JSONObject guildConfig = controller.getGuildMainConfig(guild);
        JSONObject logConfig = guildConfig.getJSONObject("log-channel");

        AtomicReference<String> webhookUrl = new AtomicReference<>();

        if (logConfig.isNull("category") || logConfig.getString("category").isBlank()) {
            Util.createLogChannelCategory(guild)
                    .onSuccess(category -> logConfig.put("category", category.getId()))
                    .flatMap(category -> Util.createLogChannel(category, LogChannelType.MOD))
                    .onSuccess(channel -> logConfig.getJSONObject(modLogName).put("channelId", channel.getId()))
                    .flatMap(channel -> channel.createWebhook(guild.getJDA().getSelfUser().getName()))
                    .queue(webhook -> {
                        logConfig.getJSONObject(modLogName).put("webhookUrl", webhook.getUrl());
                        guildConfig.put("log-channel", logConfig);
                        controller.writeInGuildMainConfig(guild, guildConfig);
                        webhookUrl.set(webhook.getUrl());
                    });
        } else {
            Category category = guild.getCategoryById(logConfig.getString("category"));
            if (category == null) {
                Util.createLogChannelCategory(guild)
                        .onSuccess(c -> logConfig.put("category", c.getId()))
                        .flatMap(c -> Util.createLogChannel(c, LogChannelType.MOD))
                        .onSuccess(channel -> logConfig.getJSONObject(modLogName).put("channelId", channel.getId()))
                        .flatMap(channel -> channel.createWebhook(guild.getJDA().getSelfUser().getName()))
                        .queue(webhook -> {
                            logConfig.getJSONObject(modLogName).put("webhookUrl", webhook.getUrl());
                            guildConfig.put("log-channel", logConfig);
                            controller.writeInGuildMainConfig(guild, guildConfig);
                            webhookUrl.set(webhook.getUrl());
                        });
            } else {
                if (logConfig.getJSONObject(modLogName).isEmpty() || logConfig.getJSONObject(modLogName).getString("channelId").isBlank() || logConfig.getJSONObject(modLogName).getString("webhookUrl").isBlank()) {
                    Util.createLogChannel(category, LogChannelType.MOD)
                            .onSuccess(channel -> logConfig.getJSONObject(modLogName).put("channelId", channel.getId()))
                            .flatMap(channel -> channel.createWebhook(guild.getJDA().getSelfUser().getName()))
                            .queue(webhook -> {
                                logConfig.getJSONObject(modLogName).put("webhookUrl", webhook.getUrl());
                                guildConfig.put("log-channel", logConfig);
                                controller.writeInGuildMainConfig(guild, guildConfig);
                                webhookUrl.set(webhook.getUrl());
                            });
                } else {
                    webhookUrl.set(logConfig.getJSONObject(modLogName).getString("webhookUrl"));
                }
            }
        }

        try {
            WebhookClient client = WebhookClient.withUrl(webhookUrl.get());
            client.send(embed);
            client.close();
        } catch (NullPointerException | NumberFormatException ignored) {
        }
    }

}
