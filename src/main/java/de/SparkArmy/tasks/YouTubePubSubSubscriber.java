package de.SparkArmy.tasks;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.utils.NotificationService;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YouTubePubSubSubscriber implements Runnable {
    ConfigController controller;

    public YouTubePubSubSubscriber(ConfigController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        Logger logger = controller.getMain().getLogger();
        Postgres db = controller.getMain().getPostgres();
        JSONArray data = db.getDataFromSubscribedChannelTableByService(NotificationService.YOUTUBE);

        List<String> urls = new ArrayList<>();

        String urlPattern = "https://www.youtube.com/xml/feeds/videos.xml?channel_id=%s";

        for (Object o : data) {
            JSONObject object = (JSONObject) o;
            urls.add(String.format(urlPattern, object.getString("contentCreatorId")));
        }

        logger.info(urls.toString());
        OkHttpClient client = new OkHttpClient();

        String callbackUrl = controller.getMainConfigFile().getJSONObject("youtube").getString("spring-callback-url");
        logger.info(callbackUrl);
        for (String s : urls) {
            try {
                RequestBody formBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("hub.callback", callbackUrl)
                        .addFormDataPart("hub.mode", "subscribe")
                        .addFormDataPart("hub.topic", s)
                        .addFormDataPart("hub.verify", "async")
                        .build();
                Request request = new Request.Builder()
                        .url("https://pubsubhubbub.appspot.com/subscribe")
                        .method("POST", formBody)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                logger.info(String.valueOf(response.code()));
                response.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
