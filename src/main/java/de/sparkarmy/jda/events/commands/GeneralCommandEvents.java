package de.sparkarmy.jda.events.commands;

import de.sparkarmy.config.ConfigController;
import de.sparkarmy.jda.EventManager;
import de.sparkarmy.jda.annotations.events.JDASlashCommandInteractionEvent;
import de.sparkarmy.jda.annotations.internal.JDAEvent;
import de.sparkarmy.jda.events.IJDAEvent;
import de.sparkarmy.utils.Util;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class GeneralCommandEvents implements IJDAEvent {

    private final ConfigController controller;

    public GeneralCommandEvents(EventManager manager) {
        this.controller = manager.getController();
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "ping")
    public void pingSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        event.reply("Pong!").setEphemeral(true).queue();
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "update-commands")
    public void updateCommandsSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = Util.getResourceBundle("update-commands", event.getUserLocale());

        if (controller.main().getJdaApi().getCommandRegisterer().registerCommands()) {
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
