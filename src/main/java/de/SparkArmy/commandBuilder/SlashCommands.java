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
            add(Commands.slash("modmail", "Contact to the server team for help"));
            add(Commands.slash("feedback", "Feedback to Videos, Streams, Server,...").addOptions(
                    new OptionData(OptionType.STRING, "feedback-category", "The feedback category")
                            .setRequired(true)
                            .setMinValue(4)
                            .setAutoComplete(true)
            ));
        }};
    }

    @Contract(" -> new")
    static @NotNull Collection<CommandData> guildSlashModerationCommands() {
        return new ArrayList<>() {{
            // Warn command
            add(Commands.slash("warn", "Warns a user").addOptions(
                    new OptionData(OptionType.USER, "target_user", "The targeted user").setRequired(true),
                    new OptionData(OptionType.STRING, "reason", "The Reason for the punishment").setRequired(true),
                    new OptionData(OptionType.INTEGER, "duration", "The duration of the warn (Default unit of time is minutes)"),
                    new OptionData(OptionType.STRING, "time_unit", "The time unit from the duration (Default duration is 1 time unit)").addChoices(
                            new Command.Choice("min", "minuets"),
                            new Command.Choice("h", "hours"),
                            new Command.Choice("d", "days"),
                            new Command.Choice("M", "months"),
                            new Command.Choice("y", "Years"))
            ));

            // Mute command
            add(Commands.slash("mute", "Mute a user").addOptions(
                    new OptionData(OptionType.USER, "target_user", "The targeted user").setRequired(true),
                    new OptionData(OptionType.STRING, "reason", "The Reason for the punishment").setRequired(true),
                    new OptionData(OptionType.INTEGER, "duration", "The duration of the mute (Default unit of time is minutes)"),
                    new OptionData(OptionType.STRING, "time_unit", "The time unit from the duration (Default duration is 1 time unit)").addChoices(
                            new Command.Choice("min", "minuets"),
                            new Command.Choice("h", "hours"),
                            new Command.Choice("d", "days"),
                            new Command.Choice("M", "months"),
                            new Command.Choice("y", "years")
                    )));


            // Ban command
            add(Commands.slash("ban", "Ban a user").addOptions(
                    new OptionData(OptionType.USER, "target_user", "The targeted user").setRequired(true),
                    new OptionData(OptionType.STRING, "reason", "The Reason for the punishment").setRequired(true),
                    new OptionData(OptionType.INTEGER, "duration", "The duration of the ban (Default unit of time is minutes)"),
                    new OptionData(OptionType.STRING, "time_unit", "The time unit from the duration (Default duration is 1 time unit)").addChoices(
                            new Command.Choice("min", "minuets"),
                            new Command.Choice("h", "hours"),
                            new Command.Choice("d", "days"),
                            new Command.Choice("M", "months"),
                            new Command.Choice("y", "years")
                    )));

            // Kick command
            add(Commands.slash("kick", "Kick a user").addOptions(
                    new OptionData(OptionType.USER, "target_user", "The targeted user").setRequired(true),
                    new OptionData(OptionType.STRING, "reason", "The Reason for the punishment").setRequired(true)
            ));

        }};
    }

    @Contract(" -> new")
    static @NotNull Collection<CommandData> guildSlashAdminCommands() {
        return new ArrayList<>() {{
            // Punishment command to change the punishment roles and activate/deactivate the role give function
            add(Commands.slash("punishment", "Change the punishment roles and activate/deactivate the role-give function").addOptions(
                    new OptionData(OptionType.STRING, "punishment", "The target punishment").addChoices(
                            new Command.Choice("Warn", PunishmentType.WARN.getName()),
                            new Command.Choice("Mute", PunishmentType.MUTE.getName()),
                            new Command.Choice("Kick", PunishmentType.KICK.getName()),
                            new Command.Choice("Ban", PunishmentType.BAN.getName()),
                            new Command.Choice("Timeout", PunishmentType.TIMEOUT.getName())
                    ),
                    new OptionData(OptionType.ROLE, "punishment-role", "Only available for Warn and Mute")
            ));

            // ReactionRoles command
            add(Commands.slash("reactionroles", "Create/Edit/Delete ReactionRoles").addOptions(
                    new OptionData(OptionType.STRING, "action", "The provided action").addChoices(
                            new Command.Choice("Create", "create"),
                            new Command.Choice("Edit", "edit"),
                            new Command.Choice("Delete", "delete")
                    ),
                    new OptionData(OptionType.STRING, "message", "The target reaction-role-embed (Works only with Edit/Delete)")
            ));

            // MediaOnly command
            add(Commands.slash("media-only", "Add/Edit/Remove MediaOnlyChannel").addOptions(
                    new OptionData(OptionType.STRING, "action", "The provided action").addChoices(
                            new Command.Choice("Add", "add"),
                            new Command.Choice("Edit", "edit"),
                            new Command.Choice("Remove", "remove")
                    ),
                    new OptionData(OptionType.CHANNEL, "channel", "The provided channel")
            ));

            // Notification command
            add(Commands.slash("notifications", "Add/Edit/Remove Notifications").addOptions(
                    new OptionData(OptionType.STRING, "notification", "The provided action").addChoices(
                            new Command.Choice("Twitter", "twitter"),
                            new Command.Choice("YouTube", "youtube"),
                            new Command.Choice("Twitch", "twitch")
                    ),
                    new OptionData(OptionType.STRING, "action", "The provided action").addChoices(
                            new Command.Choice("Add", "add"),
                            new Command.Choice("Edit", "edit"),
                            new Command.Choice("Remove", "remove")
                    )
            ));

            // Lockdown command
            add(Commands.slash("lockdown","Enable/Disable for all non admins to write in this channel").addOptions(
                    new OptionData(OptionType.CHANNEL,"target_channel","The target channel")
            ));

            // Update command
            add(Commands.slash("update-commands","Update commands"));
        }};
    }
}
