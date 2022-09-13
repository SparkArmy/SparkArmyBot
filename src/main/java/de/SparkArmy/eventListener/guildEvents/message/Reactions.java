package de.SparkArmy.eventListener.guildEvents.message;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;

public class Reactions extends CustomEventListener {

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.isFromGuild()) return;

        String emojiMention;
        try {
            emojiMention = event.getEmoji().asCustom().getAsMention();
        }catch (IllegalStateException e){
            emojiMention = event.getEmoji().asUnicode().getAsReactionCode();
        }

        User user = event.retrieveUser().complete();
        if (user == null || user.isBot() || user.isSystem()) return;

        ChannelUtil.logInLogChannel(logEmbed(user,emojiMention,event.getJumpUrl(),"added"),event.getGuild(), LogChannelType.MESSAGE);
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!event.isFromGuild()) return;

        String emojiMention;
        try {
            emojiMention = event.getEmoji().asCustom().getAsMention();
        }catch (IllegalStateException e){
            emojiMention = event.getEmoji().asUnicode().getAsReactionCode();
        }

        User user = event.retrieveUser().complete();
        if (user == null || user.isBot() || user.isSystem()) return;

        ChannelUtil.logInLogChannel(logEmbed(user,emojiMention,event.getJumpUrl(),"remove"),event.getGuild(), LogChannelType.MESSAGE);
    }

    private @NotNull MessageEmbed logEmbed(@NotNull User user, String emoji, String jumpUrl, String action){
        EmbedBuilder logEmbed = new EmbedBuilder();
        logEmbed.setTitle("Reaction " + action,jumpUrl);
        logEmbed.addField("Emote",emoji,true);
        logEmbed.setAuthor(user.getAsTag(),null,user.getEffectiveAvatarUrl());
        logEmbed.setTimestamp(OffsetDateTime.now());
        logEmbed.setColor(new Color(0x6CE6FE));
        return logEmbed.build();
    }
}
