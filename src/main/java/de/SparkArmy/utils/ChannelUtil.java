package de.SparkArmy.utils;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public class ChannelUtil {
    private static final JDA jda = MainUtil.jda;
    private static final ConfigController controller = MainUtil.controller;

    public static void sendMessageInRightChannel(@NotNull Object value, Channel channel){
        Class<?> targetClass = value.getClass();
        MessageChannel messageChannel = rightChannel(channel);
        if (messageChannel == null) return;

        if (EmbedBuilder.class.equals(targetClass)) {
            MessageEmbed embed = ((EmbedBuilder) value).build();
            messageChannel.sendMessageEmbeds(embed).queue();
        } else if (MessageBuilder.class.equals(targetClass)) {
            Message message = ((MessageBuilder) value).build();
            messageChannel.sendMessage(message).queue();
        } else if (String.class.equals(targetClass)) {
            String string = (String) value;
            messageChannel.sendMessage(string).queue();
        }
    }

    public static @Nullable MessageChannel rightChannel(@NotNull Channel channel){
        String channelId = channel.getId();
        switch (channel.getType()){
            case NEWS ->{
                return jda.getNewsChannelById(channelId);
            }
            case TEXT -> {
                return jda.getTextChannelById(channelId);
            }
            case VOICE -> {
                return jda.getVoiceChannelById(channelId);
            }
            case GUILD_NEWS_THREAD,GUILD_PRIVATE_THREAD,GUILD_PUBLIC_THREAD  -> {
                return jda.getThreadChannelById(channelId);
            }
            default -> {
                MainUtil.logger.info("The current Channel is from type:\n" + channel.getType());
                return null;
            }
        }
    }

    public static TextChannel createTextChannel(@NotNull Guild guild, @NotNull String channelName){
        return guild.createTextChannel(channelName).complete();
    }

    public static TextChannel createTextChannel(@NotNull Category category, @NotNull String channelName){
        return category.createTextChannel(channelName).complete();
    }

    public static VoiceChannel createVoiceChannel(@NotNull Guild guild, @NotNull String channelName){
        return guild.createVoiceChannel(channelName).complete();
    }

    public static VoiceChannel createVoiceChannel(@NotNull Category category, @NotNull String channelName){
        return category.createVoiceChannel(channelName).complete();
    }

    public static StageChannel createStageChannel(@NotNull Guild guild, @NotNull String channelName){
        return guild.createStageChannel(channelName).complete();
    }

    public static StageChannel createStageChannel(@NotNull Category category, @NotNull String channelName){
        return category.createStageChannel(channelName).complete();
    }

    public static Category createCategory(@NotNull Guild guild, @NotNull String channelName){
        return guild.createCategory(channelName).complete();
    }

    public static NewsChannel createNewsChannel(@NotNull Guild guild, @NotNull String channelName){
        return guild.createNewsChannel(channelName).complete();
    }

    public static ThreadChannel createThreadChannel(@NotNull TextChannel textChannel, @NotNull String channelName){
        return textChannel.createThreadChannel(channelName).complete();
    }

    private static JSONObject getLogChannel(Guild guild){
        JSONObject config = controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
        if (config.isNull("log-channel")){
            config.put("log-channel",new JSONObject());
        }
        return config.getJSONObject("log-channel");
    }

    private static void writeNewLogChannelsInConfig(Guild guild,JSONObject logChannel){
       JSONObject config = controller.getSpecificGuildConfig(guild,GuildConfigType.MAIN);
       config.put("log-channel",logChannel);
       controller.writeInSpecificGuildConfig(guild,GuildConfigType.MAIN,config);
    }

    @Contract(" -> new")
    private static @NotNull Collection<Permission> standardDeniedPermissions(){
        return new ArrayList<>(){{add(Permission.VIEW_CHANNEL);}};
    }

    public static void logInLogChannel(Object value, Guild guild, @NotNull LogChannelType channel){
        String channelName = channel.getName();
       JSONObject logChannel = getLogChannel(guild);
        Category category;
       if (logChannel.isNull("log-category")) {
           category = createCategory(guild, "LOGCHANNEL");
           category.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, standardDeniedPermissions()).queue();
           logChannel.put("log-category",category.getId());
           writeNewLogChannelsInConfig(guild,logChannel);
       }
        category = guild.getCategoryById(logChannel.getString("log-category"));
        if (category == null){
            category = createCategory(guild, "LOGCHANNEL");
            category.getManager().putRolePermissionOverride(guild.getPublicRole().getIdLong(), null, standardDeniedPermissions()).queue();
            logChannel.put("log-category",category.getId());
            writeNewLogChannelsInConfig(guild,logChannel);
        }


        GuildChannel guildChannel;
        if (logChannel.isNull(String.valueOf(channelName))) {
           guildChannel = createTextChannel(category, String.valueOf(channelName));
           logChannel.put(String.valueOf(channelName),guildChannel.getId());
           writeNewLogChannelsInConfig(guild,logChannel);
        }

        guildChannel = guild.getGuildChannelById(logChannel.getString(channelName));
        if (guildChannel == null){
            guildChannel = createTextChannel(category,String.valueOf(channelName));
            logChannel.put(String.valueOf(channelName),guildChannel.getId());
            writeNewLogChannelsInConfig(guild,logChannel);
        }

       sendMessageInRightChannel(value,guildChannel);
    }


}
