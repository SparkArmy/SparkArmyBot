package de.sparkarmy.tasks.runnables;

import de.sparkarmy.config.ConfigController;
import de.sparkarmy.db.DatabaseAction;
import de.sparkarmy.utils.NotificationService;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YouTubePubSubSubscriber implements Runnable {
    private final ConfigController controller;
    private final DatabaseAction db = new DatabaseAction();

    public YouTubePubSubSubscriber(ConfigController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        JSONArray data = db.getDataFromSubscribedChannelTableByService(NotificationService.YOUTUBE);

        List<String> urls = new ArrayList<>();

        String urlPattern = "https://www.youtube.com/xml/feeds/videos.xml?channel_id=%s";

        for (Object o : data) {
            JSONObject object = (JSONObject) o;
            urls.add(String.format(urlPattern, object.getString("contentCreatorId")));
        }

        OkHttpClient client = new OkHttpClient();

        String callbackUrl = controller.getConfig().youtube().callbackUrl();
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
                response.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
