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

    final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
            .fromBundles("LocalizationData/MyCommands", DiscordLocale.GERMAN)
            .build();

    public void registerCommands() {
        ArrayList<CommandData> commandData = new ArrayList<>();

        CommandData archive = Commands.slash("archive", "Move the Channel in archive-category")
                .addOptions(
                        new OptionData(OptionType.CHANNEL, "channel", "The channel you want to archive")
                                .setRequired(true)
                )
                .setLocalizationFunction(localizationFunction);
        commandData.add(archive);

        CommandData ban = Commands.slash("ban", "Bans a user from server")
                .addOptions(
                        new OptionData(OptionType.USER, "target-user", "The user you want to ban")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "reason", "The reason for the ban")
                                .setRequired(true),
                        new OptionData(OptionType.INTEGER, "days", "The days you want the messages delete")
                                .setRequiredRange(0, 7)
                )
                .setLocalizationFunction(localizationFunction);
        commandData.add(ban);

        CommandData updateCommand = Commands.slash("update-commands", "Update the commands")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);
        commandData.add(updateCommand);


        jda.updateCommands().addCommands(commandData).queue();
    }


}
