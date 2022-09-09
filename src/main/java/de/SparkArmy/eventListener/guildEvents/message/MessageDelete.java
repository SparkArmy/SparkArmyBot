package de.SparkArmy.eventListener.guildEvents.message;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.AuditLogUtil;
import de.SparkArmy.utils.ChannelUtil;
import de.SparkArmy.utils.LogChannelType;
import de.SparkArmy.utils.SqlUtil;
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

        EmbedBuilder logEmbed = new EmbedBuilder();
        logEmbed.setTitle("Message Delete");
        logEmbed.setDescription("Message was deleted");
        logEmbed.setAuthor(messageAuthor.getAsTag(),null,messageAuthor.getEffectiveAvatarUrl());

        User moderator = null;
        AuditLogEntry entry = AuditLogUtil.getAuditLogEntryByUser(messageAuthor, ActionType.MESSAGE_DELETE,event.getGuild());
        if (entry != null){
            moderator = entry.getUser();
        }

        if (moderator != null) logEmbed.appendDescription(String.format(" from %s (UserId:%s)",moderator.getAsTag(),moderator.getId()));

        String message = SqlUtil.getMessageContentFromMessageTable(event.getGuild(),event.getMessageId());

        if (!message.isEmpty()) {

            int i = 0;
            int j = 1;

            while (i < message.length()) {
                String substring = message.substring(i, message.length() - i < 1024 ? i + message.length() - i : i + 1023);
                i = message.length() - i < 1024 ? i + message.length() - i : i + 1023;
                logEmbed.addField("Part " + j, substring, false);
                j = j + 1;
            }
        }

        String attachments = getAttachments(event);

        if (!attachments.isEmpty()){
            logEmbed.addField("Attachments",attachments,false);
        }
        ChannelUtil.logInLogChannel(logEmbed,event.getGuild(), LogChannelType.MESSAGE);
    }

    private @NotNull User getUserFromMessageId(@NotNull MessageDeleteEvent event){
       String userId = SqlUtil.getUserIdFromMessageTable(event.getGuild(),event.getMessageId());
       User user = jda.getUserById(userId);
       if (user == null) return jda.getSelfUser();
       return user;
    }

    private @NotNull String getAttachments(@NotNull MessageDeleteEvent event){
        List<String> strings = SqlUtil.getAttachmentsFromMessage(event.getGuild(),event.getMessageId());
        StringBuilder builder = new StringBuilder();
        strings.forEach(x->builder.append(x).append("\n"));
        return builder.toString();
    }

}
