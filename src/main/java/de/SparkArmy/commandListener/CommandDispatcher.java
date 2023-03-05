package de.SparkArmy.commandListener;

import de.SparkArmy.Main;
import de.SparkArmy.commandListener.globalCommands.slashCommands.FeedbackCommand;
import de.SparkArmy.commandListener.globalCommands.slashCommands.ModmailCommand;
import de.SparkArmy.commandListener.guildCommands.messageCommands.Report;
import de.SparkArmy.commandListener.guildCommands.slashCommands.admin.*;
import de.SparkArmy.commandListener.guildCommands.slashCommands.moderation.*;
import de.SparkArmy.commandListener.guildCommands.userCommands.admin.ModUnmodMember;
import de.SparkArmy.commandListener.guildCommands.userCommands.general.RoleRemove;
import de.SparkArmy.commandListener.guildCommands.userCommands.moderation.UserContextMute;
import de.SparkArmy.commandListener.guildCommands.userCommands.moderation.UserContextWarn;
import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommandDispatcher extends ListenerAdapter {

    private final ConfigController controller;
    private final JDA jda;

    Set<Command> commands = ConcurrentHashMap.newKeySet();
    Set<UserCommand> userCommands = ConcurrentHashMap.newKeySet();
    Set<MessageCommand> messageCommands = ConcurrentHashMap.newKeySet();
    Set<SlashCommand> slashCommands = ConcurrentHashMap.newKeySet();


    public CommandDispatcher(@NotNull Main main) {
        this.controller = main.controller;
        this.jda = main.jda;
        registerCommands();
    }

    public void registerCommands() {
        // Message Context Commands
        this.registerMessageCommand(new Report());

        // User Context Commands
        this.registerUserCommand(new ModUnmodMember());
        this.registerUserCommand(new RoleRemove());
        this.registerUserCommand(new UserContextMute());
        this.registerUserCommand(new UserContextWarn());

        // Slash Commands
        this.registerSlashCommand(new FeedbackCommand());
        this.registerSlashCommand(new ModmailCommand());
        this.registerSlashCommand(new SlashWarn());
        this.registerSlashCommand(new SlashMute());
        this.registerSlashCommand(new SlashKick());
        this.registerSlashCommand(new SlashBan());
        this.registerSlashCommand(new UserPunishments());
        this.registerSlashCommand(new UserNicknames());
        this.registerSlashCommand(new Punishment());
        this.registerSlashCommand(new ReactionRoles());
        this.registerSlashCommand(new MediaOnly());
        this.registerSlashCommand(new Notifications());
        this.registerSlashCommand(new Lockdown());
        this.registerSlashCommand(new UpdateCommands());
        this.registerSlashCommand(new LogChannelConfig());
        this.registerSlashCommand(new ModmailConfig());
        this.registerSlashCommand(new ModerationConfig());
        this.registerSlashCommand(new GuildMemberCountChannel());
        this.registerSlashCommand(new Purge());
    }


    public Set<Command> getCommands() {
        return Set.copyOf(this.commands);
    }

    @Override
    public void onGenericCommandInteraction(@NotNull GenericCommandInteractionEvent event) {
        if (event instanceof UserContextInteractionEvent) {
            for (final UserCommand c : this.userCommands) {
                if (c.getName().equals(event.getName())) {
                    c.dispatch((UserContextInteractionEvent) event, this.jda, this.controller);
                }
            }
        } else if (event instanceof MessageContextInteractionEvent) {
            for (final MessageCommand c : this.messageCommands) {
                if (c.getName().equals(event.getName())) {
                    c.dispatch((MessageContextInteractionEvent) event, this.jda, this.controller);
                }
            }
        } else if (event instanceof SlashCommandInteractionEvent) {
            for (final SlashCommand c : this.slashCommands) {
                if (c.getName().equals(event.getName())) {
                    c.dispatch((SlashCommandInteractionEvent) event, this.jda, this.controller);
                }
            }
        }
    }

    public void registerUserCommand(final UserCommand command) {
        this.commands.add(command);
        this.userCommands.add(command);
    }

    public void registerMessageCommand(final MessageCommand command) {
        this.commands.add(command);
        this.messageCommands.add(command);
    }

    public void registerSlashCommand(final SlashCommand command) {
        this.commands.add(command);
        this.slashCommands.add(command);
    }
}
