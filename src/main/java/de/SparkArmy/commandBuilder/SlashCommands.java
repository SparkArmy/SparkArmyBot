package de.SparkArmy.commandBuilder;

import de.SparkArmy.utils.punishmentUtils.PunishmentType;
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


            // Punishment command to change the punishment roles and activate/deactivate the role give function
            add(Commands.slash("punishment","Change the punishment roles and activate/deactivate the role-give function").addOptions(
                    new OptionData(OptionType.STRING,"punishment","The target punishment").addChoices(
                            new Command.Choice("Warn",PunishmentType.WARN.getName()),
                            new Command.Choice("Mute",PunishmentType.MUTE.getName()),
                            new Command.Choice("Kick",PunishmentType.KICK.getName()),
                            new Command.Choice("Ban",PunishmentType.BAN.getName()),
                            new Command.Choice("Timeout",PunishmentType.TIMEOUT.getName())
                    ),
                    new OptionData(OptionType.ROLE,"punishment-role","Only available for Warn and Mute")
            ));

            // Ban command
            add(Commands.slash("ban","Ban a user").addOptions(
                    new OptionData(OptionType.USER,"target_user","The targeted user").setRequired(true),
                    new OptionData(OptionType.STRING,"reason","The Reason for the punishment").setRequired(true),
                    new OptionData(OptionType.INTEGER,"duration","The duration of the ban (Default unit of time is minutes)"),
                    new OptionData(OptionType.STRING,"time_unit","The time unit from the duration (Default duration is 1 time unit)").addChoices(
                            new Command.Choice("min","minuets"),
                            new Command.Choice("h","hours"),
                            new Command.Choice("d","days"),
                            new Command.Choice("M","months"),
                            new Command.Choice("y","years")
            )));

            // Kick command
            add(Commands.slash("kick","Kick a user").addOptions(
                    new OptionData(OptionType.USER,"target_user","The targeted user").setRequired(true),
                    new OptionData(OptionType.STRING,"reason","The Reason for the punishment").setRequired(true)
            ));

            // ReactionRoles command
            add(Commands.slash("reactionroles","Create/Edit/Delete ReactionRoles").addOptions(
                    new OptionData(OptionType.STRING,"action","The provided action").addChoices(
                            new Command.Choice("Create","create"),
                            new Command.Choice("Edit","edit"),
                            new Command.Choice("Delete","delete")
                    ),
                    new OptionData(OptionType.STRING,"message","The target reaction-role-embed (Works only with Edit/Delete)")
            ));
        }};
    }
}
