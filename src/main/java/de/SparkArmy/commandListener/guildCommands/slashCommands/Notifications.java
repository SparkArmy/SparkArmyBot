package de.SparkArmy.commandListener.guildCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.notifications.NotificationType;
import de.SparkArmy.notifications.NotificationUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

public class Notifications extends CustomCommandListener {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("notifications")) return;
        if (event.getGuild() == null) return;

        OptionMapping type = event.getOption("notification");
        OptionMapping action = event.getOption("action");

        if (type== null && action == null){
            NotificationUtil.sendOverviewEmbed(event, null);
        }else if (type != null && action == null){
            NotificationUtil.sendOverviewEmbed(event,type);
        }else if (type == null){
            NotificationUtil.sendNotificationSelectEmbed(event);
        }else {
            String actionString = action.getAsString();
            switch (actionString) {
                case "add" -> NotificationUtil.sendAddModal(event);
                case "edit", "remove" -> NotificationUtil.sendEditNotificationSelectEmbed(event, NotificationType.getNotificationTypeByName(type.getAsString()),actionString);
            }
        }


    }
}
