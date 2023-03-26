package de.SparkArmy.utils.punishments;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.utils.Util;
import de.SparkArmy.utils.customTypes.LogChannelType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class Punishment {

    private final ResourceBundle bundle;
    private final Postgres db;
    private final ConfigController controller;
    private final Guild guild;
    private final Logger logger;


    public Punishment(@NotNull SlashCommandInteractionEvent event, PunishmentType type, @NotNull ConfigController controller) {
        event.deferReply(true).queue();
        this.bundle = Util.getResourceBundle("PunishmentClazz", event.getUserLocale());
        this.db = controller.getMain().getPostgres();
        this.controller = controller;
        this.guild = event.getGuild();
        User target = event.getOption("target-user", OptionMapping::getAsUser);
        Member moderator = event.getMember();
        String reason = event.getOption("reason", OptionMapping::getAsString);
        Integer days = event.getOption("days", 0, OptionMapping::getAsInt);
        InteractionHook hook = event.getHook();
        //noinspection ConstantConditions // Target is a requiered option and all punishment event are guild events
        checkPreconditions(target, moderator, type, reason, days, hook);
        this.logger = controller.getMain().getLogger();
    }

    public Punishment(@NotNull SlashCommandInteractionEvent event, PunishmentType type, @NotNull ConfigController controller, OffsetDateTime duration) {
        this.bundle = Util.getResourceBundle("PunishmentClazz", event.getUserLocale());
        this.db = controller.getMain().getPostgres();
        this.controller = controller;
        this.guild = event.getGuild();
        new TemporaryPunishment();
        this.logger = controller.getMain().getLogger();
    }


    private void executePunishment(@NotNull PunishmentType type, User target, String reason, InteractionHook hook, Integer days) {
        JSONObject guildConfig = controller.getGuildMainConfig(guild);
        if (guildConfig.isNull("punishmentRoles") || guildConfig.getJSONObject("punishmentRoles").isEmpty()) {
            hook.editOriginal(bundle.getString("executePunishment.config.punishmentRolesNotFound")).queue();
            return;
        }
        JSONObject punishmentRoles = guildConfig.getJSONObject("punishmentRoles");

        switch (type) {
            case WARN -> {
                if (punishmentRoles.isNull("warn") || punishmentRoles.getString("warn").isBlank()) {
                    hook.editOriginal(bundle.getString("executePunishment.warn.config.warnIsNull")).queue();
                    return;
                }
                Role role = guild.getRoleById(punishmentRoles.getString("warn"));
                if (role == null) {
                    hook.editOriginal(bundle.getString("executePunishment.warn.role.roleIsNull")).queue();
                    return;
                }
                guild.addRoleToMember(target, role).reason(reason).queue(x -> hook.editOriginal(bundle.getString("executePunishment.warn.successfully")).queue(),
                        new ErrorHandler()
                                .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.warn.addRoleToMember.missingPermissions")).queue())
                                .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.warn.addRoleToMember.unknownMember")).queue())
                                .handle(ErrorResponse.UNKNOWN_ROLE, x -> hook.editOriginal(bundle.getString("executePunishment.warn.role.roleIsNull")).queue()));
            }
            case MUTE -> {
                if (punishmentRoles.isNull("mute") || punishmentRoles.getString("mute").isBlank()) {
                    hook.editOriginal(bundle.getString("executePunishment.mute.config.muteIsNull")).queue();
                    return;
                }
                Role role = guild.getRoleById(punishmentRoles.getString("mute"));
                if (role == null) {
                    hook.editOriginal(bundle.getString("executePunishment.mute.role.roleIsNull")).queue();
                    return;
                }
                guild.addRoleToMember(target, role).reason(reason).queue(x -> hook.editOriginal(bundle.getString("executePunishment.mute.successfully")).queue(),
                        new ErrorHandler()
                                .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.mute.addRoleToMember.missingPermissions")).queue())
                                .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.mute.addRoleToMember.unknownMember")).queue())
                                .handle(ErrorResponse.UNKNOWN_ROLE, x -> hook.editOriginal(bundle.getString("executePunishment.mute.roleIsNull")).queue()));
            }
            case KICK ->
                    guild.kick(target).reason(reason).queue(x -> hook.editOriginal(bundle.getString("executePunishment.kick.successfully")).queue(),
                            new ErrorHandler()
                                    .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.kick.missingPermissions")).queue())
                                    .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.kick.unknownMember")).queue()));

            case BAN ->
                    guild.ban(target, days, TimeUnit.DAYS).reason(reason).queue(x -> hook.editOriginal(bundle.getString("executePunishment.ban.successfully")).queue(),
                            new ErrorHandler()
                                    .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("executePunishment.ban.missingPermissions")).queue())
                                    .handle(ErrorResponse.UNKNOWN_MEMBER, x -> hook.editOriginal(bundle.getString("executePunishment.ban.unknownMember")).queue()));

            default -> hook.editOriginal(bundle.getString("executePunishment.default.reply")).queue();
        }
    }

    private void preparePunishment(User target, Member moderator, String reason, PunishmentType type, Integer days, InteractionHook hook) {

        if (!db.getIsPostgresDisabled()) {
            if (!db.putPunishmentDataInPunishmentTable(target, moderator, type.getId(), reason)) {
                hook.sendMessage(bundle.getString("preparePunishment.putDataInDbFailed")).setEphemeral(true).queue();
                return;
            }
        }
        User selfUser = hook.getJDA().getSelfUser();
        Guild guild = hook.getInteraction().getGuild();

        // Get punishmentNumber from guild
        long punishmentCount = db.getPunishmentCountFromGuild(hook.getInteraction().getGuild());
        if (punishmentCount == -1) punishmentCount = 1; // Fallback if postgres disabled or another error occur

        ResourceBundle guildBundle = Util.getResourceBundle("PunishmentClazz", hook.getInteraction().getGuildLocale());

        // Log Embed
        WebhookEmbedBuilder logEmbed = new WebhookEmbedBuilder();
        logEmbed.setTitle(new WebhookEmbed.EmbedTitle(String.format("%d || %s", punishmentCount, type.getName()), null));
        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                guildBundle.getString("preparePunishment.logEmbed.field.offender.name"),
                String.format("%s (%s)", target.getAsTag(), target.getAsMention())));
        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                guildBundle.getString("preparePunishment.logEmbed.field.moderator.name"),
                String.format("%s (%s)", moderator.getEffectiveName(), moderator.getAsMention())));
        logEmbed.addField(new WebhookEmbed.EmbedField(false,
                guildBundle.getString("preparePunishment.logEmbed.field.reason.name"),
                reason));
        logEmbed.setAuthor(new WebhookEmbed.EmbedAuthor(selfUser.getName(), selfUser.getEffectiveAvatarUrl(), null));
        logEmbed.setTimestamp(OffsetDateTime.now());
        logEmbed.setColor(new Color(255, 0, 0).getRGB());
        //noinspection ConstantConditions // Punishment interaction allways comes from guild
        logEmbed.setFooter(new WebhookEmbed.EmbedFooter(guild.getName(), guild.getIconUrl()));

        prepareSendingModLogEmbed(logEmbed.build());

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
                            hook.sendMessage(bundle.getString("preparePunishemnt.userHasDmDisabled")).setEphemeral(true).queue();
                            executePunishment(type, target, reason, hook, days);
                        }));
    }


    private void prepareSendingModLogEmbed(WebhookEmbed embed) {
        String modLogName = LogChannelType.MOD.getName();
        // Create LogChannelConfig if none exist
        if (controller.getGuildMainConfig(guild).isNull("log-channel")) {
            controller.createLogChannelConfig(guild);
        }

        final JSONObject guildConfig = controller.getGuildMainConfig(guild);
        JSONObject logConfig = guildConfig.getJSONObject("log-channel");

        if (logConfig.isNull("category") || logConfig.getString("category").isBlank()) {
            Util.createLogChannelCategory(guild)
                    .onSuccess(category -> logConfig.put("category", category.getId()))
                    .flatMap(category -> Util.createLogChannel(category, LogChannelType.MOD))
                    .onSuccess(channel -> logConfig.getJSONObject(modLogName).put("channelId", channel.getId()))
                    .flatMap(channel -> channel.createWebhook(guild.getJDA().getSelfUser().getName()))
                    .queue(webhook -> {
                        logConfig.getJSONObject(modLogName).put("webhookUrl", webhook.getUrl());
                        guildConfig.put("log-channel", logConfig);
                        controller.writeInGuildMainConfig(guild, guildConfig);
                        sendModLogEmbed(webhook.getUrl(), embed);
                    });
        } else {
            Category category = guild.getCategoryById(logConfig.getString("category"));
            if (category == null) {
                Util.createLogChannelCategory(guild)
                        .onSuccess(c -> logConfig.put("category", c.getId()))
                        .flatMap(c -> Util.createLogChannel(c, LogChannelType.MOD))
                        .onSuccess(channel -> logConfig.getJSONObject(modLogName).put("channelId", channel.getId()))
                        .flatMap(channel -> channel.createWebhook(guild.getJDA().getSelfUser().getName()))
                        .queue(webhook -> {
                            logConfig.getJSONObject(modLogName).put("webhookUrl", webhook.getUrl());
                            guildConfig.put("log-channel", logConfig);
                            controller.writeInGuildMainConfig(guild, guildConfig);
                            sendModLogEmbed(webhook.getUrl(), embed);
                        });
            } else {
                if (logConfig.getJSONObject(modLogName).isEmpty() || logConfig.getJSONObject(modLogName).getString("channelId").isBlank() || logConfig.getJSONObject(modLogName).getString("webhookUrl").isBlank()) {
                    Util.createLogChannel(category, LogChannelType.MOD)
                            .onSuccess(channel -> logConfig.getJSONObject(modLogName).put("channelId", channel.getId()))
                            .flatMap(channel -> channel.createWebhook(guild.getJDA().getSelfUser().getName()))
                            .queue(webhook -> {
                                logConfig.getJSONObject(modLogName).put("webhookUrl", webhook.getUrl());
                                guildConfig.put("log-channel", logConfig);
                                controller.writeInGuildMainConfig(guild, guildConfig);
                                sendModLogEmbed(webhook.getUrl(), embed);
                            });
                } else {
                    String url = logConfig.getJSONObject(modLogName).getString("webhookUrl");
                    sendModLogEmbed(url, embed);
                }
            }
        }
    }


    private void sendModLogEmbed(String url, WebhookEmbed embed) {
        try {
            WebhookClient.withUrl(url).send(embed);
        } catch (NullPointerException | NumberFormatException ignored) {
        }
    }

    private void checkPreconditions(@NotNull User target, @NotNull Member moderator, @NotNull PunishmentType type, String reason, Integer days, InteractionHook hook) {
        // Conditions to not execute the punishment to yourself, a bot, a member with a higher role, an admin
        moderator.getGuild().retrieveMember(target).onSuccess(x -> {
            if (type.equals(PunishmentType.UNKNOWN) || type.equals(PunishmentType.TIMEOUT) || type.equals(PunishmentType.UNBAN)) {
                hook.editOriginal(bundle.getString("checkPreconditions.falsePunishmentType")).queue();
            } else if (x.equals(moderator)) {
                hook.editOriginal(bundle.getString(String.format("checkPreconditions.%s.targetEqualsModerator", type.getName()))).queue();
            } else if (target.isBot()) {
                hook.editOriginal(bundle.getString(String.format("checkPreconditions.%s.targetIsBot", type.getName()))).queue();
            } else if (!x.getRoles().isEmpty() && !moderator.canInteract(x.getRoles().get(0))) {
                hook.editOriginal(bundle.getString(String.format("checkPreconditions.%s.targetHaveHigherRole", type.getName()))).queue();
            } else if (x.hasPermission(Permission.ADMINISTRATOR)) {
                hook.editOriginal(bundle.getString(String.format("checkPreconditions.%s.targetIsAdmin", type.getName()))).queue();
            } else {
                preparePunishment(target, moderator, reason, type, days, hook);
            }
        }).queue(null, new ErrorHandler()
                .handle(ErrorResponse.UNKNOWN_MEMBER, x -> guild.retrieveBanList().queue(y -> {
                    if (y.stream().noneMatch(z -> z.getUser().equals(target)) && type.equals(PunishmentType.BAN)) {
                        preparePunishment(target, moderator, reason, type, days, hook);
                    } else if (y.stream().allMatch(z -> z.getUser().equals(target)) && type.equals(PunishmentType.BAN)) {
                        hook.editOriginal(bundle.getString("checkPreconditions.userIsBanned")).queue();
                    } else {
                        hook.editOriginal(bundle.getString("checkPreconditions.userIsNotAMember")).queue();
                    }
                }))
                .handle(ErrorResponse.UNKNOWN_USER, x -> hook.editOriginal(bundle.getString("checkPreconditions.retrieveMember.unknownUser")).queue()));

    }

}
