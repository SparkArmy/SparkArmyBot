package de.sparkarmy.jda.events.commands;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.sparkarmy.config.ConfigController;
import de.sparkarmy.db.DBPunishment;
import de.sparkarmy.jda.EventManager;
import de.sparkarmy.jda.annotations.events.JDASlashCommandInteractionEvent;
import de.sparkarmy.jda.annotations.internal.JDAEvent;
import de.sparkarmy.jda.events.IJDAEvent;
import de.sparkarmy.jda.misc.LogChannelType;
import de.sparkarmy.jda.misc.punishments.*;
import de.sparkarmy.utils.Util;
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
    public void banSlashCommand(SlashCommandInteractionEvent event) {
        new Ban(event).createBan();
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "kick")
    public void kickSlashCommand(SlashCommandInteractionEvent event) {
        new Kick(event).createKick();
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "mute")
    public void muteSlashCommand(SlashCommandInteractionEvent event) {
        new Mute(event).createMute();
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "softban")
    public void softbanSlashCommand(SlashCommandInteractionEvent event) {
        new Softban(event).createSoftban();
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "warn")
    public void warnSlashCommand(SlashCommandInteractionEvent event) {
        new Warn(event).createWarn();
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "unban")
    public void unbanSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        Guild guild = event.getGuild();
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

            guild.unban(target).reason(reason).queue(x -> {
                        long value = new DBPunishment(guild, moderator.getUser(), target, PunishmentType.UNBAN, reason, null, false).createPunishmentEntry();
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
                        long punishmentCount = DBPunishment.getPunishmentCountForGuild(guild);
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

                        WebhookClient client = controller.main().getJdaApi().getWebhookApi().getSpecificWebhookClient(event.getGuild(), LogChannelType.MOD);
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
