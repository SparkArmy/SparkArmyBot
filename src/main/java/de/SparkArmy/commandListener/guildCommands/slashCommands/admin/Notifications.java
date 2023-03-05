package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.notifications.NotificationType;
import de.SparkArmy.notifications.NotificationUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Notifications extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        if (event.getGuild() == null) return;

        OptionMapping type = event.getOption("notification");
        OptionMapping action = event.getOption("action");

        if (type == null && action == null) {
            NotificationUtil.sendOverviewEmbed(event, null);
        } else if (type != null && action == null) {
            NotificationUtil.sendOverviewEmbed(event, type);
        } else if (type == null) {
            NotificationUtil.sendNotificationSelectEmbed(event);
        } else {
            String actionString = action.getAsString();
            switch (actionString) {
                case "add" -> NotificationUtil.sendAddModal(event);
                case "edit", "remove" ->
                        NotificationUtil.sendEditNotificationSelectEmbed(event, NotificationType.getNotificationTypeByName(type.getAsString()), actionString);
            }
        }
    }

    @Override
    public String getName() {
        return "notifications";
    }
}
