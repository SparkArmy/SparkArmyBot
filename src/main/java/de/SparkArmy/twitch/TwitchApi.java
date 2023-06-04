package de.SparkArmy.twitch;

import com.github.philippheuer.events4j.reactor.ReactorEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.User;
import de.SparkArmy.Main;
import de.SparkArmy.twitch.events.ChannelNotifications;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

public class TwitchApi {

    private final TwitchClient twitchClient;

    private final Main main;
    public TwitchApi(@NotNull Main main) {
        this.main = main;
        Logger logger = main.getLogger();
        JSONObject mainConfig = main.getController().getMainConfigFile();
        this.twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withClientId(mainConfig.getJSONObject("twitch").getString("twitch-client-id"))
                .withClientSecret(mainConfig.getJSONObject("twitch").getString("twitch-client-secret"))
                .withDefaultEventHandler(ReactorEventHandler.class)
                .build();
        registerEvents();
        logger.info("TwitchListeners successfully build");
    }

    private void registerEvents() {
        ReactorEventHandler eventHandler = twitchClient.getEventManager().getEventHandler(ReactorEventHandler.class);
        new ChannelNotifications(main.getController(), eventHandler);
    }

    public List<User> getUserInformation(String userName) {
        return twitchClient.getHelix().getUsers(null, null, Collections.singletonList(userName)).execute().getUsers();
    }

    public void closeClient() {
        twitchClient.close();
    }


    public Main getMain() {
        return main;
    }
}
