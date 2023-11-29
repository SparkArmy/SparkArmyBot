package de.SparkArmy.controller;

import de.SparkArmy.Main;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.jda.utils.MediaOnlyPermissions;
import de.SparkArmy.utils.FileHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.awt.*;
import java.io.File;
import java.util.List;


public class ConfigController {
    private final Main main;
    private final Logger logger;
    private final File configFolder = FileHandler.getDirectoryInUserDirectory("configs");

    public Main getMain() {
        return this.main;
    }


    public ConfigController(@NotNull Main main) {
        this.main = main;
        this.logger = main.getLogger();
    }

    public JSONObject getMainConfigFile(){
        if (!FileHandler.getFileInDirectory(this.configFolder,"main-config.json").exists()){
            this.logger.warn("The main-config.json-file not exist, we will created a new");
            JSONObject blankConfig = new JSONObject();
            JSONObject discord = new JSONObject(){{
                put("discord-token","Write here your Discord-Bot-Token");
                put("discord-client-id","Write here your Discord-Bot-ClientId");
            }};
            blankConfig.put("discord",discord);
            JSONObject twitch = new JSONObject(){{
                put("twitch-client-secret","[Optional] Write here your Twitch-Client-Secret");
                put("twitch-client-id","[Optional] Write here your Twitch-Client-Id");
            }};
            blankConfig.put("twitch",twitch);
            JSONObject youtube = new JSONObject(){{
                put("youtube-api-key","[Optional] Write here your API-Key from YouTube");
                put("spring-callback-url","[Optional] Your callback domain");
            }};
            blankConfig.put("youtube",youtube);
            JSONObject postgres = new JSONObject(){{
                put("url","The url for the database");
                put("user","Database-User");
                put("password", "User-Password");
            }};
            blankConfig.put("postgres", postgres);
            JSONObject otherKeys = new JSONObject() {{
                put("virustotal-api-key", "[Optional] Write here your API-Key from VirusTotal");
                put("storage-server", "Please setup a new server, delete all included channels and put the id in this field");
                put("twitter_bearer", "Please copy the twitter bearer in this field");
            }};
            blankConfig.put("otherKeys", otherKeys);

            if (FileHandler.writeValuesInFile(this.configFolder, "main-config.json", blankConfig)) {
                this.logger.info("main-config.json was successful created");
                this.logger.warn("Please finish your configuration");
                try {
                    Desktop.getDesktop().open(FileHandler.getFileInDirectory(this.configFolder, "main-config.json"));
                } catch (Exception ignored) {
                }
            } else {
                this.logger.error("Can't create a main-config.json");
                this.main.systemExit(1);
            }

            this.main.systemExit(0);
        }
        String mainConfigAsString = FileHandler.getFileContent(this.configFolder, "main-config.json");
        if (mainConfigAsString == null) {
            this.logger.error("The main-config.json is null");
            this.main.systemExit(12);
        }
        assert mainConfigAsString != null;
        return new JSONObject(mainConfigAsString);
    }

    public long setGuildLoggingChannel(@NotNull LogChannelType logChannelType, @NotNull Channel channel, @NotNull Guild guild, String url) {
        return main.getPostgres().writeInLogchannelTable(guild.getIdLong(), logChannelType.getId(), channel.getIdLong(), url);
    }

    public long getGuildLoggingChannel(@NotNull LogChannelType logChannelType, @NotNull Guild guild) {
        return main.getPostgres().getChannelIdFromLogchannelTable(logChannelType.getId(), guild.getIdLong());
    }

    public String getGuildLoggingChannelUrl(@NotNull LogChannelType logChannelType, @NotNull Guild guild) {
        return main.getPostgres().getUrlByLogchannelTypeAndGuildIdFromLogchannelTable(logChannelType.getId(), guild.getIdLong());
    }

    public List<String> getLoggingChannelWebhookUrls() {
        return main.getPostgres().getUrlsFromLogchannelTable();
    }

