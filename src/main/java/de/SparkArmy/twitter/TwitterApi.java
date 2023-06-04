package de.SparkArmy.twitter;

import de.SparkArmy.Main;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.twitter.utils.TwitterUser;
import de.andre.Requests.Request;
import org.jetbrains.annotations.NotNull;

public class TwitterApi {
    private final String twitter_bearer;

    public TwitterApi(@NotNull Main main) {
        ConfigController controller = main.getController();
        this.twitter_bearer = controller.getMainConfigFile().getJSONObject("otherKeys").getString("twitter_bearer");
    }

    final String hostUrl = "https://api.twitter.com";

    final String getUserByUsername = "/2/users/by/username/%s";

    public TwitterUser getUserDataByUsername(String username) {
        String url = String.format(hostUrl + getUserByUsername, username);
        return new TwitterUser(new Request(url).setHeader("Authorization", String.format("Bearer %s", twitter_bearer)).makeRequest());
    }
}
