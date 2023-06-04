package de.SparkArmy.youtube;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.ChannelListResponse;
import de.SparkArmy.Main;
import de.SparkArmy.controller.ConfigController;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class YouTubeApi {

    private final String apiKey;

    public YouTubeApi(@NotNull Main main) {
        ConfigController controller = main.getController();
        this.apiKey = controller.getMainConfigFile().getJSONObject("youtube").getString("youtube-api-key");
    }

    private @NotNull YouTube getYouTubeService() throws GeneralSecurityException, IOException {
        YouTubeRequestInitializer requestInitializer = new YouTubeRequestInitializer(apiKey);
        YouTube.Builder youTubeBuilder = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), new GsonFactory(), null);
        youTubeBuilder.setApplicationName("YouTube");
        youTubeBuilder.setYouTubeRequestInitializer(requestInitializer);
        return youTubeBuilder.build();
    }

    public String getUserId(String channelName) {
        try {
            List<String> parts = new ArrayList<>();
            parts.add("id");
            parts.add("contentOwnerDetails");
            YouTube service = getYouTubeService();
            YouTube.Channels.List request = service.channels().list(parts);
            ChannelListResponse response = request.setForUsername(channelName).execute();
            return response.getItems().get(0).getId();
        } catch (GeneralSecurityException | IOException e) {
            return "";
        }
    }


}
