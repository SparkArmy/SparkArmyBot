package de.SparkArmy.util;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.util.customTypes.LogChannelType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class Utils {
    public static Logger logger;
    public static ConfigController controller;
    public static JDA jda;

    public static void handleSQLExeptions(@NotNull SQLException e) {
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

    public static Guild getStorageServer() {
        // Get StorageServer
        Guild storageServer = jda.getGuildById(controller.getMainConfigFile().getJSONObject("otherKeys").getString("storage-server"));
        if (storageServer == null) {
            logger.warn("No storage-server found");
        }
        return storageServer;
    }

    public static @NotNull ChannelAction<Category> createLogChannelCategory(@NotNull Guild guild) {
        return guild.createCategory("log-channel")
                .addRolePermissionOverride(guild.getPublicRole().getIdLong(), 0, Permission.VIEW_CHANNEL.getRawValue());
    }

    public static @NotNull ChannelAction<TextChannel> createLogChannel(@NotNull Category c, @NotNull LogChannelType type) {
        return c.createTextChannel(type.getName()).syncPermissionOverrides();
    }

}
