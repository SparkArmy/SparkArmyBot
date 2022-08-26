package de.SparkArmy.notifications;

import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.RequestUtil;
import org.json.JSONObject;

public class YouTubeApi {

    private static final String apiKey = MainUtil.mainConfig.getJSONObject("youtube").getString("youtube-api-key");

    public static String getUserIdFromUserName(String userName){
        String requestString = String.format("https://youtube.googleapis.com/youtube/v3/channels?part=id&forUsername=%s&key=%s",userName,apiKey);
        return RequestUtil.get(requestString, new JSONObject(){{put("Accept","application/json");}})
                .optJSONArray("items").optJSONObject(0).optString("id","unknow");
    }

}
