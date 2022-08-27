package de.SparkArmy.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.RequestUtil;
import org.json.JSONObject;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class YouTubeApi {

    private static final String apiKey = MainUtil.mainConfig.getJSONObject("youtube").getString("youtube-api-key");

    public static String getUserIdFromUserName(String userName){
        String requestString = String.format("https://youtube.googleapis.com/youtube/v3/channels?part=id&forUsername=%s&key=%s",userName,apiKey);
        return RequestUtil.get(requestString, new JSONObject(){{put("Accept","application/json");}})
                .optJSONArray("items").optJSONObject(0).optString("id","unknow");
    }


    public static boolean subscribeOrUnsubscribeToPubSubHubBub(String userId, String mode){
        String targetUrl = String.format("https://www.youtube.com/xml/feeds/videos.xml?channel_id=%s",userId);
        String url = "https://pubsubhubbub.appspot.com/subscribe";

        Map<String,String> form = new HashMap<>();
        form.put("hub.callback",MainUtil.mainConfig.getJSONObject("youtube").getString("spring-callback-domain"));
        form.put("hub.mode",mode);
        form.put("hub.topic",targetUrl);
        form.put("hub.verify","async");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String requestBody = objectMapper
                    .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(form);
           HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(requestBody);

           HttpRequest request = RequestUtil.prepareRequest(url,new JSONObject(),body,"POST");
           HttpResponse<String> response = RequestUtil.httpRequestToResponse(request);
           return response.statusCode() == 202;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

}
