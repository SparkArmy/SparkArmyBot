package de.SparkArmy.jda.events.customEvents.commandEvents;

import club.minnced.discord.webhook.WebhookClient;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.annotations.events.JDAModalInteractionEvent;
import de.SparkArmy.jda.annotations.events.JDASlashCommandInteractionEvent;
import de.SparkArmy.jda.events.EventManager;
import de.SparkArmy.jda.events.iEvent.IJDAEvent;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ResourceBundle;

public class FeedbackSlashCommandEvents implements IJDAEvent {

    private final ConfigController controller;

    private ResourceBundle bundle(DiscordLocale locale) {
        return Util.getResourceBundle("feedback", locale);
    }

    public FeedbackSlashCommandEvents(EventManager manager) {
        this.controller = manager.getController();
    }

    @JDASlashCommandInteractionEvent(name = "feedback")
    public void feedbackInitialSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        if (checkForChannel(guild) == null) {
            event.reply(bundle.getString("feedbackEvents.checkForChannel.featureNotActivated"))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        TextInput.Builder thema = TextInput.create(
                "feedbackThema",
                bundle.getString("feedbackEvents.feedbackInitialSlashCommand.feedbackModal.thema.lable"),
                TextInputStyle.SHORT);
        thema.setRequiredRange(1, 200);
        thema.setPlaceholder(bundle.getString("feedbackEvents.feedbackInitialSlashCommand.feedbackModal.thema.placeholder"));
        thema.setRequired(true);

        TextInput.Builder feedbackText = TextInput.create(
                "feedbackText",
                bundle.getString("feedbackEvents.feedbackInitialSlashCommand.feedbackModal.feedbackText.lable"),
                TextInputStyle.PARAGRAPH);
        feedbackText.setRequiredRange(10, 2000);
        feedbackText.setPlaceholder(bundle.getString("feedbackEvents.feedbackInitialSlashCommand.feedbackModal.feedbackText.placeholder"));
        feedbackText.setRequired(true);

        Modal.Builder feedbackModal = Modal.create(
                String.format("feedbackModalEvents_SendFeedbackModal;%s", event.getUser().getId()),
                bundle.getString("feedbackEvents.feedbackInitialSlashCommand.feedbackModal.title"));
        feedbackModal.addActionRow(thema.build());
        feedbackModal.addActionRow(feedbackText.build());

        event.replyModal(feedbackModal.build()).queue();
    }

    @JDAModalInteractionEvent(startWith = "feedbackModalEvents_SendFeedbackModal")
    public void feedbackSendModalEvent(@NotNull ModalInteractionEvent event) {
        event.deferReply(true).queue();
        ModalMapping themaMapping = event.getValue("feedbackThema");
        ModalMapping textMapping = event.getValue("feedbackText");

        if (themaMapping == null || textMapping == null) return;

        String thema = themaMapping.getAsString();
        String text = textMapping.getAsString();

        ResourceBundle bundle = bundle(event.getGuildLocale());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format(bundle.getString("feedbackEvents.feedbackSendModalEvent.embed.title"), thema));
        embed.setDescription(text);
        embed.setAuthor(event.getUser().getEffectiveName(), null, event.getUser().getEffectiveAvatarUrl());

        MessageChannel channel = (MessageChannel) checkForChannel(event.getGuild());
        if (channel == null) {
            event.getHook().editOriginal(bundle.getString("feedbackEvents.checkForChannel.featureNotActivated"))
                    .setEmbeds()
                    .queue();
            return;
        }

        event.getHook()
                .editOriginal(bundle.getString("feedbackEvents.feedbackInitialSlashCommand.messageSend"))
                .setEmbeds()
                .flatMap(x -> channel.sendMessageEmbeds(embed.build()))
                .queue();
    }

    private @Nullable Channel checkForChannel(Guild guild) {
        long channelId = controller.getGuildFeedbackChannel(guild);

        if (channelId == 0) {
            return null;
        }
        WebhookClient client = controller.getMain().getJdaApi().getWebhookApi().getSpecificWebhookClient(guild, LogChannelType.SERVER);
        if (client != null) {
            client.send("No feedback channel set");
        }
        return guild.getGuildChannelById(channelId);
    }

    @Override
    public Class<?> getEventClass() {
        return this.getClass();
    }
}
