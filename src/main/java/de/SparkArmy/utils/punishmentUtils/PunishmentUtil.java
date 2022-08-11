package de.SparkArmy.utils.punishmentUtils;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.ChannelUtils;
import de.SparkArmy.utils.LogChannelType;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class PunishmentUtil {

    private static final ConfigController controller = MainUtil.controller;

    public static OffsetDateTime getRemoveTime(OptionMapping duration, OptionMapping timeUnit) {
        OffsetDateTime time = null;
        if (duration != null && timeUnit != null) {
            time = OffsetDateTime.now();
            String unit = timeUnit.getAsString();
            int dur = duration.getAsInt();
            switch (unit) {
                case "minuets" -> time = time.plusMinutes(dur);
                case "hours" -> time = time.plusHours(dur);
                case "days" -> time = time.plusDays(dur);
                case "months" -> time = time.plusMonths(dur);
                case "years" -> time = time.plusYears(dur);
                default -> time = null;
            }
        } else if (duration == null && timeUnit != null) {
            time = OffsetDateTime.now();
            String unit = timeUnit.getAsString();
            switch (unit) {
                case "minuets" -> time = time.plusMinutes(1);
                case "hours" -> time = time.plusHours(1);
                case "days" -> time = time.plusDays(1);
                case "months" -> time = time.plusMonths(1);
                case "years" -> time = time.plusYears(1);
                default -> time = null;
            }
        } else if (duration != null) {
            time = OffsetDateTime.now();
            int dur = duration.getAsInt();
            time = time.plusMinutes(dur);
        }
        return time;
    }

    public static boolean giveUserPunishment(Member offender, Guild guild, PunishmentType type) {
        JSONObject config = controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
        if (config.isNull("punishments")) {
            ChannelUtils.logInLogChannel(guild.getPublicRole().getAsMention() + " Please set punishment-parameters", guild, LogChannelType.SERVER);
            return false;
        }
        JSONObject punishment = config.getJSONObject("punishments").getJSONObject(type.getName());
        Role punishmentRole;
        switch (type) {
            case WARN -> {
                if ( !punishment.optBoolean("active")) return true;
                punishmentRole = guild.getRoleById(punishment.getString("role-id"));
                if (punishmentRole == null) {
                    ChannelUtils.logInLogChannel(guild.getPublicRole().getAsMention() + " Please set a warn-role", guild, LogChannelType.SERVER);
                    return false;
                }
                guild.addRoleToMember(offender, punishmentRole).queue();
                return true;
            }
            case MUTE -> {
                if ( !punishment.optBoolean("active")) return true;
                punishmentRole = guild.getRoleById(punishment.getString("role-id"));
                if (punishmentRole == null) {
                    ChannelUtils.logInLogChannel(guild.getPublicRole().getAsMention() + " Please set a mute-role", guild, LogChannelType.SERVER);
                    return false;
                }
                guild.addRoleToMember(offender, punishmentRole).queue();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public static void warnOrMute(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!(eventName.equals("warn") || eventName.equals("mute"))) return;

        Guild guild = event.getGuild();
        if (guild == null) return;
        Member offender = Objects.requireNonNull(event.getOption("target_user")).getAsMember();
        if (offender == null) {
            event.reply("Please give a valid target").setEphemeral(true).queue();
            return;
        }

        Member moderator = event.getMember();
        if (moderator == null) return;

        if (offender.equals(moderator)){
            event.reply("You can't warn yourself").setEphemeral(true).queue();
            return;
        } else if (offender.getUser().isBot()) {
        event.reply("You can't warn a bot").setEphemeral(true).queue();
        return;
        } else if (!offender.getRoles().isEmpty() && !moderator.canInteract(offender.getRoles().get(0))) {
            event.reply("You can't warn a member with a same/higher role").setEphemeral(true).queue();
            return;
        } else if (offender.hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You can't warn a administrator").setEphemeral(true).queue();
            return;
        }


        OffsetDateTime removeTime = null;
        OptionMapping duration = event.getOption("duration");
        OptionMapping timeUnit = event.getOption("time_unit");
        if (duration != null || timeUnit != null) {
            removeTime = PunishmentUtil.getRemoveTime(duration, timeUnit);
            if (removeTime == null) {
                event.reply("Please check the time_unit parameter").setEphemeral(true).queue();
                return;
            }
        }

        OptionMapping reason = event.getOption("reason");
        String reasonString;
        if (reason == null) {
            reasonString = "No reason was provided";
        } else {
            reasonString = reason.getAsString();
        }
        EmbedBuilder userEmbed = PunishmentEmbeds.punishmentUserEmbed(guild, reasonString, eventName.equals("warn") ? PunishmentType.WARN : PunishmentType.MUTE);
        EmbedBuilder serverEmbed = PunishmentEmbeds.punishmentLogEmbed(guild, reasonString, offender.getUser(), moderator.getUser(), eventName.equals("warn") ? PunishmentType.WARN : PunishmentType.MUTE);

        if (removeTime != null) {
            new TemporaryPunishment(offender.getUser(), eventName.equals("warn") ? PunishmentType.WARN : PunishmentType.MUTE, removeTime, guild);
            userEmbed = PunishmentEmbeds.punishmentUserEmbed(guild, reasonString, removeTime, eventName.equals("warn") ? PunishmentType.WARN : PunishmentType.MUTE);
            serverEmbed = PunishmentEmbeds.punishmentLogEmbed(guild, reasonString, offender.getUser(), moderator.getUser(), removeTime, eventName.equals("warn") ? PunishmentType.WARN : PunishmentType.MUTE);
        }
        ChannelUtils.logInLogChannel(serverEmbed, guild, LogChannelType.MOD);
        try {
            offender.getUser().openPrivateChannel().complete().sendMessageEmbeds(userEmbed.build()).queue();
        } catch (Exception ignored) {
        }

            if (!PunishmentUtil.giveUserPunishment(offender,guild,eventName.equals("warn") ? PunishmentType.WARN : PunishmentType.MUTE)){
                event.reply("User have not the punishment-role. Please give it manual").setEphemeral(true).queue();
                return;
            }
        event.reply(eventName.equals("warn") ? "User was warned" : "User was muted").setEphemeral(true).queue();
    }

    public static void sendPunishmentParamEmbed(@NotNull SlashCommandInteractionEvent event, @NotNull OptionMapping punishment, JSONObject config){
        PunishmentType type = PunishmentType.getByName(punishment.getAsString());
        EmbedBuilder embed = PunishmentEmbeds.punishmentStates(type,config);
        Collection<Button> buttons = new ArrayList<>();
        String buttonPrefix = String.format("%s;%s;", Objects.requireNonNull(event.getGuild()).getId(), Objects.requireNonNull(event.getMember()).getId());
        buttons.add(Button.danger(String.format("%s%s,Edit",buttonPrefix,type.getName()),"Edit"));
        buttons.add(Button.success(String.format("%s%s,Exit",buttonPrefix,type.getName()),"Exit"));
        event.replyEmbeds(embed.build()).addActionRows(ActionRow.of(buttons)).setEphemeral(true).queue();
    }

    public static void sendPunishmentParamEmbed(@NotNull ButtonInteractionEvent event,Guild guild){
        JSONObject config = controller.getSpecificGuildConfig(guild,GuildConfigType.MAIN).getJSONObject("punishments");
        PunishmentType type = PunishmentType.getByName(event.getComponentId().split(";")[2].split(",")[1]);
        EmbedBuilder embed = PunishmentEmbeds.punishmentStates(type,config);
        Collection<Button> buttons = new ArrayList<>();
        String buttonPrefix = String.format("%s;%s;", Objects.requireNonNull(event.getGuild()).getId(), Objects.requireNonNull(event.getMember()).getId());
        buttons.add(Button.danger(String.format("%s%s,Edit",buttonPrefix,type.getName()),"Edit"));
        buttons.add(Button.success(String.format("%s%s,Exit",buttonPrefix,type.getName()),"Exit"));
        event.replyEmbeds(embed.build()).addActionRows(ActionRow.of(buttons)).setEphemeral(true).queue();
    }

}
