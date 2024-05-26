package de.sparkarmy.twitch.events;

import com.github.philippheuer.events4j.reactor.ReactorEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientHelper;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.helix.domain.User;
import de.sparkarmy.config.ConfigController;
import de.sparkarmy.db.DatabaseAction;
import de.sparkarmy.utils.NotificationService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.*;

public class ChannelNotifications {
    private final ConfigController controller;

    private final TwitchClientHelper clientHelper;

    private final Set<String> channelNames = new HashSet<>();

    public ChannelNotifications(@NotNull ConfigController controller, @NotNull ReactorEventHandler eventHandler, @NotNull TwitchClient client) {
        this.controller = controller;
        this.clientHelper = client.getClientHelper();
        eventHandler.onEvent(ChannelGoLiveEvent.class, this::onGoLive);
        registerListenedChannels();
    }

    public void registerListenedChannels() {
        JSONArray tableData = new DatabaseAction().getDataFromSubscribedChannelTableByService(NotificationService.TWITCH);
        for (Object o : tableData) {
            JSONObject object = (JSONObject) o;
            channelNames.add(object.getString("contentCreatorName"));
        }
        clientHelper.enableStreamEventListener(channelNames);
    }

    public void unregisterListenedChannels() {
        clientHelper.disableStreamEventListener(channelNames);
    }

    public void updateListenedChannels() {
        unregisterListenedChannels();
        registerListenedChannels();
    }


    public void onGoLive(@NotNull ChannelGoLiveEvent event) {
        JSONArray tableData = new DatabaseAction().getDataFromSubscribedChannelTableByContentCreatorId(event.getChannel().getId());

        Collection<MessageCreateAction> messageSendActions = new ArrayList<>();

        for (Object o : tableData) {
            JSONObject object = (JSONObject) o;
            MessageChannel messageChannel = (MessageChannel) controller.main().getJdaApi().getShardManager().getGuildChannelById(object.getLong("messageChannelId"));
            MessageCreateBuilder messageData = new MessageCreateBuilder();
            messageData.addContent(object.getString("messageText"));
            messageData.addEmbeds(twitchNotificationPattern(event));

            if (messageChannel != null) {
                messageSendActions.add(messageChannel.sendMessage(messageData.build()));
            }
        }

        RestAction.allOf(messageSendActions).mapToResult().queue();
    }

    final @NotNull MessageEmbed twitchNotificationPattern(@NotNull ChannelGoLiveEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        User user = clientHelper.getTwitchHelix().getUsers(null, Collections.singletonList(event.getChannel().getId()), null).execute().getUsers().getFirst();
        embedBuilder.setTitle(event.getStream().getTitle(), "https://www.twitch.tv/%s".formatted(user.getLogin()));
        embedBuilder.setAuthor(event.getStream().getUserName(), null, user.getProfileImageUrl());
        embedBuilder.setColor(new Color(0x431282));
        embedBuilder.setImage(event.getStream().getThumbnailUrl(1600, 900) + new SecureRandom());
        embedBuilder.setTimestamp(OffsetDateTime.now());

        return embedBuilder.build();
    }
}
