package de.SparkArmy.twitch;

import com.github.philippheuer.events4j.reactor.ReactorEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import de.SparkArmy.Main;
import de.SparkArmy.twitch.events.ChannelNotifications;
import de.SparkArmy.utils.NotificationService;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TwitchApi {

    private final TwitchClient twitchClient;
    private final Logger logger;

    private final JSONObject mainConfig;
    private final Main main;

    public TwitchApi(@NotNull Main main) {
        this.main = main;
        this.logger = main.getLogger();
        this.mainConfig = main.getController().getMainConfigFile();
        this.twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withClientId(mainConfig.getJSONObject("twitch").getString("twitch-client-id"))
                .withClientSecret(mainConfig.getJSONObject("twitch").getString("twitch-client-secret"))
                .withDefaultEventHandler(ReactorEventHandler.class)
                .build();
        registerEventListeners();
        registerEvents();
    }

    private void registerEventListeners() {
        for (Map.Entry<String, String> entry : main.getPostgres().getServiceIdAndNameByNotificationService(NotificationService.TWITCH).entrySet()) {
            twitchClient.getClientHelper().enableStreamEventListener(entry.getValue(), entry.getKey());
        }
    }

    private void registerEvents() {
        ReactorEventHandler eventHandler = twitchClient.getEventManager().getEventHandler(ReactorEventHandler.class);
        new ChannelNotifications(main.getController(), eventHandler);
    }

    public List<String> getChannelIdsByChannelName(String name) {
        UserList userList = twitchClient.getHelix().getUsers(null, null, Collections.singletonList(name)).execute();
        return userList.getUsers().stream().map(User::getId).toList();
    }

    public void closeClient() {
        twitchClient.close();
    }


    public Main getMain() {
        return main;
    }
}
