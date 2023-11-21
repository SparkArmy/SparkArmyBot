package de.SparkArmy.jda.events.customEvents.otherEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.annotations.events.messageEvents.JDAMessageReceivedEvent;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class MediaOnlyFunction {
    private final ConfigController controller;

    public MediaOnlyFunction(@NotNull EventDispatcher eventDispatcher) {
        this.controller = eventDispatcher.getController();
    }

    @JDAMessageReceivedEvent
    public void messageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;
        if (event.isWebhookMessage()) return;

        // TODO Check have Author Mod-Roles

        JSONObject channelPermissions = controller.getGuildMediaOnlyChannelPermissions(event.getChannel().getIdLong());

        if (channelPermissions.isEmpty()) return;

        boolean textPerms = channelPermissions.getBoolean("permText");
        boolean attachmentPerms = channelPermissions.getBoolean("permAttachment");
        boolean filePerms = channelPermissions.getBoolean("permFiles");
        boolean linkPerms = channelPermissions.getBoolean("permLinks");

        Message message = event.getMessage();

        boolean hasText = !message.getContentRaw().isEmpty();
        boolean hasAttachments = !message.getAttachments().stream().filter(x -> x.isImage() || x.isVideo()).toList().isEmpty();
        boolean hasFiles = !message.getAttachments().stream().filter(x -> !x.isImage() || !x.isVideo()).toList().isEmpty();
        String linkRegex = "(?:(?:https?|ftp)://|\\b[a-z\\d]+\\.)(?:(?:[^\\s()<>]+|\\((?:[^\\s()<>]+|\\([^\\s()<>]+\\))?\\))+(?:\\((?:[^\\s()<>]+|\\(?:[^\\s()<>]+\\))?\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))?";
        boolean hasLinks = message.getContentRaw().matches(linkRegex);

        ResourceBundle bundle = Util.getResourceBundle("mediaOnlyFunction", event.getGuild().getLocale());
        if (!textPerms && !hasLinks && hasText) {
            message.delete().reason("MediaOnlyChannel").queueAfter(1, TimeUnit.SECONDS);
            message.reply(bundle.getString("textPermsViolation"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            return;
        }
        if (!attachmentPerms && hasAttachments && !hasFiles) {
            message.delete().reason("MediaOnlyChannel").queueAfter(1, TimeUnit.SECONDS);
            message.reply(bundle.getString("attachmentPermsViolation"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            return;
        }
        if (!filePerms && hasFiles) {
            message.delete().reason("MediaOnlyChannel").queueAfter(1, TimeUnit.SECONDS);
            message.reply(bundle.getString("filePermsViolation"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            return;
        }
        if (!linkPerms && hasLinks) {
            message.delete().reason("MediaOnlyChannel").queueAfter(1, TimeUnit.SECONDS);
            message.reply(bundle.getString("linkPermsViolation"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
        }
    }
}
