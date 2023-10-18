package de.SparkArmy.controller;

import de.SparkArmy.Main;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.jda.utils.LogChannelType;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public class ConfigController {
    private final Main main;
    private final Logger logger;
    private final File configFolder = FileHandler.getDirectoryInUserDirectory("configs");

    public Main getMain() {
        return this.main;
    }

    public ConfigController(@NotNull Main main) {
        this.main = main;
        this.logger = main.getLogger();
    }

    public JSONObject getMainConfigFile(){
        if (!FileHandler.getFileInDirectory(this.configFolder,"main-config.json").exists()){
            this.logger.warn("The main-config.json-file not exist, we will created a new");
            JSONObject blankConfig = new JSONObject();
            JSONObject discord = new JSONObject(){{
                put("discord-token","Write here your Discord-Bot-Token");
                put("discord-client-id","Write here your Discord-Bot-ClientId");
            }};
            blankConfig.put("discord",discord);
            JSONObject twitch = new JSONObject(){{
                put("twitch-client-secret","[Optional] Write here your Twitch-Client-Secret");
                put("twitch-client-id","[Optional] Write here your Twitch-Client-Id");
            }};
            blankConfig.put("twitch",twitch);
            JSONObject youtube = new JSONObject(){{
                put("youtube-api-key","[Optional] Write here your API-Key from YouTube");
                put("spring-callback-url","[Optional] Your callback domain");
            }};
            blankConfig.put("youtube",youtube);
            JSONObject postgres = new JSONObject(){{
                put("url","The url for the database");
                put("user","Database-User");
                put("password", "User-Password");
            }};
            blankConfig.put("postgres", postgres);
            JSONObject otherKeys = new JSONObject() {{
                put("virustotal-api-key", "[Optional] Write here your API-Key from VirusTotal");
                put("storage-server", "Please setup a new server, delete all included channels and put the id in this field");
                put("twitter_bearer", "Please copy the twitter bearer in this field");
            }};
            blankConfig.put("otherKeys", otherKeys);

            if (FileHandler.writeValuesInFile(this.configFolder, "main-config.json", blankConfig)) {
                this.logger.info("main-config.json was successful created");
                this.logger.warn("Please finish your configuration");
                try {
                    Desktop.getDesktop().open(FileHandler.getFileInDirectory(this.configFolder, "main-config.json"));
                } catch (Exception ignored) {
                }
            } else {
                this.logger.error("Can't create a main-config.json");
                this.main.systemExit(1);
            }

            this.main.systemExit(0);
        }
        String mainConfigAsString = FileHandler.getFileContent(this.configFolder,"main-config.json");
        if (mainConfigAsString == null) {
            this.logger.error("The main-config.json is null");
            this.main.systemExit(12);
        }
        assert mainConfigAsString != null;
        return new JSONObject(mainConfigAsString);
    }

    private @Unmodifiable Collection<File> getGuildConfigs(@NotNull Guild guild){
        File directory = FileHandler.getDirectoryInUserDirectory("configs/" + guild.getId());
        if (0 == Objects.requireNonNull(directory.listFiles()).length){
            FileHandler.createFile(directory,"config.json");
            FileHandler.createFile(directory,"rules.json");
            FileHandler.createFile(directory, "highlighted-keywords.json");

            FileHandler.writeValuesInFile(directory, "config.json", this.guildConfigBlank());
            FileHandler.writeValuesInFile(directory, "rules.json", new JSONObject());
            FileHandler.writeValuesInFile(directory, "highlighted-keywords.json", new JSONObject());
        }

        return List.of(Objects.requireNonNull(directory.listFiles()));
    }

    public JSONObject getSpecificGuildConfig(@NotNull Guild guild, GuildConfigType type) {
        if (this.getMainConfigFile().getJSONObject("otherKeys").getString("storage-server").equals(guild.getId()))
            return null;
        return new JSONObject(Objects.requireNonNull(FileHandler.getFileContent(this.getGuildConfigs(guild).stream().filter(f -> f.getName().equals(type.getName())).toList().get(0).getAbsolutePath())));
    }

    public JSONObject getGuildMainConfig(Guild guild) {
        return this.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
    }

    public void writeInGuildMainConfig(Guild guild, JSONObject value) {
        writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, value);
    }

    public void writeInSpecificGuildConfig(@NotNull Guild guild, GuildConfigType type, JSONObject value) {
        if (this.getMainConfigFile().getJSONObject("otherKeys").getString("storage-server").equals(guild.getId()))
            return;
        File configFile = getGuildConfigs(guild).stream().filter(f -> f.getName().equals(type.getName())).toList().get(0);

        FileHandler.writeValuesInFile(configFile.getAbsolutePath(), value);
    }

    public void createLogChannelConfig(Guild guild) {
        JSONObject logConfig = new JSONObject();
        LogChannelType.getLogChannelTypes().stream()
                .filter(x -> !x.equals(LogChannelType.UNKNOW))
                .forEach(type -> logConfig.put(type.getName(), new JSONObject() {{
                    put("channelId", "");
                    put("webhookUrl", "");
                }}));
        logConfig.put("category", "");
        JSONObject guildMainConfig = getGuildMainConfig(guild);
        guildMainConfig.put("log-channel", logConfig);
        writeInGuildMainConfig(guild, guildMainConfig);
    }

    @Contract(" -> new")
    private @NotNull JSONObject guildConfigBlank() {
        return new JSONObject();
    }


    private enum GuildConfigType {
        MAIN("config.json", "The main guild config"),
        KEYWORDS("highlighted-keywords.json", "Keywords from guild"),
        RULES("rules.json", "Rules from guild");


        private final String name;
        private final String description;

        GuildConfigType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
