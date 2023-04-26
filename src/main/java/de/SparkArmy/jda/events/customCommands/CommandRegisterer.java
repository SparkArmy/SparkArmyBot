package de.SparkArmy.jda.events.customCommands;

import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.events.annotations.JDACommandData;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

public class CommandRegisterer {

    private final JDA jda;
    private final Logger logger;

    public CommandRegisterer(@NotNull JdaApi jdaApi) {
        this.jda = jdaApi.getJda();
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
        jda.updateCommands().addCommands(commandData).queue();
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
    final @NotNull CommandData nicknameSlashCommand() {
        return Commands.slash("nickname", "Change or remove a nickname from member")
                .addSubcommands(
                        new SubcommandData("remove", "Remove the nickname from the member")
                                .addOptions(
                                        new OptionData(OptionType.USER, "member", "The target member")
                                                .setRequired(true)
                                ),
                        new SubcommandData("change", "Change the nickname from the member")
                                .addOptions(
                                        new OptionData(OptionType.USER, "member", "The target member")
                                                .setRequired(true),
                                        new OptionData(OptionType.STRING, "nickname", "The new nickname")
                                                .setRequired(true)
                                )
                )
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(moderatorCommandPermissions()));
    }
}
