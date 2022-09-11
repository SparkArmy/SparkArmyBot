package de.SparkArmy.utils.jda.mediaOnlyUtils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class MediaOnlyBuilder {
    private static final Color color = new Color(0xB40420);
    protected static @NotNull MessageEmbed overviewEmbed(@NotNull String channelName){
        EmbedBuilder embed =  new EmbedBuilder(){{
            setTitle("MediaOnlyChannel Actions");
            setDescription("Please push one button below to execute the provided action");
            if (!channelName.equals("null")) appendDescription(" for the channel: " + channelName);
            addField(MediaOnlyChannelActions.ADD.getName(), MediaOnlyChannelActions.ADD.getDescription(), false);
            addField(MediaOnlyChannelActions.EDIT.getName(), MediaOnlyChannelActions.EDIT.getDescription(), false);
            addField(MediaOnlyChannelActions.REMOVE.getName(), MediaOnlyChannelActions.REMOVE.getDescription(), false);
            setColor(color);
        }};
        return embed.build();
    }

    @Contract("_ -> new")
    protected static @NotNull MessageEmbed actionEmbed(@NotNull MediaOnlyChannelActions actionType){
        EmbedBuilder embed = new EmbedBuilder(){{
            setTitle(actionType.getName() + " MediaOnlyChannel");
            setTitle(actionType.getDescription());
            setColor(color);
        }};
        return embed.build();
    }

    protected static @NotNull MessageEmbed channelEmbed(@NotNull Channel channel, MediaOnlyChannelActions actionType, @NotNull JSONObject content){
        EmbedBuilder embed = new EmbedBuilder(){{
            setTitle(channel.getName() + " " + actionType + " Embed");
            setDescription("This is the mediaOnlyConfig from " + channel.getAsMention());
            appendDescription("\nThe fields below show you what types are enabled");
            setColor(color);
            addField("Attachments", String.valueOf(content.getBoolean("attachments")),false);
            addField("Files", String.valueOf(content.getBoolean("files")),false);
            addField("Text",String.valueOf(content.getBoolean("text")),false);
        }};

        return embed.build();
    }

    protected static @NotNull ActionRow actionRowOfOverviewEmbed(String channelName, @NotNull User user){
        String suffix = String.format("%s,%s",channelName,user.getId());
        return ActionRow.of(
                Button.primary(String.format("%sMediaOnly;%s",MediaOnlyChannelActions.ADD.getName(),suffix),"Add"),
                Button.primary(String.format("%sMediaOnly;%s",MediaOnlyChannelActions.EDIT.getName(),suffix),"Edit"),
                Button.danger(String.format("%sMediaOnly;%s",MediaOnlyChannelActions.REMOVE.getName(),suffix),"Remove")
        );
    }

    protected static @NotNull ActionRow actionRowForActionEmbed(@NotNull User user){
        String suffix = String.format("%s,%s","null",user.getId());
        return ActionRow.of(
                Button.success("nextMediaOnly;" + suffix,"Next")
        );
    }

    protected static @NotNull ActionRow actionRowForChannelEmbed(@NotNull Channel channel, @NotNull User user, @NotNull JSONObject content){
        String suffix = String.format("%s,%s",channel.getId(),user.getId());
        Collection<Button> buttons = new ArrayList<>();
        boolean attachments = content.getBoolean("attachments");
        boolean files = content.getBoolean("files");
        boolean text = content.getBoolean("text");

        if (attachments){
            buttons.add(Button.success("attachmentsMediaOnly;" + suffix,"Attachments"));
        }else {
            buttons.add(Button.secondary("attachmentsMediaOnly;" + suffix,"Attachments"));
        }

        if (files){
            buttons.add(Button.success("filesMediaOnly;" + suffix,"Files"));
        }else {
            buttons.add(Button.secondary("filesMediaOnly;" + suffix,"Files"));
        }

        if (text){
            buttons.add(Button.success("textMediaOnly;" + suffix,"Text"));
        }else {
            buttons.add(Button.secondary("textMediaOnly;" + suffix,"Text"));
        }

        return ActionRow.of(buttons);
    }
}
