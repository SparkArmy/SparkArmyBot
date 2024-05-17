package de.SparkArmy.jda.utils;

import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.annotations.internal.JDACommandData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class CommandRegisterer {

    private final Logger logger;
    private final ShardManager shardManager;

    public CommandRegisterer(@NotNull JdaApi jdaApi) {
        this.shardManager = jdaApi.getShardManager();
        this.logger = jdaApi.getLogger();
    }

    final String path = "LocalizationData/%s";

    final @NotNull LocalizationFunction getLocalizationFunction(@NotNull CommandData data) {
        return ResourceBundleLocalizationFunction.fromBundles(String.format(path, data.getName()),
                DiscordLocale.GERMAN, DiscordLocale.ENGLISH_UK).build();
    }

    public boolean registerCommands() {
        ArrayList<CommandData> commandData = new ArrayList<>();


        for (Method m : this.getClass().getDeclaredMethods()) {
            JDACommandData annotation = m.getAnnotation(JDACommandData.class);
            if (annotation != null && m.getReturnType().equals(CommandData.class)) {
                try {
                    CommandData data = (CommandData) m.invoke(this);
                    data.setLocalizationFunction(getLocalizationFunction(data));
                    commandData.add(data);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error(e.getMessage());
                    return false;
                }
            }
        }
        for (JDA jda : shardManager.getShards()) {
            jda.updateCommands().addCommands(commandData).queue();
        }
        return true;
    }

    @Contract(" -> new")
    final @NotNull Collection<Permission> moderatorCommandPermissions() {
        return new ArrayList<>() {{
            add(Permission.BAN_MEMBERS);
            add(Permission.KICK_MEMBERS);
            add(Permission.MODERATE_MEMBERS);
        }};
    }

    @JDACommandData
    final @NotNull CommandData archiveSlashCommand() {
        return Commands.slash("archive", "Move the Channel in archive-category")
                .addOptions(
                        new OptionData(OptionType.CHANNEL, "channel", "The channel you want to archive")
                                .setRequired(true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);
    }

    @JDACommandData
    final @NotNull CommandData banSlashCommand() {
        return Commands.slash("ban", "Bans a user from server")
                .addOptions(
                        new OptionData(OptionType.USER, "target-user", "The user you want to ban")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "reason", "The reason for the ban")
                                .setRequired(true),
                        new OptionData(OptionType.INTEGER, "days", "The days you want the messages delete")
                                .setRequiredRange(0, 7)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()))
                .setGuildOnly(true);
    }

    @JDACommandData
    final @NotNull CommandData kickSlashCommand() {
        return Commands.slash("kick", "Kick a user from server")
                .addOptions(
                        new OptionData(OptionType.USER, "target-user", "The user you want to kick")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "reason", "The reason for the kick")
                                .setRequired(true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()))
                .setGuildOnly(true);
    }

    @JDACommandData
    final @NotNull CommandData muteSlashCommand() {
        return Commands.slash("mute", "Mute a member")
                .addOptions(
                        new OptionData(OptionType.USER, "target-user", "The member you want to mute")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "reason", "The reason for the mute")
                                .setRequired(true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()))
                .setGuildOnly(true);
    }

    @JDACommandData
    final @NotNull CommandData updateCommandsSlashCommand() {
        return Commands.slash("update-commands", "Update the commands")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);
    }

    @JDACommandData
    final @NotNull CommandData unbanSlashCommand() {
        return Commands.slash("unban", "Unban a user")
                .addOptions(
                        new OptionData(OptionType.USER, "target-user", "The target user")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "reason", "The reason for the unban")
                                .setRequired(true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()))
                .setGuildOnly(true);
    }

    @JDACommandData
    final @NotNull CommandData warnSlashCommand() {
        return Commands.slash("warn", "Warn a member")
                .addOptions(
                        new OptionData(OptionType.USER, "target-user", "The member you want to warn")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "reason", "The reason for the warn")
                                .setRequired(true)
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()))
                .setGuildOnly(true);
    }

    @JDACommandData
    final @NotNull CommandData softbanSlashCommand() {
        return Commands.slash("softban", "Ban and unban a user and delete his messages")
                .addOptions(
                        new OptionData(OptionType.USER, "target-user", "The user you want to softban")
                                .setRequired(true),
                        new OptionData(OptionType.STRING, "reason", "the reason for the softban")
                                .setRequired(true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()))
                .setGuildOnly(true);
    }

    @JDACommandData
    final @NotNull CommandData pingSlashCommand() {
        return Commands.slash("ping", "Replies with pong")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @JDACommandData
    final @NotNull CommandData noteSlashCommand() {
        return Commands.slash("note", "Add, edit or remove a note from member")
                .addSubcommands(
                        new SubcommandData("add", "Add a note to a member")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The target user")
                                                .setRequired(true),
                                        new OptionData(OptionType.STRING, "note", "The note")
                                                .setRequired(true)
                                ),
                        new SubcommandData("show", "Show the notes from a member, you can remove or edit the notes")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "The target user")
                                                .setRequired(true)
                                )
                )
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()));
    }

    @JDACommandData
    final @NotNull CommandData notificationSlashCommand() {
        return Commands.slash("notification", "Add, remove or change notifications from YouTube, Twitch or Twitter")
                .addOptions(
                        new OptionData(OptionType.STRING, "platform", "The notification platform")
                                .setAutoComplete(true)
                )
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @JDACommandData
    final @NotNull CommandData cleanSlashCommand() {
        return Commands.slash("clean", "Remove multiple messages(max. 100)")
                .addSubcommands(
                        new SubcommandData("all", "Deletes all messages")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "count", "The amount of messages")
                                                .setRequiredRange(1, 100),
                                        new OptionData(OptionType.USER, "user", "The user")),
                        new SubcommandData("last", "Deletes all messages to a specific time")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "days", "The days")
                                                .setRequiredRange(1, 5)))
                .addSubcommandGroups(
                        new SubcommandGroupData("periodic", "Deletes all messages in a periodic time from the channel")
                                .addSubcommands(
                                        new SubcommandData("add", "Adds a new periodic clean action")
                                                .addOptions(
                                                        new OptionData(OptionType.CHANNEL, "channel", "The channel"),
                                                        new OptionData(OptionType.INTEGER, "period", "The period in days")),
                                        new SubcommandData("show", "Show all periodic clean actions for the guild")
                                ))
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()));
    }

    @JDACommandData
    final @NotNull CommandData configureSlashCommand() {
        return Commands.slash("configure", "Configures the settings for the bot in the guild")
                .addSubcommandGroups(
                        new SubcommandGroupData("channel", "Configure channels")
                                .addSubcommands(
                                        new SubcommandData("log-channels", "Configure the Log-Channels")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "type", "The log-channel-type")
                                                                .setRequired(true)
                                                                .setAutoComplete(true),
                                                        new OptionData(OptionType.CHANNEL, "target-channel", "The channel where the logs will been sent")
                                                                .setChannelTypes(ChannelType.TEXT),
                                                        new OptionData(OptionType.BOOLEAN, "remove", "The log-channel will be removed")
                                                ),
                                        new SubcommandData("media-only-channel", "Configure the MediaOnlyChannel"),
                                        new SubcommandData("archive-category", "Manage the archive category"),
                                        new SubcommandData("feedback-channel", "Manage the feedback-channel")
                                ),
                        new SubcommandGroupData("roles", "Configure the roles")
                                .addSubcommands(
                                        new SubcommandData("mod-roles", "Manage the mod roles")
                                                .addOptions(
                                                        new OptionData(OptionType.ROLE, "add", "Adds the role to the mod roles"),
                                                        new OptionData(OptionType.ROLE, "remove", "Remove the role from the mod roles")
                                                ),
                                        new SubcommandData("punishment-roles", "Manage the punishment roles")
                                                .addOptions(
                                                        new OptionData(OptionType.ROLE, "warn-role", "Set the warn role"),
                                                        new OptionData(OptionType.ROLE, "mute-role", "Set the mute role"),
                                                        new OptionData(OptionType.BOOLEAN, "warn-disabled", "Disable the warn role"),
                                                        new OptionData(OptionType.BOOLEAN, "mute-disabled", "Disable the mute role")
                                                )
                                ),
                        new SubcommandGroupData("regex", "Manage regex settings")
                                .addSubcommands(
                                        new SubcommandData("blacklist", "Manage the blacklist"),
                                        new SubcommandData("manage", "Manage the regex settings")
                                ),
                        new SubcommandGroupData("ticket", "Manage the ModMail settings")
                                .addSubcommands(
                                        new SubcommandData("category", "Manage the category and channel for ModMail channels"),
                                        new SubcommandData("blacklist", "The blacklist for users")
                                                .addOptions(
                                                        new OptionData(OptionType.USER, "user", "The user for the blacklist")
                                                ),
                                        new SubcommandData("ping-roles", "Manage the roles were are pinged")
                                                .addOptions(
                                                        new OptionData(OptionType.ROLE, "role", "The role to add/remove")
                                                ),
                                        new SubcommandData("message", "Send the message with the button to create a ticket")
                                )
                )
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Contract(" -> new")
    @JDACommandData
    final @NotNull CommandData feedbackSlashCommand() {
        return Commands.slash("feedback", "Send feedback to the staff")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }
}
