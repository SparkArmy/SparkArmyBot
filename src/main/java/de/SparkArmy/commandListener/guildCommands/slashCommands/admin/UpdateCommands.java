package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandBuilder.CommandRegisterer;
import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UpdateCommands extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        if (event.getGuild() == null) {
            event.reply("Please use this command on a guild").setEphemeral(true).queue();
            return;
        }
        CommandRegisterer.registerCommands();
        event.reply("Commands will be updated").setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "update-commands";
    }
}
