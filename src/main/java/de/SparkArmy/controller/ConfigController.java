package de.SparkArmy.controller;

import de.SparkArmy.Main;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.LogMarker;
import de.SparkArmy.utils.MainUtil;
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

@SuppressWarnings("unused")
public class ConfigController {
    private final Main main;
    private final Logger logger = MainUtil.logger;
    private final File configFolder = FileHandler.getDirectoryInUserDirectory("configs");

    public Main getMain() {
        return this.main;
    }

    public ConfigController(Main main) {
        this.main = main;
        if (null == configFolder) Main.systemExit(11);
    }

    public JSONObject getMainConfigFile(){
        assert this.configFolder != null;
        if (!FileHandler.getFileInDirectory(this.configFolder,"main-config.json").exists()){
            this.logger.warn(LogMarker.CONFIG,"The main-config.json-file not exist, we will created a new");
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
            JSONObject mariadb = new JSONObject(){{
                put("url","The url for the databases (//127.0.0.1/ -> standard for localhost)");
                put("user","Database-User");
                put("password","User-Password");
            }};
            blankConfig.put("mariaDbConnection",mariadb);
            JSONObject otherKeys = new JSONObject(){{
                put("virustotal-api-key","[Optional] Write here your API-Key from VirusTotal");
                put("storage-server","Please setup a new server, delete all included channels and put the id in this field");
                put("twitter_bearer","Please copy the twitter bearer in this field");
            }};
            blankConfig.put("otherKeys",otherKeys);

            if(FileHandler.writeValuesInFile(this.configFolder,"main-config.json",blankConfig)){
                this.logger.info(LogMarker.CONFIG,"main-config.json was successful created");
                this.logger.warn(LogMarker.CONFIG,"Please finish your configuration");
                try {
                   Desktop.getDesktop().open(FileHandler.getFileInDirectory(this.configFolder, "main-config.json"));
                } catch (Exception ignored) {
                }
                Main.systemExit(0);
            }else {
                this.logger.error(LogMarker.CONFIG,"Can't create a main-config.json");
                Main.systemExit(1);
            }
        }
        String mainConfigAsString = FileHandler.getFileContent(this.configFolder,"main-config.json");
        if (mainConfigAsString == null){
            this.logger.error(LogMarker.CONFIG,"The main-config.json is null");
            Main.systemExit(12);
        }
        assert mainConfigAsString != null;
        return new JSONObject(mainConfigAsString);
    }

    private @Unmodifiable Collection<File> getGuildConfigs(@NotNull Guild guild){
        File directory = FileHandler.getDirectoryInUserDirectory("configs/" + guild.getId());
        assert directory != null;
        if (0 == Objects.requireNonNull(directory.listFiles()).length){
            FileHandler.createFile(directory,"config.json");
            FileHandler.createFile(directory,"rules.json");
            FileHandler.createFile(directory,"highlighted-keywords.json");

            FileHandler.writeValuesInFile(directory,"config.json", this.guildConfigBlank());
            FileHandler.writeValuesInFile(directory,"rules.json",new JSONObject());
            FileHandler.writeValuesInFile(directory,"highlighted-keywords.json",new JSONObject());
        }

        return List.of(Objects.requireNonNull(directory.listFiles()));
    }

    public JSONObject getSpecificGuildConfig(@NotNull Guild guild, GuildConfigType type){
        if (MainUtil.mainConfig.getJSONObject("otherKeys").getString("storage-server").equals(guild.getId())) return null;
        return new JSONObject(Objects.requireNonNull(FileHandler.getFileContent(this.getGuildConfigs(guild).stream().filter(f -> f.getName().equals(type.getName())).toList().get(0).getAbsolutePath())));
    }

    public void writeInSpecificGuildConfig(@NotNull Guild guild, GuildConfigType type, JSONObject value){
        if (MainUtil.mainConfig.getJSONObject("otherKeys").getString("storage-server").equals(guild.getId())) return;
        File configFile = getGuildConfigs(guild).stream().filter(f->f.getName().equals(type.getName())).toList().get(0);

        FileHandler.writeValuesInFile(configFile.getAbsolutePath(),value);
    }

    @Contract(" -> new")
    private @NotNull JSONObject guildConfigBlank() {
            return new JSONObject();
    }

}
