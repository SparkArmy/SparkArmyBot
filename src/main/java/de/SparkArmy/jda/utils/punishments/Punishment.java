package de.SparkArmy.jda.utils.punishments;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.DatabaseAction;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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
import java.util.concurrent.TimeUnit;

public class Punishment {

    private final ResourceBundle bundle;
    private final DatabaseAction db;
    private final ConfigController controller;
    private final Guild guild;
    private final ResourceBundle standardPhrases;


    public Punishment(@NotNull SlashCommandInteractionEvent event, PunishmentType type, @NotNull ConfigController controller) {
        event.deferReply(true).queue();
        this.bundle = Util.getResourceBundle("PunishmentClazz", event.getUserLocale());
        this.standardPhrases = Util.getResourceBundle("standardPhrases", event.getUserLocale());
        this.db = new DatabaseAction();
        this.controller = controller;
        this.guild = event.getGuild();
        User target = event.getOption("target-user", OptionMapping::getAsUser);
        Member moderator = event.getMember();
        String reason = event.getOption("reason", OptionMapping::getAsString);
        Integer days = event.getOption("days", 0, OptionMapping::getAsInt);
        InteractionHook hook = event.getHook();
        checkPreconditions(target, moderator, type, reason, days, hook);
    }

    private void executePunishment(@NotNull PunishmentType type, User target, String reason, InteractionHook hook, Integer days) {

        long warnRoleId = controller.getGuildWarnRole(guild);
        long muteRoleId = controller.getGuildMuteRole(guild);

        if (warnRoleId <= 0 && muteRoleId <= 0) {
            hook.editOriginal(bundle.getString("executePunishment.config.punishmentRolesNotFound")).queue();
            return;
        }

        switch (type) {
            case WARN -> {

                Role role = guild.getRoleById(warnRoleId);
                if (role == null) {
                    hook.editOriginal(bundle.getString("executePunishment.switchType.warn.role.roleIsNull")).queue();
                    return;
                }
                guild.addRoleToMember(target, role).reason(reason).queue(x -> hook.editOriginal(bundle.getString("executePunishment.switchType.warn.successfully")).queue(),
                        new ErrorHandler()
                                .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.warn.addRoleToMember.missingPermissions")).queue())
                                .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.warn.addRoleToMember.unknownMember")).queue())
                                .handle(ErrorResponse.UNKNOWN_ROLE, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.warn.role.roleIsNull")).queue()));
            }
            case MUTE -> {

                Role role = guild.getRoleById(muteRoleId);
                if (role == null) {
                    hook.editOriginal(bundle.getString("executePunishment.switchType.mute.role.roleIsNull")).queue();
                    return;
                }
                guild.addRoleToMember(target, role).reason(reason).queue(x -> hook.editOriginal(bundle.getString("executePunishment.switchType.mute.successfully")).queue(),
                        new ErrorHandler()
                                .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.mute.addRoleToMember.missingPermissions")).queue())
                                .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.mute.addRoleToMember.unknownMember")).queue())
                                .handle(ErrorResponse.UNKNOWN_ROLE, x -> hook.editOriginal(bundle.getString("executePunishment.mute.roleIsNull")).queue()));
            }
            case KICK -> guild.kick(target).reason(reason).queue(x -> hook.editOriginal(bundle.getString("executePunishment.switchType.kick.successfully")).queue(),
                    new ErrorHandler()
                            .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.kick.missingPermissions")).queue())
                            .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.kick.unknownMember")).queue()));

            case BAN -> guild.ban(target, days, TimeUnit.DAYS).reason(reason).queue(x -> hook.editOriginal(bundle.getString("executePunishment.switchType.ban.successfully")).queue(),
                    new ErrorHandler()
                            .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.ban.missingPermissions")).queue())
                            .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.ban.unknownMember")).queue()));

            case SOFTBAN -> guild.ban(target, 1, TimeUnit.DAYS)
                    .reason(reason)
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(x -> guild.unban(target).reason(reason))
                    .queue(x -> hook.editOriginal(bundle.getString("executePunishment.switchType.softban.successfully")).queue(),
                            new ErrorHandler()
                                    .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.softban.missingPermissions")).queue())
                                    .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.switchType.softban.unknownMember")).queue()));
            default -> hook.editOriginal(bundle.getString("executePunishment.switchType.default.reply")).queue();
        }
    }

    private void preparePunishment(@NotNull User target, @NotNull Member moderator, String reason, @NotNull PunishmentType type, Integer days, InteractionHook hook) {
        long value = db.putPunishmentDataInPunishmentTable(target.getIdLong(), moderator.getIdLong(), guild.getIdLong(), type.getId(), reason);
        if (value < 0) {
            hook.sendMessage(String.format(standardPhrases.getString("replies.dbErrorReply"), value)).setEphemeral(true).queue();
            return;
        }

        User selfUser = hook.getJDA().getSelfUser();

        // Get punishmentNumber from guild
        long punishmentCount = db.getPunishmentCountFromGuild(moderator.getGuild().getIdLong());
        if (punishmentCount < 0) punishmentCount = 1; // Fallback if postgres disabled or another error occur

        ResourceBundle guildBundle = Util.getResourceBundle("PunishmentClazz", hook.getInteraction().getGuildLocale());

        // Log Embed
        WebhookEmbedBuilder logEmbed = new WebhookEmbedBuilder();
        logEmbed.setTitle(new WebhookEmbed.EmbedTitle(String.format("%d || %s", punishmentCount + 1, type.getName()), null));
        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                guildBundle.getString("preparePunishment.logEmbed.field.offender.name"),
                String.format("%s (%s)", target.getEffectiveName(), target.getAsMention())));
        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                guildBundle.getString("preparePunishment.logEmbed.field.moderator.name"),
                String.format("%s (%s)", moderator.getEffectiveName(), moderator.getAsMention())));
        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                guildBundle.getString("preparePunishment.logEmbed.field.reason.name"),
                reason));
        logEmbed.setAuthor(new WebhookEmbed.EmbedAuthor(selfUser.getName(), selfUser.getEffectiveAvatarUrl(), null));
        logEmbed.setTimestamp(OffsetDateTime.now());
        logEmbed.setColor(new Color(255, 0, 0).getRGB());
        logEmbed.setFooter(new WebhookEmbed.EmbedFooter(guild.getName(), guild.getIconUrl()));

