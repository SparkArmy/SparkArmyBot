package de.SparkArmy.util;

import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.DiscordLocale;
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

    public static ResourceBundle getResourceBundle(String commandName, @NotNull DiscordLocale discordLocale) {
        // Get the resourceBundle-path
        String localizationBundle = String.format("LocalizationData/%sCommand/%s", commandName, commandName);
        Locale locale = Locale.UK;
        // Check if language german
        if (discordLocale.equals(DiscordLocale.GERMAN)) locale = Locale.GERMAN;
        return ResourceBundle.getBundle(localizationBundle, locale);
    }

}
