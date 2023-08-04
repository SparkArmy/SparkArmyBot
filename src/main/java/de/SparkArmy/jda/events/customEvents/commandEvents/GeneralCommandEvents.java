package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.annotations.interactions.JDASlashCommand;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class GeneralCommandEvents {

    private final ConfigController controller;

    public GeneralCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
    }

    @JDASlashCommand(name = "ping")
    public void pingSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        event.reply("Pong!").setEphemeral(true).queue();
    }

    @JDASlashCommand(name = "update-commands")
    public void updateCommandsSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = Util.getResourceBundle("update-commands", event.getUserLocale());

        if (controller.getMain().getJdaApi().getCommandRegisterer().registerCommands()) {
            event.reply(bundle.getString("command.answer.successfully")).setEphemeral(true).queue();
        } else {
            event.reply(bundle.getString("command.answer.failed")).setEphemeral(true).queue();
        }
    }
}
