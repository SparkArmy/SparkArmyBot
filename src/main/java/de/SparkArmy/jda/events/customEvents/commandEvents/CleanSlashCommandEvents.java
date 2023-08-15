package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.annotations.interactions.JDASlashCommand;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class CleanSlashCommandEvents {
    private final ConfigController controller;

    public CleanSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
    }

    @JDASlashCommand(name = "clean")
    public void initialCleanSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {

            return;
        }
        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) return;
    }
}
