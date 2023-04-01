package de.SparkArmy.jda.events.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class PingSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, ConfigController controller) {
        event.reply("Pong!").setEphemeral(true).queue();
    }
}
