package de.SparkArmy.notifications;

import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.jda.JdaEventUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static de.SparkArmy.notifications.NotificationBuilders.actionSelectButtonCollection;
import static de.SparkArmy.notifications.NotificationBuilders.notificationSelectButtonCollection;

public class NotificationUtil {

    private final static File directory = FileHandler.getDirectoryInUserDirectory("botstuff/notifications");

    public static void checkForTwitchStreams(@NotNull JSONObject watchlist, Guild guild){
        if (watchlist.isEmpty()) return;
        watchlist.keySet().forEach(streamer->{
            JSONObject streamData = TwitchApi.getStreamDataByUserId(streamer);
            JSONObject userData = TwitchApi.getUserDataByUserName(watchlist.getJSONObject(streamer).getString("userName"));
            if (streamData == null || userData == null) return;
           String lastStreamOnline = streamData.getString("started_at");
            if (!TwitchApi.isUserNewLive(streamer, lastStreamOnline,guild)) return;
            JSONObject content = watchlist.getJSONObject(streamer);
            content.getJSONArray("channel").forEach(channel->{
                Channel targetChannel = MainUtil.jda.getGuildChannelById(channel.toString());
                if (targetChannel == null) return;
                ChannelUtil.sendMessageInRightChannel(NotificationBuilders.twitchNotification(streamData,userData,content,guild),targetChannel);
            });
        });
    }

