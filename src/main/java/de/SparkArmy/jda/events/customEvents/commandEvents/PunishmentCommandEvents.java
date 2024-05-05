package de.SparkArmy.jda.events.customEvents.commandEvents;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.DatabaseAction;
import de.SparkArmy.jda.annotations.events.JDASlashCommandInteractionEvent;
import de.SparkArmy.jda.annotations.internal.JDAEvent;
import de.SparkArmy.jda.events.EventManager;
import de.SparkArmy.jda.events.iEvent.IJDAEvent;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.jda.utils.punishments.Punishment;
import de.SparkArmy.jda.utils.punishments.PunishmentType;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.Guild;
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

public class PunishmentCommandEvents implements IJDAEvent {

    private final ConfigController controller;

    public PunishmentCommandEvents(@NotNull EventManager manager) {
        this.controller = manager.getController();
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "ban")
    public void banSlashCommand(SlashCommandInteractionEvent event, Guild guild) {
        new Punishment(event, PunishmentType.BAN, controller);
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "kick")
    public void kickSlashCommand(SlashCommandInteractionEvent event, Guild guild) {
        new Punishment(event, PunishmentType.KICK, controller);
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "mute")
    public void muteSlashCommand(SlashCommandInteractionEvent event, Guild guild) {
        new Punishment(event, PunishmentType.MUTE, controller);
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "softban")
    public void softbanSlashCommand(SlashCommandInteractionEvent event, Guild guild) {
        new Punishment(event, PunishmentType.SOFTBAN, controller);
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "warn")
    public void warnSlashCommand(SlashCommandInteractionEvent event, Guild guild) {
        new Punishment(event, PunishmentType.WARN, controller);
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "unban")
    public void unbanSlashCommand(@NotNull SlashCommandInteractionEvent event, Guild guild) {
        event.deferReply(true).queue();
        ResourceBundle bundle = Util.getResourceBundle("unban", event.getUserLocale());
        ResourceBundle standardPhrases = Util.getResourceBundle("standardPhrases", event.getUserLocale());
        InteractionHook hook = event.getHook();
        User target = event.getOption("target-user", OptionMapping::getAsUser);
        String reason = event.getOption("reason", OptionMapping::getAsString); // Option is required
        Member moderator = event.getMember();
        if (moderator == null) return;
        if (reason == null) return;
        if (guild == null) return;

        guild.retrieveBanList().queue(banList -> {
            if (banList.stream().noneMatch(x -> x.getUser().equals(target))) {
                hook.editOriginal(bundle.getString("command.userIsNotInBanList")).queue();
                return;
            }
            DatabaseAction db = new DatabaseAction();

            guild.unban(target).reason(reason).queue(x -> {
                        long value = db.putPunishmentDataInPunishmentTable(target.getIdLong(), moderator.getIdLong(), guild.getIdLong(), PunishmentType.UNBAN.getId(), reason);
                        if (value < 0) {
                            hook.editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), value)).queue();
                        } else if (value > 0) {
                            hook.editOriginal(bundle.getString("command.putInDB.unban.successfully")).queue();
                        } else {
                            hook.editOriginal(standardPhrases.getString("replies.noDataEdit")).queue();
                        }

                        ResourceBundle guildBundle = Util.getResourceBundle("unban", event.getGuildLocale());
                        User selfUser = event.getJDA().getSelfUser();

                        // Get punishmentNumber from the Guild
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
                        logEmbed.setFooter(new WebhookEmbed.EmbedFooter(guild.getName(), guild.getIconUrl()));

                        WebhookClient client = controller.getMain().getJdaApi().getWebhookApi().getSpecificWebhookClient(event.getGuild(), LogChannelType.MOD);
                        if (client == null) return;
                        client.send(logEmbed.build());
                    },
                    new ErrorHandler()
                            .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("command.putInDB.unban.error.missingPermissions")).queue())
                            .handle(ErrorResponse.UNKNOWN_USER, x -> hook.editOriginal(bundle.getString("command.putInDB.unban.error.unknownUser")).queue()));

        }, new ErrorHandler()
                .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("command.missingPermissionToGetBanList")).queue()));

    }

    @Override
    public Class<?> getEventClass() {
        return this.getClass();
    }
}
