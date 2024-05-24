package de.SparkArmy.utils;

import de.SparkArmy.config.ConfigController;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

public class Util {
    public static Logger logger;
    public static ConfigController controller;

    public static ResourceBundle getResourceBundle(String name, @NotNull DiscordLocale discordLocale) {
        // Get the resourceBundle-path
        String localizationBundle = String.format("LocalizationData/%s", name);
        Locale locale = Locale.UK;
        // Check if language german
        if (discordLocale.equals(DiscordLocale.GERMAN)) locale = Locale.GERMAN;
        return ResourceBundle.getBundle(localizationBundle, locale);
    }
}
