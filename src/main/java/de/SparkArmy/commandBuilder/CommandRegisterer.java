package de.SparkArmy.commandBuilder;

import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public enum CommandRegisterer {
    ;
    private static final JDA jda = MainUtil.jda;

    public static void registerGlobalSlashCommands() {
        Collection<CommandData> globalCommands = SlashCommands.globalSlashCommands();
        globalCommands.forEach(x->x.setDefaultPermissions(DefaultMemberPermissions.ENABLED));
        if (jda.retrieveCommands().complete().stream().anyMatch(x->{
            String name = globalCommands.stream().toList().get(0).getName();
            return name.equals(x.getName());
        })){
            return;
        }
        jda.updateCommands().addCommands(globalCommands).queue();
        MainUtil.logger.info("Global-Commands are registered");
    }

    public static void registerGuildSlashCommands(@NotNull Guild guild) {
        // Moderation related commands
        Collection<CommandData> moderationCommands = SlashCommands.guildSlashModerationCommands();
        moderationCommands.forEach(x -> x.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS)));

        // Admin related commands
        Collection<CommandData> adminCommands = SlashCommands.guildSlashAdminCommands();
        adminCommands.forEach(x -> x.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));

        // Put all guildCommands in one Collection
        Collection<CommandData> guildCommands = new ArrayList<>();
        guildCommands.addAll(moderationCommands);
        guildCommands.addAll(adminCommands);

        guild.updateCommands().addCommands(guildCommands).queue();
        MainUtil.logger.info("Guild-Commands are registered");
    }
}
