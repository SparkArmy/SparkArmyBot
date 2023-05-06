package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.events.annotations.JDAAutoComplete;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.utils.NotificationService;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class NotificationSlashCommandEvents {

    private final ConfigController controller;
    private final Postgres db;


    public NotificationSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
        this.db = controller.getMain().getPostgres();
    }

    @JDAAutoComplete(commandName = "notification")
    public void notificationPlatformAutocomplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        List<String> values = Arrays.stream(NotificationService.values()).toList().stream().map(NotificationService::getServiceName).toList();
        event.replyChoiceStrings(values).queue();
    }
}
