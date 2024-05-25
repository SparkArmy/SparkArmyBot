package de.SparkArmy.jda.events.customEvents.otherEvents;

import club.minnced.discord.webhook.WebhookClient;
import de.SparkArmy.config.ConfigController;
import de.SparkArmy.db.DatabaseAction;
import de.SparkArmy.jda.WebhookApi;
import de.SparkArmy.jda.annotations.internal.JDAEvent;
import de.SparkArmy.jda.events.EventManager;
import de.SparkArmy.jda.events.iEvent.IJDAEvent;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class MessageEvents implements IJDAEvent {

    private final DatabaseAction db;

    private final List<Long> modRoleIds;
    private final WebhookApi webhookApi;
    private final ConfigController controller;

    public MessageEvents(@NotNull EventManager dispatcher) {
        this.controller = dispatcher.getController();
        this.webhookApi = dispatcher.getApi().getWebhookApi();
        this.db = new DatabaseAction();
        this.modRoleIds = db.getModerationRoles();
    }

    private ResourceBundle bundle(DiscordLocale locale) {
        return Util.getResourceBundle("messageEvents", locale);
    }

    @JDAEvent
    public void messageBulkDeleteEvent(@NotNull MessageBulkDeleteEvent event) {
        removeDataFromDatabase(event.getMessageIds().stream().map(Long::parseLong).toList());
    }

    @JDAEvent
    public void messageDeleteEvent(@NotNull MessageDeleteEvent event) {
        removeDataFromDatabase(Collections.singletonList(event.getMessageIdLong()));
    }

    @JDAEvent
    public void messageReactionReactionRemoveAllEvent(MessageReactionRemoveAllEvent event) {
    }

    @JDAEvent
    public void messageReactionRemoveEmojiEvent(MessageReactionRemoveEmojiEvent event) {
    }

    @JDAEvent
    public void messageUpdateEvent(@NotNull MessageUpdateEvent event) {
        putDataInDatabase(event.getMessage());

    }

    @JDAEvent
    public void messageReceivedEvent(@NotNull MessageReceivedEvent event) {
        putDataInDatabase(event.getMessage());
        mediaOnlyFunction(event);
        blacklistFunction(event);
        regexFunction(event);
    }


    private synchronized void removeDataFromDatabase(List<Long> ids) {
        db.removeMessageFromDatabase(ids);
    }

    private void putDataInDatabase(@NotNull Message message) {
        if (message.getAuthor().isBot() || message.getAuthor().isSystem() || !message.isFromGuild()) return;
        db.writeInMessageTable(message);
    }

    private void mediaOnlyFunction(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;
        if (event.isWebhookMessage()) return;

        if (checkMemberPermissions(event)) return;

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

        ResourceBundle bundle = bundle(event.getGuild().getLocale());
        if (!textPerms && !hasLinks && hasText) {
            message.delete().reason("MediaOnlyChannel").queueAfter(1, TimeUnit.SECONDS);
            message.reply(bundle.getString("mediaOnlyFunction.textPermsViolation"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            return;
        }
        if (!attachmentPerms && hasAttachments && !hasFiles) {
            message.delete().reason("MediaOnlyChannel").queueAfter(1, TimeUnit.SECONDS);
            message.reply(bundle.getString("mediaOnlyFunction.attachmentPermsViolation"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            return;
        }
        if (!filePerms && hasFiles) {
            message.delete().reason("MediaOnlyChannel").queueAfter(1, TimeUnit.SECONDS);
            message.reply(bundle.getString("mediaOnlyFunction.filePermsViolation"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            return;
        }
        if (!linkPerms && hasLinks) {
            message.delete().reason("MediaOnlyChannel").queueAfter(1, TimeUnit.SECONDS);
            message.reply(bundle.getString("mediaOnlyFunction.linkPermsViolation"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
        }
    }

    private void blacklistFunction(@NotNull MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw();
        if (messageContent.isBlank()) return;
        if (checkMemberPermissions(event)) return;

        JSONObject phrases = controller.getGuildBlacklistPhrases(event.getGuild());

        if (phrases.keySet().stream().map(phrases::getString).anyMatch(messageContent::contains)) {
            event.getMessage().delete().reason("Blacklist phrase")
                    .map(x -> {
                        WebhookClient client = webhookApi.getSpecificWebhookClient(event.getGuild(), LogChannelType.MOD);
                        if (client != null)
                            client.send(String.format("Blacklist Entry (%s) used from %s", messageContent, event.getAuthor().getAsMention()));
                        return null;
                    })
                    .queue(null,
                            new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
        }
    }

    private void regexFunction(@NotNull MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw();
        if (messageContent.isBlank()) return;
        if (checkMemberPermissions(event)) return;


        JSONObject regexEntries = controller.getGuildRegexEntries(event.getGuild());
        if (regexEntries.keySet().stream().map(x -> {
            JSONObject jObj = regexEntries.getJSONObject(x);
            return jObj.getString("regex");
        }).anyMatch(messageContent::matches)) {
            event.getMessage().delete().reason("Regex Phrase")
                    .map(x -> {
                        WebhookClient client = webhookApi.getSpecificWebhookClient(event.getGuild(), LogChannelType.MOD);
                        if (client != null)
                            client.send(String.format("Regex Entry (%s) used from %s", messageContent, event.getAuthor().getAsMention()));
                        return null;
                    })
                    .queue(null,
                            new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
        }
    }

    private boolean checkMemberPermissions(@NotNull MessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null) return false;
        return modRoleIds.stream().anyMatch(x -> member.getRoles().stream().map(ISnowflake::getIdLong).toList().contains(x));
    }

    @Override
    public Class<?> getEventClass() {
        return this.getClass();
    }
}


