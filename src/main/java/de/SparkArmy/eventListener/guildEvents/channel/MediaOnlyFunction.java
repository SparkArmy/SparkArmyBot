package de.SparkArmy.eventListener.guildEvents.channel;

import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class MediaOnlyFunction extends CustomEventListener {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getMessage().isWebhookMessage() || event.getMessage().isEphemeral()) return;
        if (event.getMember() == null) return;
        if (event.getMember().hasPermission(Permission.BAN_MEMBERS)) return;
        if (event.getAuthor().isBot()) return;
        JSONObject mediaContent = MainUtil.controller.getSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN).optJSONObject("media-only");
        if (mediaContent == null) return;
        if (!mediaContent.keySet().contains(event.getChannel().getId())) return;

        JSONObject channelConfig = mediaContent.getJSONObject(event.getChannel().getId());

        boolean attachmentsEnabled = channelConfig.getBoolean("attachments");
        boolean filesEnabled = channelConfig.getBoolean("files");
        boolean textEnabled = channelConfig.getBoolean("text");
        boolean linksEnabled = channelConfig.getBoolean("links");

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
                @SuppressWarnings({"ConvertToBasicLatin", "RegExpRedundantEscape", "RegExpUnnecessaryNonCapturingGroup"})
                String regex = "(?:(?:https?|ftp):\\/\\/|\\b(?:[a-z\\d]+\\.))(?:(?:[^\\s()<>]+|\\((?:[^\\s()<>]+|(?:\\([^\\s()<>]+\\)))?\\))+(?:\\((?:[^\\s()<>]+|(?:\\(?:[^\\s()<>]+\\)))?\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))?";
                if (linksEnabled && message.getContentRaw().matches(regex)) return;
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
