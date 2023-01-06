package de.SparkArmy.eventListener.guildEvents.message;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.PostgresConnection;
import de.SparkArmy.utils.jda.AuditLogUtil;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageDelete extends CustomEventListener {
    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        logDeleteMessage(event);
    }

    private void logDeleteMessage(@NotNull MessageDeleteEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getGuild().equals(storageServer)) return;

        User messageAuthor = getUserFromMessageId(event);
        if (messageAuthor.isBot()) return;

        EmbedBuilder messageLogEmbed = new EmbedBuilder();
        messageLogEmbed.setTitle("Message Deleted");
        messageLogEmbed.setDescription("Message was deleted");
        messageLogEmbed.setAuthor(messageAuthor.getAsTag(),null,messageAuthor.getEffectiveAvatarUrl());

        EmbedBuilder attachmentLogEmbed = new EmbedBuilder();
        attachmentLogEmbed.setTitle("Message Deleted");
        attachmentLogEmbed.setDescription("Message was deleted");
        attachmentLogEmbed.setAuthor(messageAuthor.getAsTag(),null,messageAuthor.getEffectiveAvatarUrl());

        User moderator = null;
        AuditLogEntry entry = AuditLogUtil.getAuditLogEntryByUser(messageAuthor, ActionType.MESSAGE_DELETE,event.getGuild());
        if (entry != null){
            moderator = entry.getUser();
        }

        if (moderator != null){
            messageLogEmbed.appendDescription(String.format(" from %s (UserId:%s)",moderator.getAsTag(),moderator.getId()));
            attachmentLogEmbed.appendDescription(String.format(" from %s (UserId:%s)",moderator.getAsTag(),moderator.getId()));
        }

        String message = PostgresConnection.getMessageContentByMessageId(event.getMessageIdLong());

        if (!message.isEmpty()) {

            int i = 0;
            int j = 1;

            while (i < message.length()) {
                String substring = message.substring(i, message.length() - i < 1024 ? i + message.length() - i : i + 1023);
                i = message.length() - i < 1024 ? i + message.length() - i : i + 1023;
                messageLogEmbed.addField("Part " + j, substring, false);
                j = j + 1;
            }
        }

        List<String> attachments = PostgresConnection.getMessageAttachmentUrlsByDiscordMessageID(event.getMessageIdLong());
        if (!attachments.isEmpty() && attachments.size() <= 5){
            StringBuilder builder = new StringBuilder();
            attachments.forEach(x->builder.append(x).append("\n"));
            messageLogEmbed.addField("Attachments",builder.toString(),false);
        }else {
            int i = 0;
            while (i < attachments.size()) {
                List<String> sublist = attachments.subList(i, attachments.size() - i <= 5 ? i + attachments.size() - i : i + 5);
                i = attachments.size() - i <= 5 ? i + attachments.size() : i + 5;
                StringBuilder builder = new StringBuilder();
                sublist.forEach(x -> builder.append(x).append("\n"));
                attachmentLogEmbed.addField("Attachments", builder.toString(), false);
            }
        }

        if (!messageLogEmbed.getFields().isEmpty()) ChannelUtil.logInLogChannel(messageLogEmbed,event.getGuild(), LogChannelType.MESSAGE);
        if (!attachmentLogEmbed.getFields().isEmpty()) ChannelUtil.logInLogChannel(attachmentLogEmbed,event.getGuild(),LogChannelType.MESSAGE);
    }

    private @NotNull User getUserFromMessageId(@NotNull MessageDeleteEvent event){
       long userId = PostgresConnection.getUserIdByMessageId(event.getMessageIdLong());
       if (userId == 0) return jda.getSelfUser();
       User user = jda.getUserById(userId);
       if (user == null) return jda.getSelfUser();
       return user;
    }


}