    public static void checkForTweets(@NotNull JSONObject watchlist, Guild guild){
        if (watchlist.isEmpty()) return;
        if (directory == null) return;
        File file = FileHandler.getFileInDirectory(directory,guild.getId() + ".json");
        if (!file.exists()) return;
        JSONObject config = getGuildNotifications(guild);
        if (config == null) return;
        if (config.isEmpty()) return;
        if (config.isNull("latestActions")){
            config.put("latestActions",new JSONObject(){{
                put("twitter",new JSONObject());
            }});
        }
        JSONObject actions = config.getJSONObject("latestActions");
        if (actions.isNull("twitter")){
            actions.put("twitter",new JSONObject());
        }
        JSONObject twitter = actions.getJSONObject("twitter");
        watchlist.keySet().forEach(user->{
            JSONObject userContent = watchlist.getJSONObject(user);
            String tweetId = TwitterApi.getLatestTweetFromUser(user);
            if (tweetId == null) return;
            if (twitter.isNull(user)){
                twitter.put(user,tweetId);
            }else if (!twitter.getString(user).equals(tweetId)){
                twitter.put(user,tweetId);
            }else {
                return;
            }

            actions.put("twitter",twitter);
            config.put("latestActions",actions);
            FileHandler.writeValuesInFile(file,config);

            StringBuilder roles = new StringBuilder();
            userContent.getJSONArray("roles").forEach(x->{
                Role role = guild.getRoleById(x.toString());
                if (role == null) return;
                roles.append(role.getAsMention()).append(",");
            });
            roles.deleteCharAt(roles.length()-1);

            String link = "https://twitter.com/" + userContent.getString("userName") + "/status/" + tweetId;

            String messageString = String.format("%s %s %s",roles,userContent.getString("message"),link);
            userContent.getJSONArray("channel").forEach(x->{
                Channel channel = MainUtil.jda.getGuildChannelById(x.toString());
                if (channel == null) return;
                ChannelUtil.sendMessageInRightChannel(messageString,channel);
            });
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected static @Nullable JSONObject getGuildNotifications(Guild guild){
        if (directory == null) return null;

       File file = FileHandler.getFileInDirectory(directory,guild.getId() + ".json");
       if (!file.exists()){
           FileHandler.createFile(directory,guild.getId() + ".json");
           FileHandler.writeValuesInFile(file,new JSONObject());
       }

       String fileContentString = FileHandler.getFileContent(file);
       if (fileContentString == null) return null;
       return new JSONObject(fileContentString);
    }

    public static void sendOverviewEmbed(@NotNull SlashCommandInteractionEvent event,OptionMapping notificationType){
        NotificationType type = null;
        if (notificationType != null) type = NotificationType.getNotificationTypeByName(notificationType.getAsString());
        event.replyEmbeds(NotificationBuilders.overviewEmbed(type)).setEphemeral(true)
                .addActionRow(actionSelectButtonCollection(event.getUser(), type)).queue();
    }

    public static void sendAddModal(Event event){
        SlashCommandInteractionEvent slashEvent = JdaEventUtil.getSlashEvent(event);
        ButtonInteractionEvent buttonEvent = JdaEventUtil.getButtonEvent(event);

        if (slashEvent != null){
            //noinspection ConstantConditions
            NotificationType type = NotificationType.getNotificationTypeByName(slashEvent.getOption("notification").getAsString());
            slashEvent.replyModal(NotificationBuilders.addNotificationModal(type, slashEvent.getUser())).queue();
        }
        if (buttonEvent != null){
            String eventName = buttonEvent.getComponentId().split(";")[0];
            NotificationType type;
            if (eventName.equals("youtubeNotification") || eventName.equals("twitchNotification") || eventName.equals("twitterNotification")){
                type = NotificationType.getNotificationTypeByName(eventName.replace("Notification",""));
            }else {
                String typeString = buttonEvent.getComponentId().split(";")[1].split(",")[1].toLowerCase(Locale.ROOT);
                type = NotificationType.getNotificationTypeByName(typeString);
            }
            buttonEvent.replyModal(NotificationBuilders.addNotificationModal(type, buttonEvent.getUser())).queue();
        }
    }

    public static void sendEditModalOrDeleteEntry(@NotNull StringSelectInteractionEvent event){
        User user = event.getUser();
        String suffix = event.getComponentId().split(";")[1];
        if (!user.getId().equals(suffix.split(",")[0])) return;
        if (suffix.split(",")[2].equals("edit")) {
            String value = event.getValues().get(0);
            String notification = suffix.split(",")[1];
            NotificationType type = NotificationType.getNotificationTypeByName(notification);
            //noinspection ConstantConditions
            JSONObject content = getGuildNotifications(event.getGuild())
                    .getJSONObject(notification).getJSONObject(value);
            event.replyModal(NotificationBuilders.editNotificationModal(type, content, user)).queue();
        }else if (suffix.split(",")[2].equals("remove")){
            if (directory == null){
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            //noinspection ConstantConditions
            File file = FileHandler.getFileInDirectory(directory,event.getGuild().getId() + ".json");
            JSONObject config = getGuildNotifications(event.getGuild());
            if (config == null){
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            String value = event.getValues().get(0);
            String notification = suffix.split(",")[1];
            JSONObject notificationContent = config.getJSONObject(notification);
            notificationContent.remove(value);
            if (!config.isNull("latestActions")
                    && !config.getJSONObject("latestActions").isNull(notification)
                    && !config.getJSONObject("latestActions").getJSONObject(notification).isNull(value))
                config.getJSONObject("latestActions").getJSONObject(notification).remove(value);

            config.put(notification,notificationContent);
            FileHandler.writeValuesInFile(file,config);
            event.editMessage("The notification was successful removed").queue(x->{
                x.editOriginalComponents().queue();
                x.editOriginalEmbeds().queue();
            });


        }
    }

    public static void sendEditNotificationSelectEmbed(Event event,NotificationType type,String action){
        SlashCommandInteractionEvent slashEvent = JdaEventUtil.getSlashEvent(event);
        ButtonInteractionEvent buttonEvent = JdaEventUtil.getButtonEvent(event);

        if (slashEvent !=null){
            JSONObject config = getGuildNotifications(slashEvent.getGuild());
            if (config == null || config.isEmpty()){
                slashEvent.reply("Please create a new notification").setEphemeral(true).queue();
                return;
            }

            if (config.isNull(type.getTypeName())){
                slashEvent.reply("Please create a new " + type.getTypeName() + " notification").setEphemeral(true).queue();
                return;
            }

            JSONObject typeContent = config.getJSONObject(type.getTypeName());
            if (typeContent.isEmpty()){
                slashEvent.reply("Please create a new " + type.getTypeName() + " notification").setEphemeral(true).queue();
                return;
            }

            List<MessageEmbed.Field> fields = new ArrayList<>();
            var ref = new Object() {
                int i = 0;
            };
            typeContent.keySet().forEach(x->{
                ref.i = ref.i + 1;
                if (ref.i < 24) {
                    JSONObject notificationContent = typeContent.getJSONObject(x);
                    fields.add(new MessageEmbed.Field(notificationContent.getString("userName"), notificationContent.getJSONArray("channel").toList().toString(), false));
                }
            });
            int remainCount = typeContent.keySet().size() - fields.size();
            slashEvent.replyEmbeds(NotificationBuilders.selectNotificationEmbed(fields))
                    .setEphemeral(true)
                    .addComponents(NotificationBuilders.actionRowsForEditModal(0,remainCount,typeContent, slashEvent.getUser(),type,action))
                    .queue();
        }

        if (buttonEvent != null){
            JSONObject config = getGuildNotifications(buttonEvent.getGuild());
            if (config == null || config.isEmpty()){
                buttonEvent.reply("Please create a new notification").setEphemeral(true).queue();
                return;
            }
            if (config.isNull(type.getTypeName())){
                buttonEvent.reply("Please create a new " + type.getTypeName() + " notification").setEphemeral(true).queue();
                return;
            }
            JSONObject typeContent = config.getJSONObject(type.getTypeName());
            if (typeContent.isEmpty()){
                buttonEvent.reply("Please create a new " + type.getTypeName() + " notification").setEphemeral(true).queue();
                return;
            }

            List<MessageEmbed.Field> fields = new ArrayList<>();

            var ref = new Object() {
                int i = 0;
            };
            typeContent.keySet().forEach(x->{
                ref.i = ref.i + 1;
                if (ref.i < 24) {
                    JSONObject notificationContent = typeContent.getJSONObject(x);
                    fields.add(new MessageEmbed.Field(notificationContent.getString("userName"), notificationContent.getJSONArray("channel").toList().toString(), false));
                }
            });

            int remainCount = typeContent.keySet().size() - 1 - fields.size();
            buttonEvent.editMessageEmbeds(NotificationBuilders.selectNotificationEmbed(fields))
                    .setComponents(NotificationBuilders.actionRowsForEditModal(0,remainCount,typeContent, buttonEvent.getUser(),type,action))
                    .queue();
        }
    }

    public static void editNotificationSelectEmbed(@NotNull ButtonInteractionEvent event){
        JSONObject config = getGuildNotifications(event.getGuild());
        String suffix = event.getComponentId().split(";")[1];
        String notificationType = suffix.split(",")[1];
        String type = event.getComponentId().split(";")[0].replace("NotificationEmbed","");
        String action = suffix.split(",")[3];
        if (config == null || config.isNull(notificationType)){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        JSONObject content = config.getJSONObject(notificationType);
        List<String> keys = content.keySet().stream().toList();

        var ref = new Object() {
            int remain = 0;
            int before = 0;
        };
        event.getMessage().getButtons().forEach(x->{
            //noinspection ConstantConditions
            int c = Integer.parseInt(x.getId().split(";")[1].split(",")[2]);
           if (x.getLabel().equals("Next")){
               ref.remain = c;
           }
           if (x.getLabel().equals("Before")){
               ref.before = c;
           }
        });

        List<String> sublist;
        if (type.equals("next")){
            int from = ref.before;
            int to = ref.remain < 24 ? ref.before + ref.remain : ref.before + 23;
            if (ref.before == 0){
                from = 24;
                to = ref.remain < 24 ? 23 + ref.remain : 23 + 24;
            }
            sublist = keys.subList(from,to);
            ref.before = from-1;
            ref.remain = ref.remain - keys.size();
        }else {
            int from = ref.before - 23;
            int to = ref.before;
            sublist = keys.subList(from,to);
            ref.before = from;
            ref.remain = keys.size() - to;
        }
        JSONObject embedAndFieldContent = new JSONObject();
        List<MessageEmbed.Field> fields = new ArrayList<>();
        sublist.forEach(x->{
            JSONObject userContent = content.getJSONObject(x);
            embedAndFieldContent.put(x,userContent);

            fields.add(new MessageEmbed.Field(userContent.getString("userName"),userContent.getJSONArray("channel").toList().toString(),false));
        });

        event.editMessageEmbeds(NotificationBuilders.selectNotificationEmbed(fields))
                .setComponents(NotificationBuilders.actionRowsForEditModal(ref.before, ref.remain,embedAndFieldContent, event.getUser(), NotificationType.getNotificationTypeByName(notificationType),action))
                .queue();


    }



    public static void sendNotificationSelectEmbed(Event event){
        SlashCommandInteractionEvent slashEvent = JdaEventUtil.getSlashEvent(event);
        ButtonInteractionEvent buttonEvent = JdaEventUtil.getButtonEvent(event);

        if (slashEvent != null){
            OptionMapping action = slashEvent.getOption("action");
            if (action == null) return;
            String suffix = String.format("%s,%s",slashEvent.getUser().getId(),action.getAsString());
            slashEvent.replyEmbeds(NotificationBuilders.notificationSelectEmbed(action.getAsString().toUpperCase(Locale.ROOT)))
                    .addActionRow(notificationSelectButtonCollection(suffix))
                    .setEphemeral(true).queue();
        }

        if (buttonEvent != null){
            String buttonName = buttonEvent.getComponentId();
            if (!buttonName.contains(";")) return; // buttonName **action**Notification;userId(,type)
            String eventName = buttonName.split(";")[0];
            String suffix = String.format("%s,%s",buttonEvent.getUser().getId(),eventName.replace("Notification",""));
            if (!eventName.equals("addNotification") && !eventName.equals("editNotification") && !eventName.equals("removeNotification")) return;
            buttonEvent.editMessageEmbeds(NotificationBuilders.notificationSelectEmbed(eventName.replace("Notification","").toUpperCase(Locale.ROOT)))
                    .setActionRow(notificationSelectButtonCollection(suffix))
                    .queue();
        }
    }

    public static void addOrEditNotificationEntry(@NotNull ModalInteractionEvent event){
        if (event.getGuild() == null) return;
        ModalMapping userName = event.getValue("userName");
        ModalMapping channel = event.getValue("channel");
        ModalMapping roles = event.getValue("roles");
        ModalMapping message = event.getValue("message");
        NotificationType type = NotificationType.getNotificationTypeByName(event.getModalId().split(";")[0].replace("Notification",""));
        if (!event.getUser().getId().equals(event.getModalId().split(";")[1])) return;

        if (userName == null || channel == null || roles == null || message == null) {
            event.reply("Please write in all fields a value").setEphemeral(true).queue();
            return;
        }

        JSONObject config = getGuildNotifications(event.getGuild());
        if (config == null){
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        if (config.isEmpty() || config.isNull(type.getTypeName())){
            config.put(type.getTypeName(),new JSONObject());
        }
        JSONObject typeContent = config.getJSONObject(type.getTypeName());

        JSONArray channelList = new JSONArray();
        String channelString = channel.getAsString();
        if (!channelString.contains(",")){
            Channel targetChannel = MainUtil.jda.getGuildChannelById(channelString);
            if (targetChannel == null) {
                event.reply("Please check the the channelId || " + channelString).setEphemeral(true).queue();
                return;
            }
            if (ChannelUtil.rightChannel(targetChannel) == null) {
                event.reply("This chanelId is not a message-channel | " + channelString).setEphemeral(true).queue();
                return;
            }
            channelList.put(targetChannel.getId());
        }else {
            boolean error = false;
            for (String s : channelString.split(",")) {
                Channel targetChannel = event.getGuild().getGuildChannelById(s);
                String replaceString = channelString.replace(s, String.format("**%s**", s));
                if (targetChannel == null) {
                    event.reply("Please check check this channelId | " + replaceString).setEphemeral(true).queue();
                    error = true;
                    break;
                }
                if (ChannelUtil.rightChannel(targetChannel) == null) {
                    event.reply("This chanelId is not a message-channel | " + replaceString).setEphemeral(true).queue();
                    error = true;
                    break;
                }

                channelList.put(targetChannel.getId());
            }

            if (error) return;
        }

        JSONArray rolesList = new JSONArray();
        String rolesString = roles.getAsString();
        if (!rolesString.contains(",")){
            Role targetRole = MainUtil.jda.getRoleById(rolesString);
            if (targetRole == null) {
                event.reply("Please check the the role-id || " + rolesString).setEphemeral(true).queue();
                return;
            }
            rolesList.put(targetRole.getId());
        }else {
            boolean error = false;
            for (String s : rolesString.split(",")) {
                Role targetRole = MainUtil.jda.getRoleById(s);
                String replaceString = rolesString.replace(s, String.format("**%s**", s));
                if (targetRole == null) {
                    event.reply("Please check this role-id | " + replaceString).setEphemeral(true).queue();
                    error = true;
                    break;
                }

                rolesList.put(targetRole.getId());
            }

            if (error) return;
        }


        String userId = null;
        switch (type){
            case TWITCH -> userId = TwitchApi.getUserIdFromUserName(userName.getAsString());
            case TWITTER -> userId = TwitterApi.getUserIdFromUserName(userName.getAsString());
            case YOUTUBE -> userId = YouTubeApi.getUserIdFromUserName(userName.getAsString());
        }

        if (userId == null) {
            event.reply("Check the token in the main-config and the the target username").setEphemeral(true).queue();
            return;
        }
        typeContent.put(userId,new JSONObject(){{
            put("userName",userName.getAsString());
            put("channel",channelList);
            put("roles",rolesList);
            put("message",message.getAsString());
        }});

        config.put(type.getTypeName(),typeContent);
        //noinspection ConstantConditions
        FileHandler.writeValuesInFile(FileHandler.getFileInDirectory(directory,event.getGuild().getId() + ".json"),config);

        event.editMessageEmbeds(NotificationBuilders.showContent(type,typeContent.getJSONObject(userId),channel,roles)).queue(x->
                x.editOriginalComponents().queue());
    }



}
