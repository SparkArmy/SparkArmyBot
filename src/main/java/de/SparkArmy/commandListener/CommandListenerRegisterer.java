package de.SparkArmy.commandListener;

import de.SparkArmy.commandListener.globalCommands.slashCommands.FeedbackCommand;
import de.SparkArmy.commandListener.globalCommands.slashCommands.ModmailCommand;
import de.SparkArmy.commandListener.guildCommands.messageCommands.Report;
import de.SparkArmy.commandListener.guildCommands.slashCommands.admin.*;
import de.SparkArmy.commandListener.guildCommands.slashCommands.moderation.*;
import de.SparkArmy.commandListener.guildCommands.userCommands.admin.ModUnmodMember;
import de.SparkArmy.commandListener.guildCommands.userCommands.general.RoleRemove;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;

public class CommandListenerRegisterer {

    private final ArrayList<CustomCommandListener> commands = new ArrayList<>();
    private final JDA jda = MainUtil.jda;

    public CommandListenerRegisterer() {
        this.registerCommandListeners();
    }

    private void registerCommandListeners() {
        // SlashCommands
            // Global Commands
            commands.add(new FeedbackCommand());
            commands.add(new ModmailCommand());

            // Moderation Commands
            commands.add(new Warn());
            commands.add(new Mute());
            commands.add(new Ban());
            commands.add(new Kick());
            commands.add(new Punishments());

            // Admin Commands
            commands.add(new Punishment());
            commands.add(new ReactionRoles());
            commands.add(new MediaOnly());
            commands.add(new Notifications());
            commands.add(new Lockdown());
            commands.add(new UpdateCommands());
            commands.add(new LogChannelConfig());
            commands.add(new ModmailConfig());
            commands.add(new ModerationConfig());
            commands.add(new GuildMemberCountChannel());

        // User Commands
            // Admin Commands
            commands.add(new ModUnmodMember());

            // General Commands
            commands.add(new RoleRemove());

        // Message Commands
            // General Commands
            commands.add(new Report());


        this.commands.forEach(this.jda::addEventListener);
    }
}
