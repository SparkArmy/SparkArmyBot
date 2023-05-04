package de.SparkArmy.twitch.events;

import com.github.philippheuer.events4j.reactor.ReactorEventHandler;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import de.SparkArmy.controller.ConfigController;
import net.dv8tion.jda.api.JDA;
import org.jetbrains.annotations.NotNull;

public class ChannelNotifications {
    private final ConfigController controller;
    private final JDA jda;


    public ChannelNotifications(@NotNull ConfigController controller, @NotNull ReactorEventHandler eventHandler) {
        this.controller = controller;
        this.jda = controller.getMain().getJdaApi().getJda();
        eventHandler.onEvent(ChannelGoLiveEvent.class, this::onGoLive);
    }

    public void onGoLive(@NotNull ChannelGoLiveEvent event) {
        controller.getMain().getLogger().info(event.getStream().getUserName());
    }
}
