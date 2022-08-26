package de.SparkArmy.notifications;

import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.RequestUtil;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class TwitterApi {

    public static String twitter_bearer = MainUtil.mainConfig.getJSONObject("otherKeys").getString("twitter_bearer");

    protected static @Nullable String getUserIdFromUserName(String userName){
        String url = String.format("https://api.twitter.com/2/users/by/username/%s",userName);
        JSONObject returnObject = RequestUtil.get(url,new JSONObject(){{put("Authorization",String.format("Bearer %s",twitter_bearer));}});
        if (returnObject.isNull("data") || returnObject.getJSONObject("data").isNull("id")) return null;
        return returnObject.getJSONObject("data").getString("id");
    }

    protected static @Nullable String getLatestTweetFromUser(String userId){
        String url = String.format("https://api.twitter.com/2/users/%s/tweets",userId);
        JSONObject returnObject = RequestUtil.get(url,new JSONObject(){{put("Authorization",String.format("Bearer %s",twitter_bearer));}});
        if (returnObject.isNull("meta") || returnObject.getJSONObject("meta").isNull("newest_id")) return null;
        return returnObject.getJSONObject("meta").getString("newest_id");

    }

}