        WebhookClient client = controller.getMain().getJdaApi().getWebhookApi().getSpecificWebhookClient(guild, LogChannelType.MOD);
        if (client != null) client.send(logEmbed.build());

//        User Embed
        EmbedBuilder userEmbed = new EmbedBuilder();
        userEmbed.setAuthor(selfUser.getName(), null, selfUser.getEffectiveAvatarUrl());
        userEmbed.setTimestamp(OffsetDateTime.now());
        userEmbed.setFooter(guild.getName(), guild.getIconUrl());
        userEmbed.setColor(new Color(255, 0, 0).getRGB());
        userEmbed.setTitle(type.getName());

        String userEmbedDescriptionString;
        String userEmbedReasonFieldString;
        switch (type) {
            case WARN -> {
                userEmbedDescriptionString = bundle.getString("preparePunishment.userEmbed.description.warn");
                userEmbedReasonFieldString = bundle.getString("preparePunishment.userEmbed.field.reason.description.warn|mute");
            }
            case MUTE -> {
                userEmbedDescriptionString = bundle.getString("preparePunishment.userEmbed.description.mute");
                userEmbedReasonFieldString = bundle.getString("preparePunishment.userEmbed.field.reason.description.warn|mute");
            }
            case KICK -> {
                userEmbedDescriptionString = bundle.getString("preparePunishment.userEmbed.description.kick");
                userEmbedReasonFieldString = bundle.getString("preparePunishment.userEmbed.field.reason.description.kick|ban");
            }
            case BAN -> {
                userEmbedDescriptionString = bundle.getString("preparePunishment.userEmbed.description.ban");
                userEmbedReasonFieldString = bundle.getString("preparePunishment.userEmbed.field.reason.description.kick|ban");
            }
            case SOFTBAN -> {
                userEmbedDescriptionString = bundle.getString("preparePunishment.userEmbed.description.softban");
                userEmbedReasonFieldString = bundle.getString("preparePunishment.userEmbed.field.reason.description.softban");
            }
            default -> {
                hook.editOriginal(bundle.getString("preparePunishment.unexpectedError")).queue();
                return;
            }
        }
        userEmbed.setDescription(String.format(userEmbedDescriptionString, guild.getName()));
        userEmbed.addField(bundle.getString("preparePunishment.userEmbed.field.reason.name"), String.format(userEmbedReasonFieldString, reason), false);

