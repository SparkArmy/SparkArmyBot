package de.SparkArmy.twitch;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import de.SparkArmy.Main;
import de.SparkArmy.twitch.events.ChannelNotifications;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

public class TwitchApi {

    private final TwitchClient twitchClient;
    private final Logger logger;

    private final Main main;

    public TwitchApi(@NotNull Main main) {
        this.main = main;
        this.logger = main.getLogger();
        JSONObject mainConfig = main.getController().getMainConfigFile();
        this.twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withClientId(mainConfig.getJSONObject("twitch").getString("twitch-client-id"))
                .withClientSecret(mainConfig.getJSONObject("twitch").getString("twitch-client-secret"))
                .withDefaultEventHandler(SimpleEventHandler.class)
                .build();
    }

    public void registerEventListener(String twitchUserName) {
        twitchClient.getClientHelper().enableStreamEventListener(twitchUserName);
    }

    public void disableEventListener(String twitchUserName) {
        twitchClient.getClientHelper().disableStreamEventListener(twitchUserName);
    }

    private void registerEvents() {
        SimpleEventHandler eventHandler = twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class);
        new ChannelNotifications(main.getController(), eventHandler);
    }

    public Main getMain() {
        return main;
    }
}
