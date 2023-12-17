package de.SparkArmy.webserver.mappings;

import de.SparkArmy.db.DatabaseAction;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@RestController
@Component
public class YouTubePubSubMapping {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ResponseBody
    @GetMapping("/pubsubservice/youtube")
    public String onSubscribe(@RequestParam @NotNull Map allRequestParams) {
        return allRequestParams.getOrDefault("hub.challenge","Error").toString();
    }

    @ResponseBody
    @PostMapping(value = "/pubsubservice/youtube", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void onYoutubeVideoPublished(@RequestBody @NotNull String s) {
        DatabaseAction db = new DatabaseAction();
        JSONObject requestBody = XML.toJSONObject(s);
        if (requestBody.isEmpty() || requestBody.isNull("feed")) return;
        JSONObject feed = requestBody.getJSONObject("feed");
        if (feed.isEmpty() || feed.isNull("entry")) return;
        JSONObject entry = feed.getJSONObject("entry");
        if (entry.isEmpty()) return;

        String videoId = entry.getString("yt:videoId");

        LocalDateTime published = LocalDateTime.parse(entry.getString("published"), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime updated = LocalDateTime.parse(feed.getString("updated"), DateTimeFormatter.ISO_DATE_TIME);

        if (updated.isAfter(published.plusMinutes(15))) return;

        if (db.putIdInReceivedVideosTable(videoId) <= 0) {
            return;
        }

        JSONArray tableData = db.getDataFromSubscribedChannelTableByContentCreatorId(entry.getString("yt:channelId"));

        Collection<MessageCreateAction> messageSendActions = new ArrayList<>();

        for (Object o : tableData) {
            JSONObject object = (JSONObject) o;
            MessageChannel messageChannel = (MessageChannel) Util.controller.getMain().getJdaApi().getShardManager().getGuildChannelById(object.getLong("messageChannelId"));
            MessageCreateBuilder messageData = new MessageCreateBuilder();
            messageData.addContent(
                    object.getString("messageText") +
                    "\n" + "https://youtu.be/%s".formatted(videoId));

            if (messageChannel != null) {
                messageSendActions.add(messageChannel.sendMessage(messageData.build()));
            }
        }

        RestAction.allOf(messageSendActions).mapToResult().queue();
    }
}
