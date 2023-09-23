package de.SparkArmy.springBoot;


import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.FileHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked","rawtypes"})
@RestController
@Component
public class YouTubeMappings {


    @ResponseBody
    @GetMapping("/index")
    public String onSubscribe(@RequestParam @NotNull Map allRequestParams) {
        return allRequestParams.getOrDefault("hub.challenge","Error").toString();
    }

    private final HashMap<String,String> sentVideos = new HashMap<>();
    private final File directory = FileHandler.getDirectoryInUserDirectory("botstuff/notifications");

    @ResponseBody
    @PostMapping(value = "/index", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void onYoutubeVideoPublished(@RequestBody @NotNull String s) {

        if (directory == null) return;
        if (s.isEmpty()) return;

        JSONObject requestBody = XML.toJSONObject(s);
        if (requestBody.isEmpty() || requestBody.isNull("feed")) return;
        JSONObject feed = requestBody.getJSONObject("feed");
        if (feed.isEmpty() || feed.isNull("entry")) return;
        JSONObject entry = feed.getJSONObject("entry");
        if (entry.isEmpty() || entry.isNull("yt:channelId") || entry.isNull("yt:videoId")) return;

        String userId = entry.getString("yt:channelId");
        String videoId = entry.getString("yt:videoId");

        LocalDateTime published = LocalDateTime.parse(entry.getString("published"), DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime updated = LocalDateTime.parse(feed.getString("updated"),DateTimeFormatter.ISO_DATE_TIME);
        if (published.plusMinutes(5).isAfter(updated)) return;
        
        if (sentVideos.containsKey(userId)) {
            if (sentVideos.get(userId).equals(videoId)) return;
        }

        sentVideos.put(userId, videoId);
        JDA jda = MainUtil.jda;
        jda.getGuilds().forEach(guild -> {
            if (guild.equals(MainUtil.storageServer)) return;
            String filename = String.format("%s.json", guild.getId());
            File configFile = FileHandler.getFileInDirectory(directory, filename);
            if (!configFile.exists()) return;
            String contentString = FileHandler.getFileContent(configFile);
            if (contentString == null) return;
            JSONObject fileContent = new JSONObject(contentString);
            if (fileContent.isNull("youtube")) return;
            JSONObject watchlist = fileContent.getJSONObject("youtube");
            if (!watchlist.keySet().contains(userId)) return;
            JSONObject userSpecs = watchlist.getJSONObject(userId);
            StringBuilder rolesString = new StringBuilder();
            userSpecs.getJSONArray("roles").forEach(x -> {
                Role role = guild.getRoleById(x.toString());
                if (role == null) return;
                rolesString.append(role.getAsMention()).append(",");
            });
            rolesString.deleteCharAt(rolesString.length() - 1);
            String videoLink = String.format("https://youtu.be/%s",videoId);
            String finalString = String.format("%s %s %s",rolesString,userSpecs.getString("message"),videoLink);

            userSpecs.getJSONArray("channel").forEach(x->{
                Channel channel = guild.getGuildChannelById(x.toString());
                if (channel == null) return;
                ChannelUtil.sendMessageInRightChannel(finalString,channel);
            });


        });

    }

}