        target.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(userEmbed.build()))
                .queue(x -> executePunishment(type, target, reason, hook, days), new ErrorHandler()
                        .ignore(ErrorResponse.UNKNOWN_CHANNEL)
                        .handle(ErrorResponse.CANNOT_SEND_TO_USER, x -> {
                            hook.sendMessage(bundle.getString("preparePunishment.userHasDmDisabled")).setEphemeral(true).queue();
                            executePunishment(type, target, reason, hook, days);
                        }));
    }

    private void checkPreconditions(User target, Member moderator, @NotNull PunishmentType type, String reason, Integer days, InteractionHook hook) {

        if (guild == null || moderator == null) {
            hook.editOriginal(bundle.getString("checkPreconditions.nullChecks.guildIsNull")).queue();
            return;
        }

        if (target == null) {
            hook.editOriginal(bundle.getString("checkPreconditions.nullChecks.targetIsNull")).queue();
            return;
        }


        // Conditions to not execute the punishment to yourself, a bot, a member with a higher role, an admin
        guild.retrieveMember(target).queue(x -> {
            if (type.equals(PunishmentType.UNKNOWN) || type.equals(PunishmentType.TIMEOUT) || type.equals(PunishmentType.UNBAN)) {
                hook.editOriginal(bundle.getString("checkPreconditions.retrieveMember.falsePunishmentType")).queue();
            } else if (x.equals(moderator)) {
                hook.editOriginal(bundle.getString(String.format("checkPreconditions.retrieveMember.%s.targetEqualsModerator", type.getName()))).queue();
            } else if (target.isBot()) {
                hook.editOriginal(bundle.getString(String.format("checkPreconditions.retrieveMember.%s.targetIsBot", type.getName()))).queue();
            } else if (!x.getRoles().isEmpty() && !moderator.canInteract(x.getRoles().getFirst())) {
                hook.editOriginal(bundle.getString(String.format("checkPreconditions.retrieveMember.%s.targetHaveHigherRole", type.getName()))).queue();
            } else if (x.hasPermission(Permission.ADMINISTRATOR)) {
                hook.editOriginal(bundle.getString(String.format("checkPreconditions.retrieveMember.%s.targetIsAdmin", type.getName()))).queue();
            } else {
                preparePunishment(target, moderator, reason, type, days, hook);
            }
        }, new ErrorHandler()
                .handle(ErrorResponse.UNKNOWN_MEMBER, x -> guild.retrieveBanList().queue(y -> {
                    if (y.stream().noneMatch(z -> z.getUser().equals(target)) && type.equals(PunishmentType.BAN)) {
                        preparePunishment(target, moderator, reason, type, days, hook);
                    } else if (y.stream().allMatch(z -> z.getUser().equals(target)) && type.equals(PunishmentType.BAN)) {
                        hook.editOriginal(bundle.getString("checkPreconditions.retrieveMember.errorHandler.userIsBanned")).queue();
                    } else {
                        hook.editOriginal(bundle.getString("checkPreconditions.retrieveMember.errorHandler.userIsNotAMember")).queue();
                    }
                }))
                .handle(ErrorResponse.UNKNOWN_USER, x -> hook.editOriginal(bundle.getString("checkPreconditions.retrieveMember.errorHandler.unknownUser")).queue()));
    }

}
