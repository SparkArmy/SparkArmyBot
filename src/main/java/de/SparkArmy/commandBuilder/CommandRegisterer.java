package de.SparkArmy.commandBuilder;

import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import org.json.JSONObject;

import java.io.File;

public class CommandRegisterer {
    public CommandRegisterer() {
        registerGlobalSlashCommands();
    }

    private static final JDA jda = MainUtil.jda;

    public static void registerGlobalSlashCommands(){
        jda.updateCommands().queue();
        JSONObject commandData = new JSONObject();
        GlobalSlashCommands.globalSlashCommands().forEach(c->{
            commandData.append("globalCommands",c.getName());
            jda.upsertCommand(c).queue();
        });

        // Write the command names in a JSONArray
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff");
        FileHandler.createFile(directory,"commandList.json");
        FileHandler.writeValuesInFile(directory,"commandList.json",commandData);
    }
}
