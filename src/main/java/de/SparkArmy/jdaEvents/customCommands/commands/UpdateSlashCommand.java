package de.SparkArmy.jdaEvents.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jdaEvents.customCommands.CustomCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class UpdateSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "update-commands";
    }

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, @NotNull ConfigController controller) {
        controller.getMain().getCommandRegisterer().registerCommands();
        event.reply("Commands will be updated").queue();
    }
}
