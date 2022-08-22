package de.SparkArmy.utils.mediaOnlyUtils;

import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.JdaEventUtil;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Locale;

public class MediaOnlyUtil {

    public static void buttonDispatcher(@NotNull ButtonInteractionEvent event){
        String buttonId = event.getComponentId();
        if (!buttonId.contains(";")) return;
        if (event.getGuild() == null) return;
        String prefix = buttonId.split(";")[0];
        String suffix = buttonId.split(";")[1];
        if (prefix.contains(",")) return;
        if (!suffix.contains(",")) return;
        String userId = suffix.split(",")[1];
        String channelId = suffix.split(",")[0];

        if (!event.getUser().getId().equals(userId)) return;
        switch (prefix){
            case "addMediaOnly","editMediaOnly","removeMediaOnly" -> {
                if (channelId.equals("null")){
                    sendActionEmbed(event);
                }else {
                    sendChannelEmbed(event);
                }
            }
            case "nextMediaOnly" ->{
                MessageEmbed nextEmbed = event.getMessage().getEmbeds().get(0);
                if (nextEmbed.getTitle() == null) return;
                String actionString = nextEmbed.getTitle().split(" ")[0];

                TextInput channelIdInput = TextInput.create("channelId","Channel Id", TextInputStyle.SHORT)
                        .setRequired(true)
                        .setMinLength(5)
                        .build();

                Modal nextModal = Modal.create("nextModal," + actionString + ";" + suffix,"Channel Selection")
                        .addActionRows(ActionRow.of(channelIdInput))
                        .build();
                event.replyModal(nextModal).queue();
            }
            case "attachmentsMediaOnly","filesMediaOnly","textMediaOnly" -> {
                JSONObject config = MainUtil.controller.getSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN);
                JSONObject mediaConfig = config.getJSONObject("media-only");
                JSONObject content = mediaConfig.getJSONObject(channelId);

                Channel channel = event.getGuild().getGuildChannelById(channelId);

                boolean bool;
                switch (prefix){
                    case "attachmentsMediaOnly" -> {
                        bool = content.getBoolean("attachments");
                        content.put("attachments",!bool);
                    }
                    case "filesMediaOnly" -> {
                        bool = content.getBoolean("files");
                        content.put("files",!bool);
                    }
                    case "textMediaOnly" -> {
                        bool = content.getBoolean("text");
                        content.put("text",!bool);
                    }
                }

                mediaConfig.put(channelId,content);
                config.put("media-only",mediaConfig);

                MainUtil.controller.writeInSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN,config);

                //noinspection ConstantConditions
                String actionString = event.getMessage().getEmbeds().get(0).getTitle().split(" ")[1].toLowerCase(Locale.ROOT);

                //noinspection ConstantConditions
                event.editMessageEmbeds(MediaOnlyBuilder.channelEmbed(channel,MediaOnlyChannelActions.getActionByName(actionString),content))
                        .setComponents(MediaOnlyBuilder.actionRowForChannelEmbed(channel, event.getUser(), content)).queue();
            }
        }
    }


    public static void sendOverviewEmbed(@NotNull SlashCommandInteractionEvent event){
        OptionMapping channel = event.getOption("channel");
        String channelName = "null";
        String channelId = "null";
        if (channel != null){
            channelName = channel.getAsChannel().getName();
            channelId = channel.getAsChannel().getId();
        }
        event.replyEmbeds(MediaOnlyBuilder.overviewEmbed(channelName))
                .setComponents(MediaOnlyBuilder.actionRowOfOverviewEmbed(channelId,event.getUser()))
                .setEphemeral(true).queue();
    }

    public static void sendActionEmbed(@NotNull Event event){
        if (event.getClass().equals(SlashCommandInteractionEvent.class)){
          SlashCommandInteractionEvent slashEvent = getSlashEvent(event);
          //noinspection ConstantConditions
            slashEvent.replyEmbeds(MediaOnlyBuilder
                    .actionEmbed(MediaOnlyChannelActions.getActionByName(
                            slashEvent.getOption("action").getAsString())))
                    .setComponents(MediaOnlyBuilder.actionRowForActionEmbed(slashEvent.getUser()))
                    .setEphemeral(true).queue();
        } else if (event.getClass().equals(ButtonInteractionEvent.class)) {
            ButtonInteractionEvent buttonEvent = getButtonEvent(event);
            String buttonId = buttonEvent.getComponentId();
            if (!buttonId.contains(";") || !buttonId.contains(",")) return;
            buttonEvent.editMessageEmbeds(MediaOnlyBuilder.actionEmbed(MediaOnlyChannelActions.getActionByName(buttonEvent.getComponent().getLabel().toLowerCase(Locale.ROOT))))
                    .setComponents(MediaOnlyBuilder.actionRowForActionEmbed(buttonEvent.getUser()))
                    .queue();
        }
    }

    public static void sendChannelEmbed(@NotNull Event event){
        SlashCommandInteractionEvent slashEvent = JdaEventUtil.getSlashEvent(event);
        ButtonInteractionEvent buttonEvent = JdaEventUtil.getButtonEvent(event);
        ModalInteractionEvent modalEvent = JdaEventUtil.getModalEvent(event);
        if (slashEvent != null) {
            OptionMapping action = slashEvent.getOption("action");
            OptionMapping channel = slashEvent.getOption("channel");
            Guild guild = slashEvent.getGuild();

            // Inspections in MediaOnly
            //noinspection ConstantConditions
            MediaOnlyChannelActions type = MediaOnlyChannelActions.getActionByName(action.getAsString());
            //noinspection ConstantConditions
            Channel targetChannel = channel.getAsChannel();
            //noinspection ConstantConditions
            JSONObject config = MainUtil.controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);


            JSONObject mediaOnlyConfig;
            if (config.isNull("media-only")) {
                mediaOnlyConfig = new JSONObject();
            } else {
                mediaOnlyConfig = config.getJSONObject("media-only");
            }

            switch (type) {
                case ADD, EDIT -> {
                    if (mediaOnlyConfig.isEmpty() || mediaOnlyConfig.isNull(targetChannel.getId())) {
                        mediaOnlyConfig.put(targetChannel.getId(), new JSONObject() {{
                            put("attachments", true);
                            put("files", true);
                            put("text", false);
                        }});

                        config.put("media-only", mediaOnlyConfig);
                        MainUtil.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);
                    }

                    JSONObject channelConfig = mediaOnlyConfig.getJSONObject(targetChannel.getId());

                    slashEvent.replyEmbeds(MediaOnlyBuilder.channelEmbed(targetChannel, type, channelConfig))
                            .addComponents(MediaOnlyBuilder.actionRowForChannelEmbed(targetChannel, slashEvent.getUser(), channelConfig))
                            .setEphemeral(true).queue();
                }
                case REMOVE -> {
                    if (mediaOnlyConfig.isEmpty() || mediaOnlyConfig.isNull(targetChannel.getId())) {
                        slashEvent.reply("This channel is no media-only-channel").setEphemeral(true).queue();
                        return;
                    }

                    mediaOnlyConfig.remove(targetChannel.getId());
                    config.put("media-only", mediaOnlyConfig);
                    MainUtil.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);

                    slashEvent.reply("The channel " + targetChannel.getAsMention() + " was successful deleted from the media-only list").setEphemeral(true).queue();
                }
            }
        }

        if (buttonEvent != null){
            String buttonId = buttonEvent.getComponentId();
            String action = buttonId.split(";")[0].replaceAll("MediaOnly","");
            String channel = buttonId.split(";")[1].split(",")[0];
            Guild guild = buttonEvent.getGuild();

            //noinspection ConstantConditions
            JSONObject config = MainUtil.controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);

            MediaOnlyChannelActions type = MediaOnlyChannelActions.getActionByName(action);

            Channel targetChannel = guild.getGuildChannelById(channel);
            if (targetChannel == null) return;

            JSONObject mediaOnlyConfig;
            if (config.isNull("media-only")) {
                mediaOnlyConfig = new JSONObject();
            } else {
                mediaOnlyConfig = config.getJSONObject("media-only");
            }
            MainUtil.logger.info(action);
            switch (type) {
                case ADD, EDIT -> {
                    if (mediaOnlyConfig.isEmpty() || mediaOnlyConfig.isNull(targetChannel.getId())) {
                        mediaOnlyConfig.put(targetChannel.getId(), new JSONObject() {{
                            put("attachments", true);
                            put("files", true);
                            put("text", false);
                        }});

                        config.put("media-only", mediaOnlyConfig);
                        MainUtil.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);
                    }

                    JSONObject channelConfig = mediaOnlyConfig.getJSONObject(targetChannel.getId());

                    buttonEvent.editMessageEmbeds(MediaOnlyBuilder.channelEmbed(targetChannel, type, channelConfig))
                            .setComponents(MediaOnlyBuilder.actionRowForChannelEmbed(targetChannel, buttonEvent.getUser(), channelConfig))
                            .queue();
                }
                case REMOVE -> {
                    if (mediaOnlyConfig.isEmpty() || mediaOnlyConfig.isNull(targetChannel.getId())) {
                        buttonEvent.reply("This channel is no media-only-channel").setEphemeral(true).queue();
                        return;
                    }

                    mediaOnlyConfig.remove(targetChannel.getId());
                    config.put("media-only", mediaOnlyConfig);
                    MainUtil.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);

                    buttonEvent.reply("The channel " + targetChannel.getAsMention() + " was successful deleted from the media-only list").setEphemeral(true).queue();
                }
            }
        }

        if (modalEvent != null){
            String modalId = modalEvent.getModalId();
            if (!modalId.startsWith("nextModal")) return;
            if (!modalId.contains(";") || !modalId.contains(",")) return;
            String action = modalId.split(";")[0].split(",")[1].toLowerCase(Locale.ROOT);
            if (!modalEvent.getUser().getId().equals(modalId.split(";")[1].split(",")[1])) return;
            ModalMapping channelId = modalEvent.getValue("channelId");
            if (channelId == null) return;
            if (modalEvent.getGuild() == null) return;
            Guild guild = modalEvent.getGuild();
            Channel targetChannel = guild.getGuildChannelById(channelId.getAsString());

            JSONObject config = MainUtil.controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);

            MediaOnlyChannelActions type = MediaOnlyChannelActions.getActionByName(action);

            if (targetChannel == null) return;

            JSONObject mediaOnlyConfig;
            if (config.isNull("media-only")) {
                mediaOnlyConfig = new JSONObject();
            } else {
                mediaOnlyConfig = config.getJSONObject("media-only");
            }
            switch (type) {
                case ADD, EDIT -> {
                    if (mediaOnlyConfig.isEmpty() || mediaOnlyConfig.isNull(targetChannel.getId())) {
                        mediaOnlyConfig.put(targetChannel.getId(), new JSONObject() {{
                            put("attachments", true);
                            put("files", true);
                            put("text", false);
                        }});

                        config.put("media-only", mediaOnlyConfig);
                        MainUtil.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);
                    }

                    JSONObject channelConfig = mediaOnlyConfig.getJSONObject(targetChannel.getId());

                    modalEvent.editMessageEmbeds(MediaOnlyBuilder.channelEmbed(targetChannel, type, channelConfig))
                            .setComponents(MediaOnlyBuilder.actionRowForChannelEmbed(targetChannel, modalEvent.getUser(), channelConfig))
                            .queue();
                }
                case REMOVE -> {
                    if (mediaOnlyConfig.isEmpty() || mediaOnlyConfig.isNull(targetChannel.getId())) {
                        modalEvent.reply("This channel is no media-only-channel").setEphemeral(true).queue();
                        return;
                    }

                    mediaOnlyConfig.remove(targetChannel.getId());
                    config.put("media-only", mediaOnlyConfig);
                    MainUtil.controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, config);

                    modalEvent.reply("The channel " + targetChannel.getAsMention() + " was successful deleted from the media-only list").setEphemeral(true).queue();
                }
            }
        }
    }

    @Contract("_ -> new")
    private static @NotNull SlashCommandInteractionEvent getSlashEvent(@NotNull Event event){
        return new SlashCommandInteractionEvent(event.getJDA(), event.getResponseNumber(), ((SlashCommandInteractionEvent) event).getInteraction());
    }

    @Contract("_ -> new")
    private static @NotNull ButtonInteractionEvent getButtonEvent(@NotNull Event event){
        return new ButtonInteractionEvent(event.getJDA(), event.getResponseNumber(),((ButtonInteractionEvent) event).getInteraction());
    }
}
