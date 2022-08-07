package de.SparkArmy.commandListener.guildCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.timedOperations.TemporaryPunishment;
import de.SparkArmy.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Warn extends CustomCommandListener{
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if(!eventName.equals("warn")) return;

        Guild guild = event.getGuild();
        if (guild == null) return;
        User offender = Objects.requireNonNull(event.getOption("target_user")).getAsUser();
        User moderator = event.getUser();

        OffsetDateTime removeTime = null;
        OptionMapping duration = event.getOption("duration");
        OptionMapping timeUnit = event.getOption("time_unit");
        if (duration != null || timeUnit != null){
            removeTime = removeTime(duration,timeUnit);
            if (removeTime == null){
                event.reply("Please check the time_unit parameter").setEphemeral(true).queue();
                return;
            }
        }

        OptionMapping reason = event.getOption("reason");
        String reasonString;
        if (reason == null){
            reasonString = "No reason was provided";
        }else {
            reasonString = reason.getAsString();
        }
        logger.info("1");
        EmbedBuilder userEmbed = Embeds.punishmentUserEmbed(guild,reasonString);
        logger.info("t");
        EmbedBuilder serverEmbed = Embeds.punishmentLogEmbed(guild,reasonString,offender,moderator);
        logger.info("2");

        if (removeTime != null){
            new TemporaryPunishment(offender, PunishmentType.WARN, removeTime,guild);
            userEmbed = Embeds.punishmentUserEmbed(guild,reasonString, removeTime);
            serverEmbed = Embeds.punishmentLogEmbed(guild,reasonString,offender,moderator, removeTime);
        }
        ChannelUtils.logInLogChannel(serverEmbed,guild, LogChannelType.MOD);
        try {
            offender.openPrivateChannel().complete().sendMessageEmbeds(userEmbed.build()).queue();
        }catch (Exception ignored){}

        //TODO give punishment-role user

        event.reply("User was warned").setEphemeral(true).queue();
    }

    private OffsetDateTime removeTime(OptionMapping duration, OptionMapping timeUnit){
        OffsetDateTime time = null;
        if (duration != null && timeUnit != null){
            time = OffsetDateTime.now();
            String unit = timeUnit.getAsString();
            int dur = duration.getAsInt();
            switch (unit){
                case "minuets"->time = time.plusMinutes(dur);
                case "hours" ->time = time.plusHours(dur);
                case "days" ->time = time.plusDays(dur);
                case "months"->time = time.plusMonths(dur);
                case "years"->time = time.plusYears(dur);
                default -> time = null;
            }
        }else if (duration == null && timeUnit != null){
            time = OffsetDateTime.now();
            String unit = timeUnit.getAsString();
            switch (unit){
                case "minuets"->time = time.plusMinutes(1);
                case "hours" ->time = time.plusHours(1);
                case "days" ->time = time.plusDays(1);
                case "months"->time = time.plusMonths(1);
                case "years"->time = time.plusYears(1);
                default -> time = null;
            }
        }else if (duration != null) {
            time = OffsetDateTime.now();
            int dur = duration.getAsInt();
            time = time.plusMinutes(dur);
        }
        return time;
    }

    private enum Embeds{
        ;

        static @NotNull EmbedBuilder punishmentUserEmbed(@NotNull Guild guild, @NotNull String reason){
            return new EmbedBuilder()
                    .setTitle("Warn")
                    .setDescription("You was warned on " + guild.getName())
                    .setColor(new Color(255,0,0))
                    .addField("Reason", reason,false)
                    .setTimestamp(OffsetDateTime.now());
        }

        static @NotNull EmbedBuilder punishmentUserEmbed(@NotNull Guild guild, @NotNull String reason, @NotNull OffsetDateTime removeTime){
            return new EmbedBuilder()
                    .setTitle("Warn")
                    .setDescription("You was warned on " + guild.getName())
                    .setColor(new Color(255,0,0))
                    .addField("Reason", reason,false)
                    .addField("Date of removal",MessageUtils.discordTimestamp(removeTime,"R"),false)
                    .setTimestamp(OffsetDateTime.now());
        }

        static @NotNull EmbedBuilder punishmentLogEmbed(@NotNull Guild guild,@NotNull String reason,@NotNull User offender, @NotNull User moderator){
            return new EmbedBuilder()
                    .setTitle(String.format("Warn | %d",1 /*SqlUtils.getPunishmentCaseIdFromGuild(guild)*/))
                    .setAuthor(moderator.getAsTag(),null,moderator.getEffectiveAvatarUrl())
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter(guild.getJDA().getSelfUser().getAsTag(),guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .addField("Offender",String.format("%s (%s)",offender.getAsTag(),offender.getId()),false)
                    .addField("Reason",reason,false);
        }

        static @NotNull EmbedBuilder punishmentLogEmbed(@NotNull Guild guild,@NotNull String reason,@NotNull User offender, @NotNull User moderator, @NotNull OffsetDateTime removeTime){
            return new EmbedBuilder()
                    .setTitle(String.format("Warn | %d", 1 /*SqlUtils.getPunishmentCaseIdFromGuild(guild)*/))
                    .setAuthor(moderator.getAsTag(),null,moderator.getEffectiveAvatarUrl())
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter(guild.getJDA().getSelfUser().getAsTag(),guild.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .addField("Offender",String.format("%s (%s)",offender.getAsTag(),offender.getId()),false)
                    .addField("Reason",reason,false)
                    .addField("Expired time",MessageUtils.discordTimestamp(removeTime,"R"),false);
        }
    }

}
