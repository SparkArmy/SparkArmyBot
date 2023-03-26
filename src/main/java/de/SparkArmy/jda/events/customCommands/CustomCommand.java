package de.SparkArmy.jda.events.customCommands;

import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public abstract class CustomCommand {
    public abstract String getName();

    public void dispatchSlashEvent(SlashCommandInteractionEvent event, ConfigController controller) {
    }

    public void dispatchUserContextEvent(UserContextInteractionEvent event, ConfigController controller) {
    }

    public void dispatchMessageContextEvent(MessageContextInteractionEvent event, ConfigController controller) {
    }

}
