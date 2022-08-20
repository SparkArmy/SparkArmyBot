package de.SparkArmy.eventListener.guildEvents.commands;

import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.mediaOnlyUtils.MediaOnlyUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class MediaOnlyListener extends CustomEventListener {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        MediaOnlyUtil.buttonDispatcher(event);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        MediaOnlyUtil.sendChannelEmbed(event);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getMessage().isWebhookMessage() || event.getMessage().isEphemeral()) return;
        if (event.getAuthor().isBot()) return;
        JSONObject mediaContent = MainUtil.controller.getSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN).optJSONObject("media-only");
        if (mediaContent == null) return;
        if (!mediaContent.keySet().contains(event.getChannel().getId())) return;

        JSONObject channelConfig = mediaContent.getJSONObject(event.getChannel().getId());

        boolean attachmentsEnabled = channelConfig.getBoolean("attachments");
        boolean filesEnabled = channelConfig.getBoolean("files");
        boolean textEnabled = channelConfig.getBoolean("text");

        Message message = event.getMessage();

        if (!attachmentsEnabled){
            new Thread(()->{
            if (message.getAttachments().stream().filter(x-> x.isImage() || x.isVideo()).toList().isEmpty()) return;
            message.reply("You can't send Attachments in this channel").mentionRepliedUser(true).queue(x->{
                message.delete().queue();
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                x.delete().queue();
            });
            }).start();
        }

        if (!filesEnabled){
            new Thread(()->{
            if (message.getAttachments().isEmpty()) return;
            if (message.getAttachments().stream().filter(x-> !x.isImage() && !x.isVideo()).toList().isEmpty()) return;
            message.reply("You can't send Files in this channel").mentionRepliedUser(true).queue(x->{
                message.delete().queue();
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                x.delete().queue();
            });
            }).start();
        }

        if (!textEnabled){
            new Thread(()->{
            if (message.getContentRaw().isEmpty()) return;
            if (!message.getAttachments().isEmpty()) return;
            message.reply("You can't send TextMessages in this channel").mentionRepliedUser(true).queue(x->{
                message.delete().queue();
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                x.delete().queue();
            });
            }).start();
        }
    }
}
