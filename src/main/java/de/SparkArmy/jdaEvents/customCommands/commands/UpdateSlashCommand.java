package de.SparkArmy.jdaEvents.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jdaEvents.customCommands.CustomCommand;
import de.SparkArmy.util.Utils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class UpdateSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "update-commands";
    }

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, @NotNull ConfigController controller) {
        ResourceBundle bundle = Utils.getResourceBundle(getName(), event.getUserLocale());

        if (controller.getMain().getCommandRegisterer().registerCommands()) {
            event.reply(bundle.getString("command.answer.successfully")).setEphemeral(true).queue();
        } else {
            event.reply(bundle.getString("command.answer.failed")).setEphemeral(true).queue();
        }
    }
}
