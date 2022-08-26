package de.SparkArmy.notifications;

import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.RequestUtil;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class TwitchApi {

    private static final String client_id = MainUtil.mainConfig.getJSONObject("twitch").getString("twitch-client-id");
    private static final String client_secret = MainUtil.mainConfig.getJSONObject("twitch").getString("twitch-client-secret");
    private static final JSONObject header =
            new JSONObject() {{
                put("Authorization", String.format("Bearer %s", getBearerToken()));
                put("Client-Id", client_id);
            }};


    private static final HashMap<String,JSONObject> userData = new HashMap<>();
    private static final HashMap<String,JSONObject> streamData = new HashMap<>();
    private static String getBearerToken(){
        String url = String.format("https://id.twitch.tv/oauth2/token?client_id=%s&client_secret=%s&grant_type=client_credentials",client_id,client_secret);
        return  RequestUtil.post(url,new JSONObject(){{put("Content-Type","application/x-www-form-urlencoded");}}).getString("access_token");
    }

    private static void getUserData(String userName){
        String url = String.format("https://api.twitch.tv/helix/users?login=%s",userName);
        JSONObject data = RequestUtil.get(url,header);
        if (data.isEmpty()) return;
        if (data.isNull("data")) return;
        if (data.getJSONArray("data").isNull(0)) return;
        userData.put(userName,data.getJSONArray("data").getJSONObject(0));
    }

    private static void getStreamData(String userId){
        String url = String.format("https://api.twitch.tv/helix/streams?user_id=%s",userId);
        JSONObject data = RequestUtil.get(url,header);
        if (data.isEmpty()) return;
        if (data.isNull("data")) return;
        if (data.getJSONArray("data").isNull(0)) return;
        streamData.put(userId,data.getJSONArray("data").getJSONObject(0));
    }


    protected static @Nullable String getUserIdFromUserName(String userName){
        getUserData(userName);
        JSONObject data = userData.get(userName);
        if (data.isEmpty()) return null;
        return data.getString("id");
    }

    protected static @Nullable JSONObject getUserDataByUserName(String userName){
        getUserData(userName);
        if (!userData.containsKey(userName)) return null;
        return userData.get(userName);
    }

    protected static @Nullable JSONObject getStreamDataByUserId(String userId){
        getStreamData(userId);
        if (!streamData.containsKey(userId)) return null;
        return streamData.get(userId);
    }

    protected static boolean isUserNewLive(String userId, String time, Guild guild){
        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/notifications");
        if (directory == null) return false;
        File file = FileHandler.getFileInDirectory(directory,guild.getId() + ".json");
        if (!file.exists()) return false;
        JSONObject config = NotificationUtil.getGuildNotifications(guild);
        if (config == null) return false;
        if (config.isEmpty()) return false;
        if (config.isNull("latestActions")) {
            config.put("latestActions", new JSONObject() {{
                put("twitch", new JSONObject());
            }});
        }
        JSONObject actions = config.getJSONObject("latestActions");
        if (actions.isNull("twitch")){
            actions.put("twitch",new JSONObject(){{
                put(userId,time);
            }});
            config.put("latestActions",actions);
            FileHandler.writeValuesInFile(file,config);
            return true;
        }
        JSONObject twitch = actions.getJSONObject("twitch");
        if (!twitch.isNull(userId)) {
            if (twitch.getString(userId).equals(time)) {
                return false;
            }
        }
        twitch.put(userId, time);
        actions.put("twitch",twitch);
        config.put("latestActions",actions);
        FileHandler.writeValuesInFile(file,config);
        return true;
    }
}
