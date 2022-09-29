package de.SparkArmy.commandBuilder;

import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.Collection;

public enum CommandRegisterer {
    ;
    private static final JDA jda = MainUtil.jda;

    public static void registerCommands() {

        // Global commands
        Collection<CommandData> globalCommands = SlashCommands.globalSlashCommands();
        globalCommands.forEach(x->{
            x.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
            x.setGuildOnly(false);
        });

        // Moderation related commands
        Collection<CommandData> moderationCommands = SlashCommands.guildSlashModerationCommands();
        moderationCommands.forEach(x -> {
            x.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));
            x.setGuildOnly(true);
        });

        // Admin related commands
        Collection<CommandData> adminCommands = new ArrayList<>(){{
            addAll(SlashCommands.guildSlashAdminCommands());
            addAll(UserCommands.adminUserCommands());
        }};
        adminCommands.forEach(x -> {
            x.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
            x.setGuildOnly(true);
        });

        // Public guild commands
        Collection<CommandData> generalCommands = new ArrayList<>(){{
            Collection<CommandData> generalUserCommands = UserCommands.generalUserCommands();
            generalUserCommands.forEach(x->{
                x.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
                x.setGuildOnly(true);
            });
            addAll(generalUserCommands);
            Collection<CommandData> generalMessageCommands = MessageCommands.generalMessageCommands();
            generalMessageCommands.forEach(x->{
                x.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
                x.setGuildOnly(true);
            });
            addAll(generalMessageCommands);
        }};

        Collection<CommandData> commands = new ArrayList<>(){{
            addAll(globalCommands);
            addAll(moderationCommands);
            addAll(adminCommands);
            addAll(generalCommands);
        }};

        if (jda.retrieveCommands().complete().equals(commands)) return;

        jda.updateCommands().addCommands(commands).queue();
        MainUtil.logger.info("Commands are registered");
    }

}
