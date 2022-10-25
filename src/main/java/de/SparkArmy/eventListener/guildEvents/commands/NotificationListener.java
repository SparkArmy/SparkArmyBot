package de.SparkArmy.eventListener.guildEvents.commands;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.notifications.NotificationType;
import de.SparkArmy.notifications.NotificationUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class NotificationListener extends CustomEventListener {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;
        String buttonId = event.getComponentId();
        if (!buttonId.contains(";")) return;
        String eventName = buttonId.split(";")[0];
        String userId = event.getUser().getId();
        boolean actionCondition = eventName.equals("addNotification") || eventName.equals("editNotification") || eventName.equals("removeNotification");
        boolean notificationCondition = eventName.equals("youtubeNotification") || eventName.equals("twitchNotification") || eventName.equals("twitterNotification");
        if (buttonId.contains(",")){
            if (!userId.equals(buttonId.split(";")[1].split(",")[0])) return;
            if (actionCondition) {
                if (eventName.equals("addNotification")) NotificationUtil.sendAddModal(event);
                else if (eventName.equals("editNotification")) NotificationUtil.sendEditNotificationSelectEmbed(event,NotificationType.getNotificationTypeByName(buttonId.split(";")[1].split(",")[1].toLowerCase(Locale.ROOT)),"edit");
                else NotificationUtil.sendEditNotificationSelectEmbed(event,NotificationType.getNotificationTypeByName(buttonId.split(";")[1].split(",")[1].toLowerCase(Locale.ROOT)),"remove");
            }
            if (notificationCondition){
                String action = buttonId.split(";")[1].split(",")[1];
                switch (action) {
                    case "add" -> NotificationUtil.sendAddModal(event);
                    case "edit" ->
                            NotificationUtil.sendEditNotificationSelectEmbed(event, NotificationType.getNotificationTypeByName(eventName.replace("Notification", "")),"edit");
                    case "remove" ->
                            NotificationUtil.sendEditNotificationSelectEmbed(event, NotificationType.getNotificationTypeByName(eventName.replace("Notification", "")),"remove");
                }
            }

            if (eventName.equals("nextNotificationEmbed") || eventName.equals("beforeNotificationEmbed")){
                if (!userId.equals(buttonId.split(";")[1].split(",")[0])) return;
                NotificationUtil.editNotificationSelectEmbed(event);
            }
        }else {
            if (!userId.equals(buttonId.split(";")[1])) return;
            if (actionCondition){
                NotificationUtil.sendNotificationSelectEmbed(event);
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("twitterNotification") && !event.getModalId().startsWith("twitchNotification") && !event.getModalId().startsWith("youtubeNotification")) return;
        NotificationUtil.addOrEditNotificationEntry(event);
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (event.getGuild() == null) return;
        String menuName = event.getComponentId();
        if (!menuName.startsWith("notificationSelectMenu")) return;
        NotificationUtil.sendEditModalOrDeleteEntry(event);
    }
}
