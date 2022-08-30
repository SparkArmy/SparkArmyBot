package de.SparkArmy.commandListener.guildCommands.slashCommands;

import de.SparkArmy.commandBuilder.CommandRegisterer;
import de.SparkArmy.commandListener.CustomCommandListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class UpdateCommands extends CustomCommandListener {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("update-commands")) return;
        if (event.getGuild() == null){
            event.reply("Please use this command on a guild").setEphemeral(true).queue();
            return;
        }
        CommandRegisterer.registerGuildSlashCommands(event.getGuild());
        event.reply("Commands will be updated").setEphemeral(true).queue();
    }
}
