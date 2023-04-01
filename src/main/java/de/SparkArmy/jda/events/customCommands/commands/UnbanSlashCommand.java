package de.SparkArmy.jda.events.customCommands.commands;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
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

public class UnbanSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "unban";
    }


    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, @NotNull ConfigController controller) {
        event.deferReply(true).queue();
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());
        InteractionHook hook = event.getHook();
        User target = event.getOption("target-user", OptionMapping::getAsUser);
        String reason = event.getOption("reason", OptionMapping::getAsString); // Option is required
        Member moderator = event.getMember();

        //noinspection ConstantConditions
        event.getGuild().retrieveBanList().queue(banList -> {
            if (banList.stream().noneMatch(x -> x.getUser().equals(target))) {
                hook.editOriginal(bundle.getString("command.userIsNotInBanList")).queue();
                return;
            }
            Postgres db = controller.getMain().getPostgres();
            if (!db.getIsPostgresEnabled()) {

                event.getGuild().unban(target).reason(reason).queue(x -> {
                            if (!controller.getMain().getPostgres().putPunishmentDataInPunishmentTable(target, moderator, PunishmentType.UNBAN.getId(), reason)) {
                                hook.editOriginal(bundle.getString("command.putInDB.unban.successfully.errorDB")).queue();
                            } else {
                                hook.editOriginal(bundle.getString("command.putInDB.unban.successfully")).queue();
                            }

                            ResourceBundle guildBundle = Util.getResourceBundle(getName(), event.getGuildLocale());
                            User selfUser = event.getJDA().getSelfUser();

                            // Get punishmentNumber from guild
                            long punishmentCount = db.getPunishmentCountFromGuild(hook.getInteraction().getGuild());
                            if (punishmentCount == -1)
                                punishmentCount = 1; // Fallback if postgres disabled or another error occur

                            WebhookEmbedBuilder logEmbed = new WebhookEmbedBuilder();
                            logEmbed.setTitle(new WebhookEmbed.EmbedTitle(String.format("%d || %s", punishmentCount, getName()), null));
                            logEmbed.addField(new WebhookEmbed.EmbedField(false,
                                    guildBundle.getString("command.logEmbed.field.offender.name"),
                                    String.format("%s (%s)", target.getAsTag(), target.getAsMention())));
                            //noinspection ConstantConditions // Punishment events are all guild events
                            logEmbed.addField(new WebhookEmbed.EmbedField(false,
                                    guildBundle.getString("command.logEmbed.field.moderator.name"),
                                    String.format("%s (%s)", moderator.getEffectiveName(), moderator.getAsMention())));
                            //noinspection ConstantConditions // Reason is a requiered option
                            logEmbed.addField(new WebhookEmbed.EmbedField(false,
                                    guildBundle.getString("command.logEmbed.field.reason.name"),
                                    reason));
                            logEmbed.setAuthor(new WebhookEmbed.EmbedAuthor(selfUser.getName(), selfUser.getEffectiveAvatarUrl(), null));
                            logEmbed.setTimestamp(OffsetDateTime.now());
                            logEmbed.setColor(new Color(255, 0, 0).getRGB());
                            logEmbed.setFooter(new WebhookEmbed.EmbedFooter(event.getGuild().getName(), event.getGuild().getIconUrl()));

                            Util.prepareSendingModLogEmbed(logEmbed.build(), event.getGuild());
                        },
                        new ErrorHandler()
                                .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("command.putInDB.unban.error.missingPermissions")).queue())
                                .handle(ErrorResponse.UNKNOWN_USER, x -> hook.editOriginal(bundle.getString("command.putInDB.unban.error.unknownUser")).queue()));
            } else {
                hook.editOriginal(bundle.getString("command.putInDB.failed")).queue();
            }
        }, new ErrorHandler()
                .handle(ErrorResponse.MISSING_PERMISSIONS, x -> hook.editOriginal(bundle.getString("command.missingPermissionToGetBanList")).queue()));
    }
}
