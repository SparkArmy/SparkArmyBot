package de.sparkarmy.twitch;

import com.github.philippheuer.events4j.reactor.ReactorEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.domain.User;
import de.sparkarmy.Main;
import de.sparkarmy.config.Twitch;
import de.sparkarmy.twitch.events.ChannelNotifications;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

public class TwitchApi {

    private final TwitchClient twitchClient;

    private final ChannelNotifications channelNotifications;

    private final Main main;

    public TwitchApi(@NotNull Main main) {
        this.main = main;
        Logger logger = main.getLogger();
        Twitch twitch = main.getController().getConfig().getTwitch();
        this.twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withClientId(twitch.getTwitchClientId())
                .withClientSecret(twitch.getTwitchClientSecret())
                .withDefaultEventHandler(ReactorEventHandler.class)
                .withTimeout(30000)
                .withHelperThreadDelay(30_000)
                .build();

        ReactorEventHandler eventHandler = twitchClient.getEventManager().getEventHandler(ReactorEventHandler.class);
        this.channelNotifications = new ChannelNotifications(main.getController(), eventHandler, twitchClient);
        logger.info("TwitchListeners successfully build");
    }

    public List<User> getUserInformation(String userName) {
        return twitchClient.getHelix().getUsers(null, null, Collections.singletonList(userName)).execute().getUsers();
    }

    public void closeClient() {
        twitchClient.close();
    }

    public ChannelNotifications getChannelNotifications() {
        return channelNotifications;
    }

    public Main getMain() {
        return main;
    }
}
