package de.SparkArmy.timedOperations;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.notifications.NotificationUtil;
import de.SparkArmy.notifications.YouTubeApi;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.jda.FileHandler;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TimedOperations {
    private static final JDA jda = MainUtil.jda;
    private static final ConfigController controller = MainUtil.controller;

    protected static void removeOldTemporaryPunishments() {
        try {

            File directory = FileHandler.getDirectoryInUserDirectory("botstuff/timed-punishments");
            if (directory == null) {
                MainUtil.logger.info("Can't create/ get a directory for timed-punishments");
                return;
            }

            File file = FileHandler.getFileInDirectory(directory, "entrys.json");
            if (!file.exists()) return;


            String fileContent = FileHandler.getFileContent(file);
            if (fileContent == null || fileContent.isEmpty()) return;
            JSONObject entrys = new JSONObject(fileContent);
            List<String> keyList = new ArrayList<>();
            if (entrys.isEmpty()) return;
            entrys.keySet().forEach(key -> {
                JSONObject entry = entrys.getJSONObject(key);
                DateTimeFormatter formatter = PunishmentUtil.punishmentFormatter;
                String time = entry.getString("expirationTime");
                String timeNow = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
                boolean timeReached = LocalDateTime.parse(timeNow, formatter).isAfter(LocalDateTime.parse(time, formatter));
                if (timeReached) {
                    Guild guild = jda.getGuildById(entry.getString("guild"));
                    if (guild == null) {
                        keyList.add(key);
                        return;
                    }
                    JSONObject config = controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
                    if (config.isNull("punishments")) {
                        keyList.add(key);
                        return;
                    }
                    JSONObject punishments = config.getJSONObject("punishments");
                    String type = entry.getString("type");
                    User user = jda.retrieveUserById(entry.getString("user")).complete();
                    if (user == null) {
                        keyList.add(key);
                        return;
                    }
                    if (type.equals("warn") || type.equals("mute")) {
                        String roleId = punishments.getJSONObject(type).getString("role-id");
                        if (roleId.equals("Empty")) {
                            keyList.add(key);
                            return;
                        } else if (guild.getRoleById(roleId) == null) {
                            keyList.add(key);
                            return;
                        }

                        Member member = guild.getMember(user);
                        if (member == null){
                            keyList.add(key);
                            return;
                        }

                        if (member.getRoles().isEmpty() || !member.getRoles().contains(guild.getRoleById(roleId))) {
                            keyList.add(key);
                            return;
                        }

                        Role role = guild.getRoleById(roleId);

                        if (role == null) {
                            keyList.add(key);
                            return;
                        }

                        guild.removeRoleFromMember(user, role).reason("Automatic role remove").queue();
                        keyList.add(key);
                        return;
                    }
                    if (type.equals("ban")) {
                        guild.unban(user).reason("Automatic unban").queue();
                        keyList.add(key);
                        return;
                    }

                    keyList.add(key);
                }
            });
            if (keyList.isEmpty()) return;
            keyList.forEach(entrys::remove);
            FileHandler.writeValuesInFile(file,entrys);
        }catch (Exception e){
        MainUtil.logger.error(e.getMessage());
        e.printStackTrace();
        }
    }

    protected static void checkForNotificationUpdates() {
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/notifications");
        if (directory == null) return;
        List<File> files = FileHandler.getFilesInDirectory(directory);
        if (files == null) return;
        files.forEach(file->{
          Guild guild = MainUtil.jda.getGuildById(file.getName().replace(".json",""));
          if (guild == null || guild.equals(MainUtil.storageServer)) return;

          String fileContentString = FileHandler.getFileContent(file);
          if (fileContentString == null) return;

          JSONObject fileContent = new JSONObject(fileContentString);

          if (fileContent.isEmpty()) return;
          if (!fileContent.isNull("twitch")){
              JSONObject twitchWatchlist = fileContent.getJSONObject("twitch");
              NotificationUtil.checkForTwitchStreams(twitchWatchlist,guild);
          }

            if (!fileContent.isNull("twitter")){
                JSONObject twitterWatchlist = fileContent.getJSONObject("twitter");
                    NotificationUtil.checkForTweets(twitterWatchlist,guild);
            }

        });
    }

    protected static void updateYouTubeSubscriptions(){
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/notifications");
        if (directory == null) return;
        List<File> files = FileHandler.getFilesInDirectory(directory);
        if (files == null) return;
        files.forEach(file-> {
            Guild guild = MainUtil.jda.getGuildById(file.getName().replace(".json", ""));
            if (guild == null || guild.equals(MainUtil.storageServer)) return;

            String fileContentString = FileHandler.getFileContent(file);
            if (fileContentString == null) return;

            JSONObject fileContent = new JSONObject(fileContentString);

            if (fileContent.isEmpty()) return;
            if (fileContent.isNull("youtube")) return;
            JSONObject watchlist = fileContent.getJSONObject("youtube");
            if (watchlist.isEmpty()) return;
            watchlist.keySet().forEach(x ->{
                if(!YouTubeApi.subscribeOrUnsubscribeToPubSubHubBub(x, "subscribe")){
                    MainUtil.logger.info("Subscription for " + x + " failed");
                }
            });
        });
    }

    protected static void updateStatusPhrase(){
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff");
        assert directory != null;
        File file = FileHandler.getFileInDirectory(directory,"status.json");

        if (!file.exists()) {
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(Activity.ActivityType.COMPETING,"How many errors I can generate in one run"));
            FileHandler.createFile(directory,file.getName());
            return;
        }
        String contentString = FileHandler.getFileContent(file);
        if (contentString == null){
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(Activity.ActivityType.COMPETING,"How many errors I can generate in one run"));
            return;
        }

        JSONObject content = new JSONObject(contentString);

        List<String> keys = content.keySet().stream().toList();
        String activity = String.valueOf(keys.get(new Random().nextInt(keys.size())));
        String phrase;
        if (content.getJSONArray(activity).isEmpty()) {
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.of(Activity.ActivityType.COMPETING,"How many errors I can generate in one run"));
            return;
        }
        phrase = content.getJSONArray(activity).getString(new Random().nextInt(content.getJSONArray(activity).length()));
        Activity statusActivity;
        switch (activity){
            case "listening" -> statusActivity = Activity.of(Activity.ActivityType.LISTENING, phrase);
            case "streaming" -> statusActivity = Activity.of(Activity.ActivityType.STREAMING, phrase);
            case "watching" -> statusActivity = Activity.of(Activity.ActivityType.WATCHING, phrase);
            case "playing" -> statusActivity = Activity.of(Activity.ActivityType.PLAYING, phrase);
            default -> statusActivity = Activity.of(Activity.ActivityType.COMPETING,phrase);
        }
        jda.getPresence().setPresence(OnlineStatus.ONLINE,statusActivity);
    }

    protected static void updateUserCount(){
        jda.getGuilds().forEach(guild->{
            if (guild.equals(MainUtil.storageServer)) return;
            JSONObject config = controller.getSpecificGuildConfig(guild,GuildConfigType.MAIN);
            if (config.isNull("user-count")) return;
            JSONObject countConfig = config.getJSONObject("user-count");
            String suffix = countConfig.getString("string");
            String name = String.format("%s:%d",suffix,guild.getMemberCount());
            //noinspection ConstantConditions
            guild.getGuildChannelById(countConfig.getString("count-channel")).getManager().setName(name).queue(null,
                    new ErrorHandler().ignore(NullPointerException.class));
        });
    }

    protected static void deleteOldLogs(){
        File directory = FileHandler.getDirectoryInUserDirectory("logs/archived");
        if (directory == null || !directory.exists()) return;
        File[] files = directory.listFiles();
        if (files == null || files.length <= 10) return;
        //noinspection ResultOfMethodCallIgnored
        Arrays.stream(files).toList().get(0).delete();
    }
}
