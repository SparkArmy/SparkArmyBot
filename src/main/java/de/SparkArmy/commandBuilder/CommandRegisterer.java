package de.SparkArmy.commandBuilder;

import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;


@SuppressWarnings("unused")
public enum CommandRegisterer {
    ;
    private static final JDA jda = MainUtil.jda;

    public static void registerGlobalSlashCommands(){
        jda.updateCommands().queue();
        JSONObject commandData = new JSONObject();
        SlashCommands.globalSlashCommands().forEach(c-> {
            CommandRegisterer.jda.upsertCommand(c).queue();
            c.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
            commandData.append("globalCommands",c.getName());
            MainUtil.logger.info(c.getName() + " has been updated/created.");
        });

        // Write the command names in a JSONArray
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff");
        FileHandler.createFile(Objects.requireNonNull(directory),"commandList.json");
        FileHandler.writeValuesInFile(directory,"commandList.json",commandData);
    }

    public static void registerGuildSlashCommands(@NotNull Guild guild){
        guild.updateCommands().queue();
        JSONObject commandData = new JSONObject();
        // Moderation related commands
        SlashCommands.guildSlashModerationCommands().forEach(c->{
            commandData.append("guildCommands",c.getName());
            c.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));
            guild.upsertCommand(c).queue();
            MainUtil.logger.info(c.getName() + " has been updated/created.");
        });

        // Admin related commands
        SlashCommands.guildSlashAdminCommands().forEach(c->{
            commandData.append("guildCommands",c.getName());
            c.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
            guild.upsertCommand(c).queue();
            MainUtil.logger.info(c.getName() + " has been updated/created.");
        });

        // Write the command names in a JSONArray
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff");
        FileHandler.createFile(Objects.requireNonNull(directory),"commandList.json");
        FileHandler.writeValuesInFile(directory,"commandList.json",commandData);
    }
}
