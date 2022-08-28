package de.SparkArmy.commandBuilder;

import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.jetbrains.annotations.NotNull;

public enum CommandRegisterer {
    ;
    private static final JDA jda = MainUtil.jda;

    public static void registerGlobalSlashCommands(){
        jda.updateCommands().queue();
        SlashCommands.globalSlashCommands().forEach(c-> {
            CommandRegisterer.jda.upsertCommand(c).queue();
            c.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
            MainUtil.logger.info(c.getName() + " has been updated/created.");
        });
    }

    public static void registerGuildSlashCommands(@NotNull Guild guild){
        guild.updateCommands().queue();
        // Moderation related commands
        SlashCommands.guildSlashModerationCommands().forEach(c->{
            c.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS));
            guild.upsertCommand(c).queue();
            MainUtil.logger.info(c.getName() + " has been updated/created.");
        });

        // Admin related commands
        SlashCommands.guildSlashAdminCommands().forEach(c->{
            c.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
            guild.upsertCommand(c).queue();
            MainUtil.logger.info(c.getName() + " has been updated/created.");
        });
    }
}
