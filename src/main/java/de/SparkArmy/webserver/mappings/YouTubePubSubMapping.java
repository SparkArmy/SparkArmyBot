package de.SparkArmy.webserver.mappings;

import de.SparkArmy.db.Postgres;
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

        JSONObject requestBody = XML.toJSONObject(s);
        if (requestBody.isEmpty() || requestBody.isNull("feed")) return;
        JSONObject feed = requestBody.getJSONObject("feed");
        if (feed.isEmpty() || feed.isNull("entry")) return;
        JSONObject entry = feed.getJSONObject("entry");
        if (entry.isEmpty()) return;
        if (!entry.getString("published").equals(entry.getString("updated"))) return;

        Postgres db = Util.controller.getMain().getPostgres();
        JSONArray tableData = db.getDataFromSubscribedChannelTableByContentCreatorId(entry.getString("yt:channelId"));

        Collection<MessageCreateAction> messageSendActions = new ArrayList<>();

        for (Object o : tableData) {
            JSONObject object = (JSONObject) o;
            MessageChannel messageChannel = (MessageChannel) Util.controller.getMain().getJdaApi().getShardManager().getGuildChannelById(object.getLong("messageChannelId"));
            MessageCreateBuilder messageData = new MessageCreateBuilder();
            messageData.addContent(
                    object.getString("messageText") +
                    "\n" + "https://youtu.be/%s".formatted(entry.getString("yt:videoId")));

            if (messageChannel != null) {
                messageSendActions.add(messageChannel.sendMessage(messageData.build()));
            }
        }

        RestAction.allOf(messageSendActions).mapToResult().queue();
    }
}
