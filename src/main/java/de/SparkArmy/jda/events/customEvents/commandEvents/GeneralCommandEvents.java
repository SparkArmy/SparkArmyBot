package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.annotations.events.JDASlashCommandInteractionEvent;
import de.SparkArmy.jda.events.EventManager;
import de.SparkArmy.jda.events.iEvent.IJDAEvent;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class GeneralCommandEvents implements IJDAEvent {

    private final ConfigController controller;

    public GeneralCommandEvents(EventManager manager) {
        this.controller = manager.getController();
    }

    @JDASlashCommandInteractionEvent(name = "ping")
    public void pingSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        event.reply("Pong!").setEphemeral(true).queue();
    }

    @JDASlashCommandInteractionEvent(name = "update-commands")
    public void updateCommandsSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = Util.getResourceBundle("update-commands", event.getUserLocale());

        if (controller.getMain().getJdaApi().getCommandRegisterer().registerCommands()) {
            event.reply(bundle.getString("command.answer.successfully")).setEphemeral(true).queue();
        } else {
            event.reply(bundle.getString("command.answer.failed")).setEphemeral(true).queue();
        }
    }

    @Override
    public Class<?> getEventClass() {
        return this.getClass();
    }
}