    public long setGuildArchiveCategory(@NotNull GuildChannel category, @NotNull Guild guild) {
        return main.getPostgres().writeInArchiveCategoryTable(category.getIdLong(), guild.getIdLong());
    }

    public long getGuildArchiveCategory(@NotNull Guild guild) {
        return main.getPostgres().getCategoryIdFromArchiveCategoryTable(guild.getIdLong());
    }

    public long clearGuildArchiveCategory(@NotNull Guild guild) {
        return main.getPostgres().clearGuildArchiveFromArchiveCategoryTable(guild.getIdLong());
    }

    public long addOrEditGuildMediaOnlyChannel(long channelId, @NotNull Guild guild, @NotNull List<MediaOnlyPermissions> permissions) {
        long guildId = guild.getIdLong();
        boolean textPerms = permissions.contains(MediaOnlyPermissions.TEXT);
        boolean attachmentsPerms = permissions.contains(MediaOnlyPermissions.ATTACHMENT);
        boolean filePerms = permissions.contains(MediaOnlyPermissions.FILES);
        boolean linkPerms = permissions.contains(MediaOnlyPermissions.LINKS);
        return main.getPostgres().writeInMediaOnlyChannelTable(channelId, guildId, textPerms, attachmentsPerms, filePerms, linkPerms);
    }

    public JSONObject getGuildMediaOnlyChannels(@NotNull Guild guild) {
        return main.getPostgres().getChannelIdsFromMediaOnlyChannelTable(guild.getIdLong());
    }

    public JSONObject getGuildMediaOnlyChannelPermissions(long channelId) {
        return main.getPostgres().getChannelPermissionsByChannelIdFromMediaOnlyChannelTable(channelId);
    }

    public long removeGuildMediaOnlyChannel(long channelId) {
        return main.getPostgres().removeChannelFromMediaOnlyChannelTable(channelId);
    }

    public long addGuildModerationRole(@NotNull Role role, @NotNull Guild guild) {
        return main.getPostgres().addModRoleToModRolesTable(role.getIdLong(), guild.getIdLong());
    }

    public long removeGuildModerationRole(@NotNull Role role) {
        return main.getPostgres().deleteModRoleFromModRolesTable(role.getIdLong());
    }

    public List<Long> getGuildModerationRoles(@NotNull Guild guild) {
        return main.getPostgres().getModRoleIdsByGuildFromModRolesTable(guild.getIdLong());
    }

    public long setGuildWarnRole(@NotNull Role warnRole, @NotNull Guild guild) {
        return main.getPostgres().addOrEditRoleIdInGuildWarnRoleTable(warnRole.getIdLong(), guild.getIdLong());
    }

    public long getGuildWarnRole(@NotNull Guild guild) {
        return main.getPostgres().getWarnRoleIdByGuildFromMuteRoleTable(guild.getIdLong());
    }

    public long setGuildMuteRole(@NotNull Role muteRole, @NotNull Guild guild) {
        return main.getPostgres().addOrEditRoleIdInGuildMuteRoleTable(muteRole.getIdLong(), guild.getIdLong());
    }

    public long getGuildMuteRole(@NotNull Guild guild) {
        return main.getPostgres().getMuteRoleIdByGuildFromMuteRoleTable(guild.getIdLong());
    }

    public long addPhraseToGuildTextBlacklist(String phrase, @NotNull Guild guild) {
        return main.getPostgres().addPhraseToBlacklistTable(phrase, guild.getIdLong());
    }

    public JSONObject getGuildBlacklistPhrases(@NotNull Guild guild) {
        return main.getPostgres().getPhrasesByGuildIdFromBlacklistTable(guild.getIdLong());
    }

    public String getSpecificBlacklistPhrase(long id) {
        return main.getPostgres().getSpecificBlacklistPhraseByIdFromBlacklistTable(id);
    }

    public long updatePhraseFromGuildTextBlacklist(String phrase, long databaseId) {
        return main.getPostgres().updatePhraseInBlacklistTable(phrase, databaseId);
    }

