package de.SparkArmy.utils.jda.punishmentUtils;

import de.SparkArmy.utils.PostgresConnection;
import de.SparkArmy.utils.jda.MessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.time.OffsetDateTime;
public class PunishmentEmbeds{

    public static @NotNull EmbedBuilder punishmentUserEmbed(@NotNull Guild guild, @NotNull String reason, @NotNull PunishmentType type){
        String action;
        switch (type){
            case BAN -> action = "banned";
            case KICK -> action = "kicked";
            case MUTE -> action = "muted";
            case WARN -> action = "warned";
            case TIMEOUT -> action = "timeout";
            default -> action = "unknown";
        }

        String modmailHint =String.format( """
                Have you any questions about this %s?
                Please use "/modmail" in this channel to contact the mods!
                PS: Any other way will be ignored""",type.getName());

        switch (type){
            case MUTE,WARN,TIMEOUT -> reason = reason + "\n" + modmailHint;
        }

        return new EmbedBuilder()
                .setTitle(type.getName())
                .setDescription("You was " + action +  " on " + guild.getName())
                .setColor(new Color(255,0,0))
                .addField("Reason", reason,false)
                .setTimestamp(OffsetDateTime.now());
    }

    public static @NotNull EmbedBuilder punishmentUserEmbed(@NotNull Guild guild, @NotNull String reason, @NotNull OffsetDateTime removeTime, @NotNull PunishmentType type){
        String action;
        switch (type){
            case BAN -> action = "banned";
            case KICK -> action = "kicked";
            case MUTE -> action = "muted";
            case WARN -> action = "warned";
            case TIMEOUT -> action = "timeout";
            default -> action = "unknown";
        }

        String modmailHint =String.format( """
                Have you any questions about this %s?
                Please use "/modmail" in this channel to contact the mods!
                PS: Any other way will be ignored""",type.getName());

        switch (type){
            case MUTE,WARN,TIMEOUT -> reason = reason + "\n" + modmailHint;
        }

        return new EmbedBuilder()
                .setTitle(type.getName())
                .setDescription("You was " + action +  " on " + guild.getName())
                .setColor(new Color(255,0,0))
                .addField("Reason", reason,false)
                .addField("Date of removal", MessageUtil.discordTimestamp(removeTime,"R"),false)
                .setTimestamp(OffsetDateTime.now());
    }

    public static @NotNull EmbedBuilder punishmentLogEmbed(@NotNull Guild guild, @NotNull String reason, @NotNull User offender, @NotNull User moderator, @NotNull PunishmentType type){
        return new EmbedBuilder()
                .setTitle(String.format("%s | %d",type.getName(), PostgresConnection.getLatestPunishmentIdFromPunishmentTable(guild)))
                .setAuthor(moderator.getAsTag(),null,moderator.getEffectiveAvatarUrl())
                .setTimestamp(OffsetDateTime.now())
                .setFooter(guild.getJDA().getSelfUser().getAsTag(),guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .addField("Offender",String.format("%s (%s)",offender.getAsTag(),offender.getId()),false)
                .addField("Reason",reason,false);
    }

    public static @NotNull EmbedBuilder punishmentLogEmbed(@NotNull Guild guild, @NotNull String reason, @NotNull User offender, @NotNull User moderator, @NotNull OffsetDateTime removeTime, @NotNull PunishmentType type){
        return new EmbedBuilder()
                .setTitle(String.format("%s | %d",type.getName(),PostgresConnection.getLatestPunishmentIdFromPunishmentTable(guild)))
                .setAuthor(moderator.getAsTag(),null,moderator.getEffectiveAvatarUrl())
                .setTimestamp(OffsetDateTime.now())
                .setFooter(guild.getJDA().getSelfUser().getAsTag(),guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .addField("Offender",String.format("%s (%s)",offender.getAsTag(),offender.getId()),false)
                .addField("Reason",reason,false)
                .addField("Expired time", MessageUtil.discordTimestamp(removeTime,"R"),false);
    }

    public static EmbedBuilder punishmentStates(@NotNull PunishmentType type, @NotNull JSONObject config){
        JSONObject psm = config.getJSONObject(type.getName());
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(type.getName());
        embed.setDescription(type.getDescription());
        switch (type){
            case BAN -> {
                embed.addField("Deleted Days",psm.getString("standard-deleted-days"),false);
                embed.addField("Standard Reason",psm.getString("standard-reason"),false);
                return embed;
            }
            case KICK -> {
                embed.addField("Standard Reason",psm.getString("standard-reason"),false);
                return embed;
            }
            case MUTE, WARN -> {
                embed.addField("Role active", String.valueOf(psm.getBoolean("active")),false);
                embed.addField("Punishment Role Id",psm.getString("role-id"),false);
                return embed;
            }
            case TIMEOUT -> {
                embed.addField("Standard Reason",psm.getString("standard-reason"),false);
                embed.addField("Standard Duration",psm.getString("standard-duration"),false);
                embed.addField("Standard Time-Unit",psm.getString("standard-time-unit"),false);
                return embed;
            }
            default -> {return embed;}
        }
    }
}
