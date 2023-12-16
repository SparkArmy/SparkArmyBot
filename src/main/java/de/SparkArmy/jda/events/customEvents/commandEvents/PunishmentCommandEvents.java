package de.SparkArmy.jda.events.customEvents.commandEvents;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.DatabaseAction;
import de.SparkArmy.jda.events.annotations.interactions.JDASlashCommand;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.jda.utils.punishments.Punishment;
import de.SparkArmy.jda.utils.punishments.PunishmentType;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ResourceBundle;

public class PunishmentCommandEvents {

    private final ConfigController controller;

    public PunishmentCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
    }

    @JDASlashCommand(name = "ban")
    public void banSlashCommand(SlashCommandInteractionEvent event) {
        new Punishment(event, PunishmentType.BAN, controller);
    }

    @JDASlashCommand(name = "kick")
    public void kickSlashCommand(SlashCommandInteractionEvent event) {
        new Punishment(event, PunishmentType.KICK, controller);
    }

    @JDASlashCommand(name = "mute")
    public void muteSlashCommand(SlashCommandInteractionEvent event) {
        new Punishment(event, PunishmentType.MUTE, controller);
    }

    @JDASlashCommand(name = "softban")
    public void softbanSlashCommand(SlashCommandInteractionEvent event) {
        new Punishment(event, PunishmentType.SOFTBAN, controller);
    }

    @JDASlashCommand(name = "warn")
    public void warnSlashCommand(SlashCommandInteractionEvent event) {
        new Punishment(event, PunishmentType.WARN, controller);
    }

    @JDASlashCommand(name = "unban")
    public void unbanSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        ResourceBundle bundle = Util.getResourceBundle("unban", event.getUserLocale());
        InteractionHook hook = event.getHook();
        User target = event.getOption("target-user", OptionMapping::getAsUser);
        String reason = event.getOption("reason", OptionMapping::getAsString); // Option is required
        Member moderator = event.getMember();
        if (moderator == null) return;
        if (reason == null) return;

        //noinspection ConstantConditions
        event.getGuild().retrieveBanList().queue(banList -> {
            if (banList.stream().noneMatch(x -> x.getUser().equals(target))) {
                hook.editOriginal(bundle.getString("command.userIsNotInBanList")).queue();
                return;
            }
            DatabaseAction db = new DatabaseAction();

            event.getGuild().unban(target).reason(reason).queue(x -> {
                        if (db.putPunishmentDataInPunishmentTable(target.getIdLong(), moderator.getIdLong(), event.getGuild().getIdLong(), PunishmentType.UNBAN.getId(), reason) <= 0) { // TODO Add error handling
                            hook.editOriginal(bundle.getString("command.putInDB.unban.successfully.errorDB")).queue();
                        } else {
                            hook.editOriginal(bundle.getString("command.putInDB.unban.successfully")).queue();
                        }

                        ResourceBundle guildBundle = Util.getResourceBundle("unban", event.getGuildLocale());
                        User selfUser = event.getJDA().getSelfUser();

                        // Get punishmentNumber from guild
                        long punishmentCount = db.getPunishmentCountFromGuild(moderator.getGuild().getIdLong());
                        if (punishmentCount == -1)
                            punishmentCount = 1; // Fallback if postgres disabled or another error occur

                        WebhookEmbedBuilder logEmbed = new WebhookEmbedBuilder();
                        logEmbed.setTitle(new WebhookEmbed.EmbedTitle(String.format("%d || %s", punishmentCount, "unban"), null));
                        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                                guildBundle.getString("command.logEmbed.field.offender.name"),
                                String.format("%s (%s)", target.getEffectiveName(), target.getAsMention())));
                        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                                guildBundle.getString("command.logEmbed.field.moderator.name"),
                                String.format("%s (%s)", moderator.getEffectiveName(), moderator.getAsMention())));
                        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                                guildBundle.getString("command.logEmbed.field.reason.name"),
                                reason));
                        logEmbed.setAuthor(new WebhookEmbed.EmbedAuthor(selfUser.getName(), selfUser.getEffectiveAvatarUrl(), null));
                        logEmbed.setTimestamp(OffsetDateTime.now());
                        logEmbed.setColor(new Color(255, 0, 0).getRGB());
                        logEmbed.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName(), event.getGuild().getIconUrl()));

                        // TODO see Util
//                            Util.sendingModLogEmbed(logEmbed.build(), event.getGuild());
                    },
                    new ErrorHandler()
                            .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("command.putInDB.unban.error.missingPermissions")).queue())
                            .handle(ErrorResponse.UNKNOWN_USER, x -> hook.editOriginal(bundle.getString("command.putInDB.unban.error.unknownUser")).queue()));

        }, new ErrorHandler()
                .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("command.missingPermissionToGetBanList")).queue()));

    }
}