    public long deletePhraseFromGuildTextBlacklist(long id) {
        return main.getPostgres().removePhraseFromBlacklistTable(id);
    }

    public long addOrEditRegexToGuildRegexTable(String phrase, String name, @NotNull Guild guild, long id) {
        return main.getPostgres().addOrEditRegexToRegexTable(guild.getIdLong(), phrase, name, id);
    }

    public JSONObject getGuildRegexEntries(@NotNull Guild guild) {
        return main.getPostgres().getRegexEntriesByGuildIdFromRegexTable(guild.getIdLong());
    }

    public JSONObject getRegexEntryById(String id) {
        return main.getPostgres().getRegexEntryByDatabaseIdFromRegexTable(Long.parseLong(id));
    }

    public long removeGuildRegexEntry(long id) {
        return main.getPostgres().removeRegexEntryByDatabaseIdFromRegexTable(id);
    }

    public long setGuildFeedbackChannel(@NotNull Channel channel, @NotNull Guild guild) {
        return main.getPostgres().writeInFeedbackChannelTable(channel.getIdLong(), guild.getIdLong());
    }

    public long removeGuildFeedbackChannel(@NotNull Guild guild) {
        return main.getPostgres().removeFromFeedbackChannelTable(guild.getIdLong());
    }

    public long getGuildFeedbackChannel(@NotNull Guild guild) {
        return main.getPostgres().getFeedbackChannelByGuildIdFromFeedbackChannelTable(guild.getIdLong());
    }

    public List<Long> getGuildModmailBlacklistedUsers(@NotNull Guild guild) {
        return main.getPostgres().getBlacklistedModmailUsersFromModmailBlacklistTable(guild.getIdLong());
    }

    public long isUserOnGuildModmailBlacklist(@NotNull Guild guild, @NotNull User user) {
        return main.getPostgres().isGuildUserInModmailBlacklistTable(user.getIdLong(), guild.getIdLong());
    }

    public long removeUserFromGuildModmailBlacklist(@NotNull Guild guild, @NotNull User user) {
        return main.getPostgres().removeUserFromModmailBlacklistTable(user.getIdLong(), guild.getIdLong());
    }

    public long addUserToGuildModmailBlacklist(@NotNull Guild guild, @NotNull User user) {
        return main.getPostgres().addUserToModmailBlacklistTable(user.getIdLong(), guild.getIdLong());
    }

    public long setGuildModmailCategory(@NotNull Guild guild, @NotNull Category category) {
        return main.getPostgres().writeCategoryIdInModmailChannelTable(category.getIdLong(), guild.getIdLong());
    }

    public long disableGuildModmail(@NotNull Guild guild) {
        return main.getPostgres().removeDataFromModmailChannelTable(guild.getIdLong());
    }

    public long setGuildModmailArchiveChannel(@NotNull Guild guild, TextChannel channel) {
        return main.getPostgres().setArchiveChannelInModmailChannelTable(guild.getIdLong(), channel != null ? channel.getIdLong() : null);
    }

    public long setGuildModmailLogChannel(@NotNull Guild guild, TextChannel channel) {
        return main.getPostgres().setLogChannelInModmailChannelTable(guild.getIdLong(), channel != null ? channel.getIdLong() : null);
    }

    public long isRoleGuildModmailPingRole(@NotNull Role role) {
        return main.getPostgres().isRoleInModmailPingRoleTable(role.getIdLong());
    }

    public long addGuildModmailPingRole(@NotNull Role role, @NotNull Guild guild) {
        return main.getPostgres().addRoleToModmailPingRoleTable(role.getIdLong(), guild.getIdLong());
    }

    public long removeGuildModmailPingRole(@NotNull Role role) {
        return main.getPostgres().removeRoleFromModmailPingRoleTable(role.getIdLong());
    }

    public List<Long> getGuildModmailPingRoles(@NotNull Guild guild) {
        return main.getPostgres().getRoleIdsFromModmailPingRoleTable(guild.getIdLong());
    }
}
