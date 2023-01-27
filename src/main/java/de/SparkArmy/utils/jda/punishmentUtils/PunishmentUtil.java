package de.SparkArmy.utils.jda.punishmentUtils;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.MainUtil;
import de.SparkArmy.utils.PostgresConnection;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.TargetType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PunishmentUtil {

    private static final ConfigController controller = MainUtil.controller;
    public static final DateTimeFormatter punishmentFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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
                case "weeks" -> time = time.plusWeeks(dur);
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
                case "weeks" -> time = time.plusWeeks(1);
                case "months" -> time = time.plusMonths(1);
                case "years" -> time = time.plusYears(1);
                default -> time = null;
            }
        } else if (duration != null) {
            time = OffsetDateTime.now();
            int dur = duration.getAsInt();
            time = time.plusMinutes(dur);
        }
        // comment out or delete to allow punishments longer than 6 weeks
        if (time != null && time.isAfter(OffsetDateTime.now().plusWeeks(6))){
            time = OffsetDateTime.now().plusWeeks(6);
        }
        //
        return time;
    }

    public static boolean giveUserPunishment(Member offender, Guild guild, PunishmentType type,String reason) {
        // Check if guild has punishment parameters
        JSONObject config = controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN);
        if (config.isNull("punishments")) {
            ChannelUtil.logInLogChannel(guild.getPublicRole().getAsMention() + " Please set punishment-parameters", guild, LogChannelType.SERVER);
            return true;
        }
        JSONObject punishment = config.getJSONObject("punishments").getJSONObject(type.getName());
        Role punishmentRole;
        switch (type) {
            case WARN -> {
                if (!punishment.optBoolean("active")) return false;
                punishmentRole = guild.getRoleById(punishment.getString("role-id"));
                if (punishmentRole == null) {
                    ChannelUtil.logInLogChannel(guild.getPublicRole().getAsMention() + " Please set a warn-role", guild, LogChannelType.SERVER);
                    return true;
                }
                guild.addRoleToMember(offender, punishmentRole).reason(reason).queue();
                return false;
            }
            case MUTE -> {
                if (!punishment.optBoolean("active")) return false;
                punishmentRole = guild.getRoleById(punishment.getString("role-id"));
                if (punishmentRole == null) {
                    ChannelUtil.logInLogChannel(guild.getPublicRole().getAsMention() + " Please set a mute-role", guild, LogChannelType.SERVER);
                    return true;
                }
                guild.addRoleToMember(offender, punishmentRole).reason(reason).queue();
                return false;
            }
            case KICK ->{
                guild.kick(offender).reason(reason).queue();
                return false;
            }
            case BAN -> {
                int days = Integer.parseInt(punishment.getString("standard-deleted-days"));
                guild.ban(offender,days, TimeUnit.DAYS).reason(reason).queue();
                return false;
            }
            default -> {
                return true;
            }
        }
    }

    public static void executePunishment(@NotNull UserContextInteractionEvent event) {
        event.deferReply(true).queue();
        String eventName = event.getName();
        // Checks the event-name
        if (!(eventName.equals("warn") || eventName.equals("mute") || eventName.equals("ban") || eventName.equals("kick"))) return;

        // Checks guild and if the user a member of server
        Guild guild = event.getGuild();
        if (guild == null) return;
        Member offender = event.getTargetMember();
        if (offender == null) {
            event.getHook().editOriginal("Please give a valid target").queue();
            return;
        }

        // Check the moderator
        Member moderator = event.getMember();
        if (moderator == null) return;


        // Conditions to not execute the punishment to yourself, a bot, a member with a higher role, an admin
        if (offender.equals(moderator)) {
            event.getHook().editOriginal("You can't " + eventName + " yourself").queue();
            return;
        } else if (offender.getUser().isBot()) {
            event.getHook().editOriginal("You can't " + eventName + " a bot").queue();
            return;
        } else if (!offender.getRoles().isEmpty() && !moderator.canInteract(offender.getRoles().get(0))) {
            event.getHook().editOriginal("You can't " + eventName + " a member with a same/higher role").queue();
            return;
        } else if (offender.hasPermission(Permission.ADMINISTRATOR)) {
            event.getHook().editOriginal("You can't " + eventName + " a administrator").queue();
            return;
        }


        // Set PunishmentTime to 6 Weeks
        OffsetDateTime removeTime = OffsetDateTime.now().plusWeeks(6);

        String reasonString = "No reason was provided";

        // Create standard Embeds without timestamps
        var userEmbeds = new Object() {
            EmbedBuilder userEmbed;
        };
        EmbedBuilder serverEmbed;

        // if the remove time not null create a timed-punishment and set embeds with a timestamp
        new TemporaryPunishment(offender.getUser(), PunishmentType.getByName(eventName), removeTime, guild);
        userEmbeds.userEmbed = PunishmentEmbeds.punishmentUserEmbed(guild, reasonString, removeTime, PunishmentType.getByName(eventName));
        serverEmbed = PunishmentEmbeds.punishmentLogEmbed(guild, reasonString, offender.getUser(), moderator.getUser(), removeTime, PunishmentType.getByName(eventName));

        // log the punishment in mod-log
        ChannelUtil.logInLogChannel(serverEmbed, guild, LogChannelType.MOD);

        // try to send a message to user and ignore an error if the user has dm disabled or cant create a private channel-connection
        offender.getUser().openPrivateChannel().queue(pc->pc.sendMessageEmbeds(userEmbeds.userEmbed.build()).queue(null,
                new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER)),new ErrorHandler().ignore(UnsupportedOperationException.class));

        // give the user the punishment and write this in the database-table
        if (PunishmentUtil.giveUserPunishment(offender, guild, PunishmentType.getByName(eventName), reasonString)) {
            // if user have not the punishment send this response
            event.getHook().editOriginal("User have not the punishment. Please give it manual").queue();
            return;
        }

        PostgresConnection.putDataInMemberTable(offender);
        PostgresConnection.putDataInModeratorTable(moderator);
        PostgresConnection.putDataInPunishmentTable(offender,moderator,PunishmentType.getByName(eventName),reasonString);

        // response to moderator
        event.getHook().editOriginal(offender.getEffectiveName() + " was " + eventName).queue();
    }

    public static void executePunishment(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        String eventName = event.getName();
        // Checks the event-name
        if (!(eventName.equals("warn") || eventName.equals("mute") || eventName.equals("ban") || eventName.equals("kick"))) return;

        // Checks guild and if the user a member of server
        Guild guild = event.getGuild();
        if (guild == null) return;
        Member offender = Objects.requireNonNull(event.getOption("target_user")).getAsMember();
        if (offender == null) {
            event.getHook().editOriginal("Please give a valid target").queue();
            return;
        }

        // Check the moderator
        Member moderator = event.getMember();
        if (moderator == null) return;


        // Conditions to not execute the punishment to yourself, a bot, a member with a higher role, an admin
        if (offender.equals(moderator)) {
            event.getHook().editOriginal("You can't " + eventName + " yourself").queue();
            return;
        } else if (offender.getUser().isBot()) {
            event.getHook().editOriginal("You can't " + eventName + " a bot").queue();
            return;
        } else if (!offender.getRoles().isEmpty() && !moderator.canInteract(offender.getRoles().get(0))) {
            event.getHook().editOriginal("You can't " + eventName + " a member with a same/higher role").queue();
            return;
        } else if (offender.hasPermission(Permission.ADMINISTRATOR)) {
            event.getHook().editOriginal("You can't " + eventName + " a administrator").queue();
            return;
        }


        // Timed Punishment
        OffsetDateTime removeTime = null;
        OptionMapping duration = event.getOption("duration");
        OptionMapping timeUnit = event.getOption("time_unit");
        // Check if the Duration not null
        if (duration != null || timeUnit != null) {
            // Create a remove time
            removeTime = PunishmentUtil.getRemoveTime(duration, timeUnit);
            if (removeTime == null) {
                event.getHook().editOriginal("Please check the time_unit parameter").queue();
                return;
            }
        }

        // Reason strings
        OptionMapping reason = event.getOption("reason");
        String reasonString;
        if (reason == null) {
            reasonString = "No reason was provided";
        } else {
            reasonString = reason.getAsString();
        }

        // Create standard Embeds without timestamps
        var userEmbeds = new Object() {
            EmbedBuilder userEmbed = PunishmentEmbeds.punishmentUserEmbed(guild, reasonString, PunishmentType.getByName(eventName));
        };
        EmbedBuilder serverEmbed = PunishmentEmbeds.punishmentLogEmbed(guild, reasonString, offender.getUser(), moderator.getUser(), PunishmentType.getByName(eventName));

        // if the remove time not null create a timed-punishment and set embeds with a timestamp
        if (removeTime != null) {
                new TemporaryPunishment(offender.getUser(), PunishmentType.getByName(eventName), removeTime, guild);
                userEmbeds.userEmbed = PunishmentEmbeds.punishmentUserEmbed(guild, reasonString, removeTime, PunishmentType.getByName(eventName));
                serverEmbed = PunishmentEmbeds.punishmentLogEmbed(guild, reasonString, offender.getUser(), moderator.getUser(), removeTime, PunishmentType.getByName(eventName));
            }

        // log the punishment in mod-log
        ChannelUtil.logInLogChannel(serverEmbed, guild, LogChannelType.MOD);

        // Send the user a close message
        offender.getUser().openPrivateChannel().queue(pc->pc.sendMessageEmbeds(userEmbeds.userEmbed.build()).queue(null,
                new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER)),new ErrorHandler().ignore(UnsupportedOperationException.class));

        // give the user the punishment and write this in the database-table
        if (PunishmentUtil.giveUserPunishment(offender, guild, PunishmentType.getByName(eventName), reasonString)) {
            // if user have not the punishment send this response
            event.getHook().editOriginal("User have not the punishment. Please give it manual").queue();
            return;
        }

        PostgresConnection.putDataInMemberTable(offender);
        PostgresConnection.putDataInModeratorTable(moderator);
        PostgresConnection.putDataInPunishmentTable(offender,moderator,PunishmentType.getByName(eventName),reasonString);

        // response to moderator
        event.getHook().editOriginal(offender.getEffectiveName() + " was " + eventName).queue();
    }

    public static void sendPunishmentParamEmbed(@NotNull SlashCommandInteractionEvent event, @NotNull OptionMapping punishment, JSONObject config) {
        PunishmentType type = PunishmentType.getByName(punishment.getAsString());
        EmbedBuilder embed = PunishmentEmbeds.punishmentStates(type, config);
        Collection<Button> buttons = new ArrayList<>();
        String buttonPrefix = String.format("%s;%s;", Objects.requireNonNull(event.getGuild()).getId(), Objects.requireNonNull(event.getMember()).getId());
        buttons.add(Button.danger(String.format("%s%s,Edit", buttonPrefix, type.getName()), "Edit"));
        buttons.add(Button.success(String.format("%s%s,Exit", buttonPrefix, type.getName()), "Exit"));
        event.replyEmbeds(embed.build()).addComponents(ActionRow.of(buttons)).setEphemeral(true).queue();
    }

    public static void sendPunishmentParamEmbed(@NotNull ButtonInteractionEvent event, Guild guild) {
        JSONObject config = controller.getSpecificGuildConfig(guild, GuildConfigType.MAIN).getJSONObject("punishments");
        PunishmentType type = PunishmentType.getByName(event.getComponentId().split(";")[2].split(",")[1]);
        EmbedBuilder embed = PunishmentEmbeds.punishmentStates(type, config);
        Collection<Button> buttons = new ArrayList<>();
        String buttonPrefix = String.format("%s;%s;", Objects.requireNonNull(event.getGuild()).getId(), Objects.requireNonNull(event.getMember()).getId());
        buttons.add(Button.danger(String.format("%s%s,Edit", buttonPrefix, type.getName()), "Edit"));
        buttons.add(Button.success(String.format("%s%s,Exit", buttonPrefix, type.getName()), "Exit"));
        event.replyEmbeds(embed.build()).addComponents(ActionRow.of(buttons)).setEphemeral(true).queue();
    }

    public static final List<String> bannedOrKickedUsers = new ArrayList<>();

    public static void sendBanOrKickEmbed(@NotNull AuditLogEntry entry,Member offender) {
        if (!bannedOrKickedUsers.isEmpty()){
            bannedOrKickedUsers.remove(0);
            return;
        }
        if (entry.getUser() == null) return;
        Member moderator = entry.getGuild().getMember(entry.getUser());
        if (moderator == null) return;
        if (!entry.getTargetType().equals(TargetType.MEMBER)){
            return;
        }
        String reason;
        reason = entry.getReason();
        JSONObject config = controller.getSpecificGuildConfig(entry.getGuild(), GuildConfigType.MAIN);


        JSONObject punishments = config.getJSONObject("punishments");
        switch (entry.getType()){
            case KICK -> {
                if (reason == null) {
                    if (config.isNull("punishments")) {
                        reason = "No reason provided";
                    } else {
                        reason = punishments.getJSONObject("kick").getString("standard-reason");
                    }
                }

                PostgresConnection.putDataInPunishmentTable(offender,moderator,PunishmentType.KICK,reason);
                PostgresConnection.addLeaveTimestampInMemberTable(offender.getIdLong(),entry.getGuild().getIdLong());
                ChannelUtil.logInLogChannel(PunishmentEmbeds.punishmentLogEmbed(entry.getGuild(), entry.getReason() == null ? reason : entry.getReason(), offender.getUser(),moderator.getUser(),PunishmentType.KICK), entry.getGuild(),LogChannelType.MOD);
            }
            case BAN -> {
                if (reason == null) {
                    if (config.isNull("punishments")) {
                        reason = "No reason provided";
                    } else {
                        reason = punishments.getJSONObject("ban").getString("standard-reason");
                    }
                }

                PostgresConnection.putDataInPunishmentTable(offender,moderator,PunishmentType.BAN,reason);
                PostgresConnection.addLeaveTimestampInMemberTable(offender.getIdLong(),entry.getGuild().getIdLong());
                ChannelUtil.logInLogChannel(PunishmentEmbeds.punishmentLogEmbed(entry.getGuild(), entry.getReason() == null ? reason : entry.getReason(), offender.getUser(),moderator.getUser(),PunishmentType.BAN), entry.getGuild(),LogChannelType.MOD);
            }
        }
    }


}
