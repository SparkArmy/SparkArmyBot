package de.SparkArmy.jda.events.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ResourceBundle;

public class NoteSlashCommand extends CustomCommand {

    @Override
    public String getName() {
        return "note";
    }

    Logger logger;

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, @NotNull ConfigController controller) {
        logger = controller.getMain().getLogger();
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());
        String subcommandName = event.getSubcommandName();

        //noinspection DataFlowIssue
        switch (subcommandName) {
            case "add" -> addNoteCommand(event, controller);
            case "show" -> showNoteCommand(event, controller);
            default -> {
                logger.warn(getName() + " has a default value in switch(subcommandName) with value: " + subcommandName);
                event.reply(bundle.getString("command.dispatchSlashEvent.defaultReply")).setEphemeral(true).queue();
            }
        }

    }

    private void showNoteCommand(@NotNull SlashCommandInteractionEvent event, ConfigController controller) {
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());
    }

    private void addNoteCommand(@NotNull SlashCommandInteractionEvent event, ConfigController controller) {
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());
    }
}
