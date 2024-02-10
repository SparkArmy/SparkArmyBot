package de.SparkArmy.controller;

import de.SparkArmy.Main;
import de.SparkArmy.db.DatabaseAction;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.jda.utils.MediaOnlyPermissions;
import de.SparkArmy.utils.ErrorCodes;
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
            JSONObject blankConfig = getBlankConfig();

            if (FileHandler.writeValuesInFile(this.configFolder, "main-config.json", blankConfig)) {
                this.logger.debug("main-config.json was successful created");
                this.logger.warn("Please finish your configuration");
                try {
                    Desktop.getDesktop().open(FileHandler.getFileInDirectory(this.configFolder, "main-config.json"));
                } catch (Exception ignored) {
                    this.main.systemExit(ErrorCodes.GENERAL_CONFIG_ERROR.getId());
                }
            } else {
                this.logger.error(ErrorCodes.GENERAL_CONFIG_CANT_CREATE_MAIN_CONFIG.getDescription());
                this.main.systemExit(ErrorCodes.GENERAL_CONFIG_CANT_CREATE_MAIN_CONFIG.getId());
            }

            this.main.systemExit(0);
        }
        String mainConfigAsString = FileHandler.getFileContent(this.configFolder, "main-config.json");
        if (mainConfigAsString == null) {
            this.logger.error(ErrorCodes.GENERAL_CONFIG_IS_NULL.getDescription());
            this.main.systemExit(ErrorCodes.GENERAL_CONFIG_IS_NULL.getId());
            return new JSONObject();
        }
        return new JSONObject(mainConfigAsString);
    }

    @NotNull
    private static JSONObject getBlankConfig() {
        JSONObject blankConfig = new JSONObject();
        JSONObject discord = new JSONObject() {{
            put("discord-token", "Write here your Discord-Bot-Token");
            put("discord-client-id", "Write here your Discord-Bot-ClientId");
        }};
        blankConfig.put("discord", discord);
        JSONObject twitch = new JSONObject() {{
            put("twitch-client-secret", "[Optional] Write here your Twitch-Client-Secret");
            put("twitch-client-id", "[Optional] Write here your Twitch-Client-Id");
        }};
        blankConfig.put("twitch", twitch);
        JSONObject youtube = new JSONObject() {{
            put("youtube-api-key", "[Optional] Write here your API-Key from YouTube");
            put("spring-callback-url", "[Optional] Your callback domain");
        }};
        blankConfig.put("youtube", youtube);
        JSONObject postgres = new JSONObject() {{
            put("url", "The url for the database");
            put("user", "Database-User");
            put("password", "User-Password");
        }};
        blankConfig.put("postgres", postgres);
        JSONObject otherKeys = new JSONObject() {{
            put("virusTotal-api-key", "[Optional] Write here your API-Key from VirusTotal");
        }};
        blankConfig.put("otherKeys", otherKeys);
        return blankConfig;
    }

    public long setGuildLoggingChannel(@NotNull LogChannelType logChannelType, @NotNull Channel channel, @NotNull Guild guild, String url) {
        return new DatabaseAction().writeInLogChannelTable(guild.getIdLong(), logChannelType, channel.getIdLong(), url);
    }

    public long removeGuildLoggingChannel(LogChannelType logChannelType, @NotNull Channel channel) {
        return new DatabaseAction().removeLogChannelStateFromLogChannelTable(logChannelType, channel.getIdLong());
    }

    public long getGuildLoggingChannel(@NotNull LogChannelType logChannelType, @NotNull Guild guild) {
        return new DatabaseAction().getLogChannelIdByLogChannelTypeAndGuildId(logChannelType, guild.getIdLong());
    }

    public String getGuildLoggingChannelUrl(@NotNull LogChannelType logChannelType, @NotNull Guild guild) {
        return new DatabaseAction().getLogChannelWebhookUrlByLogChannelTypeAndGuildId(logChannelType, guild.getIdLong());
    }

    public List<String> getLoggingChannelWebhookUrls() {
        return new DatabaseAction().getWebhookUrlsFromGuild();
    }

    public long setGuildArchiveCategory(@NotNull GuildChannel category, @NotNull Guild guild) {
        return new DatabaseAction().setArchiveCategory(category.getIdLong(), guild.getIdLong());
    }

    public long getGuildArchiveCategory(@NotNull Guild guild) {
        return new DatabaseAction().getArchiveCategoryByGuildId(guild.getIdLong());
    }

    public long clearGuildArchiveCategory(@NotNull Guild guild) {
        return new DatabaseAction().removeArchiveCategoryByGuildId(guild.getIdLong());
    }

    public long addOrEditGuildMediaOnlyChannel(long channelId, @NotNull Guild guild, @NotNull List<MediaOnlyPermissions> permissions) {
        long guildId = guild.getIdLong();
        boolean textPerms = permissions.contains(MediaOnlyPermissions.TEXT);
        boolean attachmentsPerms = permissions.contains(MediaOnlyPermissions.ATTACHMENT);
        boolean filePerms = permissions.contains(MediaOnlyPermissions.FILES);
        boolean linkPerms = permissions.contains(MediaOnlyPermissions.LINKS);
        return new DatabaseAction().addMediaOnlyChannel(channelId, guildId, textPerms, attachmentsPerms, filePerms, linkPerms);
    }

    public JSONObject getGuildMediaOnlyChannels(@NotNull Guild guild) {
        return new DatabaseAction().getMediaOnlyChannelDataByGuildId(guild.getIdLong());
    }

    public JSONObject getGuildMediaOnlyChannelPermissions(long channelId) {
        return new DatabaseAction().getMediaOnlyChannelDataByChannelId(channelId);
    }

    public long removeGuildMediaOnlyChannel(long channelId) {
        return new DatabaseAction().removeMediaOnlyChannel(channelId);
    }

    public long addGuildModerationRole(@NotNull Role role) {
        return new DatabaseAction().addModerationRole(role.getIdLong(), role.getGuild().getIdLong());
    }

    public long removeGuildModerationRole(@NotNull Role role) {
        return new DatabaseAction().removeModerationRole(role.getIdLong(), role.getGuild().getIdLong());
    }

    public List<Long> getGuildModerationRoles(@NotNull Guild guild) {
        return new DatabaseAction().getModerationRolesByGuildId(guild.getIdLong());
    }

    public long setGuildWarnRole(@NotNull Role warnRole) {
        return new DatabaseAction().setWarnRole(warnRole.getGuild().getIdLong(), warnRole.getIdLong());
    }

    public long disableGuildWarnRole(@NotNull Guild guild) {
        return new DatabaseAction().disableWarnRole(guild.getIdLong());
    }

    public long getGuildWarnRole(@NotNull Guild guild) {
        return new DatabaseAction().getWarnRoleByGuildId(guild.getIdLong());
    }

    public long setGuildMuteRole(@NotNull Role muteRole) {
        return new DatabaseAction().setMuteRole(muteRole.getGuild().getIdLong(), muteRole.getIdLong());
    }

    public long disableGuildMuteRole(@NotNull Guild guild) {
        return new DatabaseAction().disableMuteRole(guild.getIdLong());
    }

    public long getGuildMuteRole(@NotNull Guild guild) {
        return new DatabaseAction().getMuteRoleByGuildId(guild.getIdLong());
    }

    public long addPhraseToGuildTextBlacklist(String phrase, @NotNull Guild guild) {
        return new DatabaseAction().addPhraseToBlacklistTable(phrase, guild.getIdLong());
    }

    public JSONObject getGuildBlacklistPhrases(@NotNull Guild guild) {
        return new DatabaseAction().getPhrasesFromBlacklistTableByGuildId(guild.getIdLong());
    }

    public String getSpecificBlacklistPhrase(long id) {
        return new DatabaseAction().getPhraseByDatabaseId(id);
    }

    public long updatePhraseFromGuildTextBlacklist(String phrase, long databaseId, @NotNull Guild guild) {
        return new DatabaseAction().updatePhraseInBlacklistTable(databaseId, phrase, guild.getIdLong());
    }

    public long deletePhraseFromGuildTextBlacklist(long id) {
        return new DatabaseAction().removePhraseFromBlacklistTable(id);
    }

    public long addOrEditRegexToGuildRegexTable(String phrase, String name, @NotNull Guild guild, long id) {
        return new DatabaseAction().writeInRegexTable(guild.getIdLong(), phrase, name, id);
    }

    public JSONObject getGuildRegexEntries(@NotNull Guild guild) {
        return new DatabaseAction().getRegexEntriesByGuildId(guild.getIdLong());
    }

    public JSONObject getRegexEntryById(String id) {
        return new DatabaseAction().getRegexEntryByDatabaseId(Long.parseLong(id));
    }

    public long removeGuildRegexEntry(long id) {
        return new DatabaseAction().removeRegexEntry(id);
    }

    public long setGuildFeedbackChannel(@NotNull GuildChannel channel) {
        return new DatabaseAction().setGuildFeedbackChannel(channel.getIdLong(), channel.getGuild().getIdLong());
    }

    public long removeGuildFeedbackChannel(@NotNull Guild guild) {
        return new DatabaseAction().removeGuildFeedbackChannel(guild.getIdLong());
    }

    public long getGuildFeedbackChannel(@NotNull Guild guild) {
        return new DatabaseAction().getGuildFeedbackChannelByGuildId(guild.getIdLong());
    }

    public List<Long> getGuildModMailBlacklistedUsers(@NotNull Guild guild) {
        return new DatabaseAction().getUserIdsFromModMailBlacklistTableByGuildId(guild.getIdLong());
    }

    public long isUserOnGuildModMailBlacklist(@NotNull Guild guild, @NotNull User user) {
        return new DatabaseAction().isUserOnModMailBlacklist(guild.getIdLong(), user.getIdLong());
    }

    public long removeUserFromGuildModMailBlacklist(@NotNull Guild guild, @NotNull User user) {
        return new DatabaseAction().removeUserFromModMailBlacklist(guild.getIdLong(), user.getIdLong());
    }

    public long addUserToGuildModMailBlacklist(@NotNull Guild guild, @NotNull User user) {
        return new DatabaseAction().addUserToModMailBlacklist(guild.getIdLong(), user.getIdLong());
    }

    public long setGuildModMailCategory(@NotNull Category category) {
        return new DatabaseAction().writeCategoryInModMailChannelTable(category.getIdLong(), category.getGuild().getIdLong());
    }

    public long getGuildModMailCategory(@NotNull Guild guild) {
        return new DatabaseAction().getCategoryIdByGuildIdFromModMailChannelTable(guild.getIdLong());
    }

    public long disableGuildModMail(@NotNull Guild guild) {
        return new DatabaseAction().removeDataFromModMailChannelTable(guild.getIdLong());
    }

    public long setGuildModMailArchiveChannel(@NotNull Guild guild, TextChannel channel) {
        return new DatabaseAction().setModMailArchiveChannel(guild.getIdLong(), channel != null ? channel.getIdLong() : null);
    }

    public long getGuildModMailArchiveChannel(@NotNull Guild guild) {
        return new DatabaseAction().getModMailArchiveChannelByGuildId(guild.getIdLong());
    }

    public long setGuildModMailLogChannel(@NotNull Guild guild, TextChannel channel) {
        return new DatabaseAction().setModMailLogChannel(guild.getIdLong(), channel != null ? channel.getIdLong() : null);
    }

    public long getGuildModMailLogChannel(@NotNull Guild guild) {
        return new DatabaseAction().getModMailLogChannelByGuildId(guild.getIdLong());
    }

    public long isRoleGuildModMailPingRole(@NotNull Role role) {
        return new DatabaseAction().isRoleModMailPingRole(role.getIdLong(), role.getGuild().getIdLong());
    }

    public long addGuildModMailPingRole(@NotNull Role role) {
        return new DatabaseAction().addModMailPingRole(role.getGuild().getIdLong(), role.getIdLong());
    }

    public long removeGuildModMailPingRole(@NotNull Role role) {
        return new DatabaseAction().removeModMailPingRole(role.getGuild().getIdLong(), role.getIdLong());
    }

    public List<Long> getGuildModMailPingRoles(@NotNull Guild guild) {
        return new DatabaseAction().getModMailPingRoleIdsByGuildId(guild.getIdLong());
    }
}
