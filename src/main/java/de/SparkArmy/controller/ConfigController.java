package de.SparkArmy.controller;

import de.SparkArmy.Main;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONObject;

import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class ConfigController {
    private final Main main;
    private final Logger logger = MainUtil.logger;
    private final File configFolder = FileHandler.getDirectoryInUserDirectory("configs");

    public Main getMain() {
        return main;
    }

    public ConfigController(Main main) {
        this.main = main;
        if (configFolder == null) Main.systemExit(11);
    }

    public JSONObject getMainConfigFile(){
        if (!FileHandler.getFileInDirectory(configFolder,"main-config.json").exists()){
            logger.config("The main-config.json-file not exist, we will created a new");
            JSONObject blankConfig = new JSONObject();
            blankConfig.put("discord-token","Write here your Discord-Bot-Token");
            blankConfig.put("discord-client-id","Write here your Discord-Bot-ClientId");
            blankConfig.put("twitch-client-secret","[Optional] Write here your Twitch-Client-Secret");
            blankConfig.put("twitch-client-id","[Optional] Write here your Twitch-Client-Id");
            blankConfig.put("virustotal-api-key","[Optional] Write here your API-Key from VirusTotal");
            blankConfig.put("youtube-api-key","[Optional] Write here your API-Key from YouTube");
            blankConfig.put("storage-server","948898866009362433");
            if(FileHandler.writeValuesInFile(configFolder,"main-config.json",blankConfig)){
                logger.info("main-config.json was successful created");
                logger.config("Please finish your configuration");
                try {
                   Desktop.getDesktop().open(FileHandler.getFileInDirectory(configFolder, "main-config.json"));
                } catch (Exception ignored) {
                }
                Main.systemExit(0);
            }else {
                logger.severe("Can't create a main-config.json");
                Main.systemExit(1);
            }
        }
        String mainConfigAsString = FileHandler.getFileContent(configFolder,"main-config.json");
        if (mainConfigAsString == null){
            logger.severe("The main-config.json is null");
            Main.systemExit(12);
        }
        return new JSONObject(mainConfigAsString);
    }

    private Collection<File> getGuildConfigs(Guild guild){
        File directory = FileHandler.getDirectoryInUserDirectory("configs/" + guild.getId());
        if (directory.listFiles().length == 0){
            FileHandler.createFile(directory,"config.json");
            FileHandler.createFile(directory,"rules.json");
            FileHandler.createFile(directory,"highlighted-keywords.json");

            FileHandler.writeValuesInFile(directory,"config.json",guildConfigBlank());
            FileHandler.writeValuesInFile(directory,"rules.json",new JSONObject());
            FileHandler.writeValuesInFile(directory,"highlighted-keywords.json",new JSONObject());
        }

        return List.of(directory.listFiles());
    }

    public JSONObject getSpecificConfig(Guild guild,String name){
        if (MainUtil.mainConfig.getString("storage-server").equals(guild.getId())) return null;
        return new JSONObject(Objects.requireNonNull(FileHandler.getFileContent(getGuildConfigs(guild).stream().filter(f -> f.getName().equals(name)).toList().get(0).getAbsolutePath())));
    }

    private JSONObject guildConfigBlank(){
        return new JSONObject(){{
            put("command-permissions",new JSONObject());

        }};
    }

}
