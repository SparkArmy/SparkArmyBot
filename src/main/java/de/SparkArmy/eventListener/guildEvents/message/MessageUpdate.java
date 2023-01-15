package de.SparkArmy.eventListener.guildEvents.message;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.PostgresConnection;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import org.jetbrains.annotations.NotNull;

public class MessageUpdate extends CustomEventListener {
    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        logUpdateMessage(event);
    }

    private void logUpdateMessage(@NotNull MessageUpdateEvent event){
        if (event.getAuthor().isSystem() || event.getAuthor().isBot()) return;

        EmbedBuilder newEmbed = new EmbedBuilder();
        EmbedBuilder oldEmbed = new EmbedBuilder();

        String newMessage = event.getMessage().getContentRaw();
        String oldMessage = PostgresConnection.getMessageContentByMessageId(event.getMessageIdLong());
        int i = 0;
        int j = 1;

        while (oldMessage != null && i < oldMessage.length()){
            String substring = oldMessage.substring(i,oldMessage.length()-i<1024 ? i + oldMessage.length() - i :i + 1023);
            i = oldMessage.length()-i<1024 ? i + oldMessage.length() - i : i + 1023;
            oldEmbed.addField("Old Part " + j,substring,false);
            j = j+1;
        }

        i = 0;
        j = 1;

        while (i < newMessage.length()){
            String substring = newMessage.substring(i,newMessage.length()-i<1024 ? i + newMessage.length() - i :i + 1023);
            i = newMessage.length()-i<1024 ? i + newMessage.length() - i : i + 1023;
            newEmbed.addField("New Part " + j,substring,false);
            j = j+1;
        }

        newEmbed.setTitle("Message Update");
        newEmbed.setDescription(String.format("This is the new content from [%s](%s)",event.getMessage().getId(),event.getMessage().getJumpUrl()));
        newEmbed.setAuthor(event.getAuthor().getAsTag(),null,event.getAuthor().getEffectiveAvatarUrl());

        oldEmbed.setTitle("Message Update");
        oldEmbed.setDescription(String.format("This is the old content from [%s](%s)",event.getMessage().getId(),event.getMessage().getJumpUrl()));
        oldEmbed.setAuthor(event.getAuthor().getAsTag(),null,event.getAuthor().getEffectiveAvatarUrl());

        ChannelUtil.logInLogChannel(oldEmbed,event.getGuild(),LogChannelType.MESSAGE);
        ChannelUtil.logInLogChannel(newEmbed,event.getGuild(), LogChannelType.MESSAGE);
        updateDataInDatabase(event);
    }

    private void updateDataInDatabase(@NotNull MessageUpdateEvent event) {
        if (!event.isFromGuild()) return;
        if (event.getGuild().equals(storageServer)) return;
        if (event.getAuthor().isBot()) return;
        if (event.getAuthor().isSystem()) return;
        if (event.getMember() == null) return;

        PostgresConnection.putDataInMemberTable(event.getMember());

        PostgresConnection.updateDataInMessageTable(event.getMessage());
    }
}
