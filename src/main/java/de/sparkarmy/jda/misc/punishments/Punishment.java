package de.sparkarmy.jda.misc.punishments;

import de.sparkarmy.db.DBLogChannel;
import de.sparkarmy.db.DBPunishment;
import de.sparkarmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ResourceBundle;

public abstract class Punishment {
    protected final ResourceBundle userPunishmentBundle;
    protected final ResourceBundle guildPunishmentBundle;
    protected final ResourceBundle standardPhrases;
    protected final User moderator;
    protected final User target;
    protected final Guild guild;
    protected final String reason;
    private final JDA jda;
    private final Color embedColor = new Color(255, 0, 0);

    public Punishment(@NotNull SlashCommandInteractionEvent event) {
        this.userPunishmentBundle = Util.getResourceBundle("PunishmentClazz", event.getUserLocale());
        this.guildPunishmentBundle = Util.getResourceBundle("PunishmentClazz", event.getGuildLocale());
        this.standardPhrases = Util.getResourceBundle("standardPhrases", event.getUserLocale());
        this.guild = event.getGuild();
        this.moderator = event.getUser();
        this.target = event.getOption("target-user", OptionMapping::getAsUser);
        this.reason = event.getOption("reason", OptionMapping::getAsString);
        this.jda = event.getJDA();
    }

    protected long createPunishmentEntry(PunishmentType type) {
        return new DBPunishment(this.guild, this.moderator, this.target, type, this.reason, null, false).createPunishmentEntry();
    }

    protected boolean checkPrecondition(InteractionHook hook) {
        if (this.guild == null) {
            hook.editOriginal(userPunishmentBundle.getString("checkPrecondition.nullChecks.guildIsNull")).queue();
            return true;
        } else if (this.moderator == null) {
            hook.editOriginal(standardPhrases.getString("replies.unexpectedError")).queue();
            return true;
        } else if (this.target == null) {
            hook.editOriginal(userPunishmentBundle.getString("checkPrecondition.nullChecks.targetIsNull")).queue();
            return true;
        } else if (this.target.equals(this.moderator)) {
            hook.editOriginal(userPunishmentBundle.getString("checkPrecondition.targetEqualsModerator")).queue();
            return true;
        }
        return false;
    }

    protected RestAction<Object> checkMemberConditions(InteractionHook hook) {
        return this.guild.retrieveMember(this.target)
                .flatMap(member -> this.guild.retrieveMember(moderator) // Retrieve the moderator as member
                        .map(mod -> {
                            // Checks if moderator-role can interact with the target role
                            if (!member.getRoles().isEmpty() && !mod.canInteract(member.getRoles().getFirst())) {
                                hook.editOriginal(this.userPunishmentBundle.getString("checkPrecondition.targetHaveHigherRole")).queue();
                                return null;
                            }
                            return member;
                        }));
    }

    protected RestAction<Message> createLogMessageRestAction(@NotNull PunishmentType type) {
        String url;
        DBLogChannel log = DBLogChannel.getModLogFromGuild(this.guild);
        if (log == null) {
            log = DBLogChannel.getServerLogFromGuild(this.guild);
            if (log == null) {
                return null;
            }
            url = log.webhookUrl();
            return WebhookClient.createClient(this.jda, url).sendMessage("No modLog is set"); // TODO Integrate ResourceBundle
        }
        url = log.webhookUrl();
        WebhookClient<Message> webhookClient = WebhookClient.createClient(this.jda, url);
        EmbedBuilder logMessageEmbed = new EmbedBuilder();
        logMessageEmbed.setTitle(String.format("%d || %s", DBPunishment.getPunishmentCountForGuild(this.guild) + 1, type.getName()), null);
        logMessageEmbed.addField(
                this.guildPunishmentBundle.getString("preparePunishment.logEmbed.field.offender.name"),
                String.format("%s (%s)", this.target.getEffectiveName(), this.target.getAsMention()),
                false
        );
        logMessageEmbed.addField(
                this.guildPunishmentBundle.getString("preparePunishment.logEmbed.field.moderator.name"),
                String.format("%s (%s)", this.moderator.getEffectiveName(), this.moderator.getAsMention()),
                false
        );
        logMessageEmbed.addField(
                this.guildPunishmentBundle.getString("preparePunishment.logEmbed.field.reason.name"),
                this.reason,
                false
        );
        logMessageEmbed.setAuthor(this.jda.getSelfUser().getName(), null, this.jda.getSelfUser().getEffectiveAvatarUrl());
        logMessageEmbed.setTimestamp(OffsetDateTime.now());
        logMessageEmbed.setColor(this.embedColor);
        logMessageEmbed.setFooter(this.guild.getName(), this.guild.getIconUrl());
        return webhookClient.sendMessageEmbeds(logMessageEmbed.build());
    }

    protected RestAction<Message> createUserLogRestAction(Message message, @NotNull PunishmentType type) {
        EmbedBuilder userEmbedBuilder = new EmbedBuilder();

        userEmbedBuilder.setAuthor(this.jda.getSelfUser().getName(), null, this.jda.getSelfUser().getEffectiveAvatarUrl());
        userEmbedBuilder.setTimestamp(OffsetDateTime.now());
        userEmbedBuilder.setFooter(this.guild.getName(), this.guild.getIconUrl());
        userEmbedBuilder.setColor(this.embedColor);
        userEmbedBuilder.setTitle(type.getName());

        String userEmbedDescriptionString;
        String userEmbedReasonFieldString;
        switch (type) {
            case WARN -> {
                userEmbedDescriptionString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.description.warn");
                userEmbedReasonFieldString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.field.reason.description.warn|mute");
            }
            case MUTE -> {
                userEmbedDescriptionString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.description.mute");
                userEmbedReasonFieldString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.field.reason.description.warn|mute");
            }
            case KICK -> {
                userEmbedDescriptionString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.description.kick");
                userEmbedReasonFieldString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.field.reason.description.kick|ban");
            }
            case BAN -> {
                userEmbedDescriptionString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.description.ban");
                userEmbedReasonFieldString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.field.reason.description.kick|ban");
            }
            case SOFTBAN -> {
                userEmbedDescriptionString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.description.softban");
                userEmbedReasonFieldString = this.userPunishmentBundle.getString("preparePunishment.userEmbed.field.reason.description.softban");
            }
            default -> {
                return message.reply(this.standardPhrases.getString("replies.unexpectedError"));
            }
        }
        userEmbedBuilder.setDescription(String.format(userEmbedDescriptionString, guild.getName()));
        userEmbedBuilder.addField(this.userPunishmentBundle.getString("preparePunishment.userEmbed.field.reason.name"), String.format(userEmbedReasonFieldString, reason), false);


        return this.target.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessageEmbeds(userEmbedBuilder.build()))
                .onErrorFlatMap(ErrorResponse.CANNOT_SEND_TO_USER::test,
                        (error) -> message.reply(this.guildPunishmentBundle.getString("preparePunishment.userHasDmDisabled")));
    }

}
