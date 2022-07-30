package de.SparkArmy.controller;

import de.SparkArmy.Main;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import org.json.JSONObject;

import java.io.File;
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
            if(FileHandler.writeValuesInFile(configFolder,"main-config.json",blankConfig)){
                logger.info("main-config.json was successful created");
            }else {
                logger.severe("Can't create the main-config.json");
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
}
