package de.sparkarmy.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.sparkarmy.utils.ErrorCodes;
import de.sparkarmy.utils.FileHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public record Config(
        @JsonProperty("postgres") Database database,
        @JsonProperty("discord") Discord discord,
        @JsonProperty("twitch") Twitch twitch,
        @JsonProperty("youtube") Youtube youtube,
        @JsonProperty("virustotal-api-key") String virustotal
) {
    static final Logger logger = LoggerFactory.getLogger("Config");
    static final File configFolder = FileHandler.getDirectoryInUserDirectory("configs");

    public static Config getConfig() {
        File configFile = FileHandler.getFileInDirectory(configFolder, "main-config.json");
        if (!configFile.exists()) {
            logger.warn("The main-config.json-file not exist, we will created a new");

            if (copyConfigPreset(configFile)) {
                logger.debug("main-config.json was successful created");
                logger.warn("Please finish your configuration");
                try {
                    Desktop.getDesktop().open(FileHandler.getFileInDirectory(configFolder, "main-config.json"));
                } catch (Exception ignored) {
                    systemExit(ErrorCodes.GENERAL_CONFIG_ERROR.getId());
                }
            } else {
                logger.error(ErrorCodes.GENERAL_CONFIG_CANT_CREATE_MAIN_CONFIG.getDescription());
                systemExit(ErrorCodes.GENERAL_CONFIG_CANT_CREATE_MAIN_CONFIG.getId());
            }
            systemExit(0);
        }
        try {
            return new ObjectMapper().readValue(configFile, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static boolean copyConfigPreset(@NotNull File configFilePath) {
        try {
            InputStream is = Config.class.getResourceAsStream("/config.json");
            if (is == null) throw new IllegalStateException("Could not find config.json preset in resources");
            Files.copy(is, configFilePath.toPath());
            return true;
        } catch (IOException e) {
            logger.error("Error to copy configFile from resources", e);
            return false;
        }
    }

    static void systemExit(int exitCode) {
        System.exit(exitCode);
    }

}

