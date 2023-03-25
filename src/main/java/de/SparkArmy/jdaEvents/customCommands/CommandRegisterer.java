package de.SparkArmy.jdaEvents.customCommands;

import de.SparkArmy.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CommandRegisterer {

    private final JDA jda;

    public CommandRegisterer(@NotNull Main main) {
        this.jda = main.getJda();
    }

    final String path = "LocalizationData/%s";

    final @NotNull LocalizationFunction getLocalizationFunction(@NotNull CommandData data) {
        return ResourceBundleLocalizationFunction.fromBundles(String.format(path, data.getName()),
                DiscordLocale.GERMAN, DiscordLocale.ENGLISH_UK).build();
    }

    public void registerCommands() {
        ArrayList<CommandData> commandData = new ArrayList<>();

        CommandData archive = Commands.slash("archive", "Move the Channel in archive-category")
                .addOptions(
                        new OptionData(OptionType.CHANNEL, "channel", "The channel you want to archive")
                                .setRequired(true)
                );
        archive.setLocalizationFunction(getLocalizationFunction(archive));
        commandData.add(archive);

        CommandData ban = Commands.slash("ban", "Bans a user from server")
                .addOptions(
                        new OptionData(OptionType.USER, "target-user", "The user you want to ban")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "reason", "The reason for the ban")
                                .setRequired(true),
                        new OptionData(OptionType.INTEGER, "days", "The days you want the messages delete")
                                .setRequiredRange(0, 7)
                );
        ban.setLocalizationFunction(getLocalizationFunction(ban));
        commandData.add(ban);

        CommandData updateCommand = Commands.slash("update-commands", "Update the commands")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);
        updateCommand.setLocalizationFunction(getLocalizationFunction(updateCommand));
        commandData.add(updateCommand);


        jda.updateCommands().addCommands(commandData).queue();
    }
}
