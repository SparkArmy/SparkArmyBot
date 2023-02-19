package de.SparkArmy.notifications;

import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.RequestUtil;
import org.json.JSONObject;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class YouTubeApi {

    private static final String apiKey = MainUtil.mainConfig.getJSONObject("youtube").getString("youtube-api-key");

    public static String getUserIdFromUserName(String userName){
        String requestString = String.format("https://youtube.googleapis.com/youtube/v3/channels?part=id&forUsername=%s&key=%s",userName,apiKey);
        return RequestUtil.get(requestString, new JSONObject(){{put("Accept","application/json");}})
                .optJSONArray("items").optJSONObject(0).optString("id","unknow");
    }


    public static boolean subscribeOrUnsubscribeToPubSubHubBub(String userId, String mode){
        String targetUrl = String.format("https://www.youtube.com/xml/feeds/videos.xml?channel_id=%s",userId);
        String url = String.format("https://pubsubhubbub.appspot.com/subscribe?hub.callback=%s&hub.mode=%s&hub.topic=%s&hub.verify=async",
                MainUtil.mainConfig.getJSONObject("youtube").getString("spring-callback-url"),
                mode,
                targetUrl);
           HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.noBody();
           HttpRequest request = RequestUtil.prepareRequest(url,new JSONObject(),body,"POST");
           HttpResponse<String> response = RequestUtil.httpRequestToResponse(request);
           return response.statusCode() == 202;

    }

}
