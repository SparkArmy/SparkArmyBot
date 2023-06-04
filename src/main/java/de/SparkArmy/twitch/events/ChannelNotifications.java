package de.SparkArmy.twitch.events;

import com.github.philippheuer.events4j.reactor.ReactorEventHandler;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import de.SparkArmy.controller.ConfigController;
import org.jetbrains.annotations.NotNull;

public class ChannelNotifications {
    private final ConfigController controller;

    public ChannelNotifications(@NotNull ConfigController controller, @NotNull ReactorEventHandler eventHandler) {
        this.controller = controller;
        eventHandler.onEvent(ChannelGoLiveEvent.class, this::onGoLive);
    }

    public void onGoLive(@NotNull ChannelGoLiveEvent event) {
        controller.getMain().getLogger().info(event.getStream().getUserName());
    }
}
