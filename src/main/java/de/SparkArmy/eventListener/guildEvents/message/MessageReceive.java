package de.SparkArmy.eventListener.guildEvents.message;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.SqlUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class MessageReceive extends CustomEventListener {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        putMessageInDatabase(event);
    }

    private void putMessageInDatabase(@NotNull MessageReceivedEvent event){
        if (!event.isFromGuild()) return;
        if (event.getGuild().equals(storageServer)) return;
        if (event.isWebhookMessage()) return;
        if (event.getAuthor().isBot()) return;
        if (event.getAuthor().isSystem()) return;
        if (event.getMember() == null) return;

        SqlUtil.putUserDataInUserTable(event.getGuild(),event.getMember());
        SqlUtil.putDataInMessageTable(event.getMessage());
        SqlUtil.putDataInMessageAttachmentsTable(event.getMessage());
    }
}
