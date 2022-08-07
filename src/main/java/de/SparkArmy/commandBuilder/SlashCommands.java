package de.SparkArmy.commandBuilder;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public enum SlashCommands {
    ;

    @Contract(" -> new")
    static @NotNull Collection<CommandData> globalSlashCommands() {
        return new ArrayList<>() {{
            this.add(Commands.slash("modmail", "Contact to the server team for help"));
            this.add(Commands.slash("feedback", "Contact for feedback"));
        }};
    }

    @Contract(" -> new")
    static @NotNull Collection<CommandData> guildSlashCommands(){
        return new ArrayList<>(){{
            // Warn command
            add(Commands.slash("warn","Warns a user").addOptions(
                    new OptionData(OptionType.USER,"target_user","The targeted user").setRequired(true),
                    new OptionData(OptionType.STRING,"reason","The Reason for the punishment").setRequired(true),
                    new OptionData(OptionType.INTEGER,"duration","The duration of the warn (Default unit of time is minutes)"),
                    new OptionData(OptionType.STRING,"time_unit","The time unit from the duration (Default duration is 1 time unit)").addChoices(
                            new Command.Choice("min","minuets"),
                            new Command.Choice("h","hours"),
                            new Command.Choice("d","days"),
                            new Command.Choice("M","months"),
                            new Command.Choice("y","Years"))
                    ));

            // Mute command
            add(Commands.slash("mute","Mute a user").addOptions(
                    new OptionData(OptionType.USER,"target_user","The targeted user").setRequired(true),
                    new OptionData(OptionType.STRING,"reason","The Reason for the punishment").setRequired(true),
                    new OptionData(OptionType.INTEGER,"duration","The duration of the mute (Default unit of time is minutes)"),
                    new OptionData(OptionType.STRING,"time_unit","The time unit from the duration (Default duration is 1 time unit)").addChoices(
                            new Command.Choice("min","minuets"),
                            new Command.Choice("h","hours"),
                            new Command.Choice("d","days"),
                            new Command.Choice("M","months"),
                            new Command.Choice("y","years")
                    )));

            // Kick command
//            add(Commands.slash("kick",""));
        }};
    }
}
