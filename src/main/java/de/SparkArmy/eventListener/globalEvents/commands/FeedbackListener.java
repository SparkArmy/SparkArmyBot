package de.SparkArmy.eventListener.globalEvents.commands;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.jda.ChannelUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class FeedbackListener extends CustomEventListener {
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        if (!modalId.startsWith("feedback")) return;
        if (!modalId.contains(";")) return;
        String userId = event.getUser().getId();
        if (!userId.equals(modalId.split(";")[1])) return;

        ModalMapping topic = event.getValue("topic");
        ModalMapping text = event.getValue("text");

        if (topic == null || text == null) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        String title = String.format("Feedback to: %s", topic.getAsString());
        String description = text.getAsString();

        Color color = new Color(0x45AD1A);

        EmbedBuilder feedbackEmbed = new EmbedBuilder();
        feedbackEmbed.setTitle(title);
        feedbackEmbed.setDescription(description);
        feedbackEmbed.setColor(color);

        String buttonFormat = "%s;%s";
        Collection<Button> buttons = new ArrayList<>() {{
            add(Button.danger(String.format(buttonFormat, "exitFeedback", userId), "Exit"));
            add(Button.primary(String.format(buttonFormat, "editFeedback", userId), "Edit"));
            add(Button.success(String.format(buttonFormat, "sendFeedback", userId), "Send"));
        }};

        event.replyEmbeds(feedbackEmbed.build()).addActionRow(buttons).setEphemeral(true).queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        if (!buttonId.contains(";")) return;
        if (!buttonId.split(";")[0].contains("Feedback")) return;
        String userId = buttonId.split(";")[1];
        if (!userId.equals(event.getUser().getId())) return;
        String action = buttonId.split(";")[0].replace("Feedback", "");
        switch (action) {
            case "exit" -> exitFeedback(event);
            case "edit" -> editFeedback(event);
            case "send" -> sendFeedback(event);
        }
    }

    private void sendFeedback(@NotNull ButtonInteractionEvent event) {
        MessageEmbed embed = event.getMessage().getEmbeds().get(0);
        if (event.getGuild() != null) {
            Guild guild = event.getGuild();
            MessageChannel channel = getFeedbackChannel(guild);
            event.editMessage("Your ticket was received").queue(a -> {
                a.editOriginalComponents().queue();
                a.editOriginalEmbeds().queue();
            });
            channel.sendMessageEmbeds(embed).queue();
        } else {
            SelectMenu.Builder guildSelect = SelectMenu.create("feedbackGuildSelect");
            jda.getGuilds().forEach(x -> {
                if (storageServer.equals(x)) return;
                guildSelect.addOption(x.getName(), x.getId());
            });

            if (guildSelect.getOptions().isEmpty()) {
                event.editMessage("Ups I'm not on any server").queue(a -> {
                    a.editOriginalComponents().queue();
                    a.editOriginalEmbeds().queue();
                });
            }
            event.editComponents().setActionRow(guildSelect.build()).queue(x ->
                    waiter.waitForEvent(SelectMenuInteractionEvent.class, e -> e.getUser().equals(event.getUser()) && e.getComponentId().equals("feedbackGuildSelect"), e -> {
                        Guild guild = jda.getGuildById(e.getValues().get(0));
                        MessageChannel channel = getFeedbackChannel(guild);
                        e.editMessage("Will you add attachments?").setComponents(
                                        ActionRow.of(
                                                Button.primary("feedbackAttachmentYes", "Yes"),
                                                Button.primary("feebackAttachmentsNo", "No")))
                                .queue(y -> {
                                    y.editOriginalEmbeds().queue();
                                    waiter.waitForEvent(ButtonInteractionEvent.class, f -> f.getUser().equals(event.getUser()) && (f.getComponentId().equals("feedbackAttachmentYes") || f.getComponentId().equals("feebackAttachmentsNo"))
                                            , f -> {
                                                String id = f.getComponentId();
                                                if (id.equals("feebackAttachmentsNo")) {
                                                    channel.sendMessageEmbeds(embed).queue();
                                                    f.editMessage("Your ticket was received").queue(a -> {
                                                        a.editOriginalComponents().queue();
                                                        a.editOriginalEmbeds().queue();
                                                    });
                                                } else {
                                                    f.editMessage("Click the button below after you send attachments").setActionRow(Button.success("feedbackAttachmentsOk", "Ok")).queue(
                                                            z -> waiter.waitForEvent(ButtonInteractionEvent.class, g -> g.getComponentId().equals("feedbackAttachmentsOk") && g.getUser().equals(event.getUser()), g -> {
                                                                StringBuilder links = new StringBuilder();
                                                                g.getChannel().getHistory().retrievePast(10).complete().forEach(message -> {
                                                                    if (message.getTimeCreated().isBefore(event.getTimeCreated())) return;
                                                                    if (message.getAttachments().isEmpty()) return;
                                                                    message.getAttachments().forEach(a->links.append(a.getUrl()).append("\n"));
                                                                });
                                                                channel.sendMessageEmbeds(embed).queue();
                                                                if (!links.isEmpty()) channel.sendMessage(links).queue();

                                                                g.editMessage("Your ticket was received").queue(a -> {
                                                                    a.editOriginalEmbeds().queue();
                                                                    a.editOriginalComponents().queue();
                                                                });

                                                            }, 2, TimeUnit.MINUTES, () -> {
                                                                channel.sendMessageEmbeds(embed).queue();
                                                                z.editOriginal("Your ticket was received").queue(a -> {
                                                                    a.editMessageComponents().queue();
                                                                    a.editMessageEmbeds().queue();
                                                                });
                                                            })
                                                    );
                                                }
                                            }, 5, TimeUnit.MINUTES, () -> {
                                                channel.sendMessageEmbeds(embed).queue();
                                                y.editOriginal("Your ticket was received").queue(a -> {
                                                    a.editMessageComponents().queue();
                                                    a.editMessageEmbeds().queue();
                                                });
                                            });
                                });
                    }, 5, TimeUnit.MINUTES, () -> exitFeedback(event)));
        }
    }

    private void exitFeedback(@NotNull ButtonInteractionEvent event) {
        event.editMessage("Your feedback message has been withdrawn").queue(x -> {
            x.editOriginalEmbeds().queue();
            x.editOriginalComponents().queue();
        });
    }

    private void editFeedback(@NotNull ButtonInteractionEvent event) {
        MessageEmbed feedbackEmbed = event.getMessage().getEmbeds().get(0);

        //noinspection ConstantConditions
        String topicText = feedbackEmbed.getTitle().replace("Feedback to: ", "");
        String textText = feedbackEmbed.getDescription();

        String placeholder = """
                In this field you can write all what you wish.
                "The content will be send to the moderation-team.
                """;

        TextInput.Builder topic = TextInput.create("topic", "Topic", TextInputStyle.SHORT)
                .setPlaceholder("Your topic, mostly your feedback-category")
                .setMaxLength(241)
                .setValue(topicText)
                .setRequired(true);

        TextInput text = TextInput.create("text", "Text", TextInputStyle.PARAGRAPH)
                .setPlaceholder(placeholder)
                .setValue(textText)
                .build();

        String modalId = String.format("feedback;%s", event.getUser().getId());

        Modal feedbackModal = Modal.create(modalId, "Feedback").addActionRows(
                ActionRow.of(topic.build()),
                ActionRow.of(text)
        ).build();

        event.getHook().editOriginalComponents().queue();
        event.replyModal(feedbackModal).queue();
    }

    private @NotNull MessageChannel getFeedbackChannel(Guild guild) {
        JSONObject config = getGuildMainConfig(guild);

        Collection<Permission> denied = new ArrayList<>() {{
            add(Permission.VIEW_CHANNEL);
        }};
        long publicRoleId = guild.getPublicRole().getIdLong();

        if (config.isNull("feedback")) {
            JSONObject feedback = new JSONObject();

            Category category = ChannelUtil.createCategory(guild, "Feedback");
            category.getManager().putRolePermissionOverride(publicRoleId, null, denied).queue();

            TextChannel channel = ChannelUtil.createTextChannel(category, "user-feedback");
            channel.getManager().putRolePermissionOverride(publicRoleId, null, denied).queue();

            feedback.put("category", category.getId());
            feedback.put("channel", channel.getId());
            config.put("feedback", feedback);
            writeInGuildMainConfig(guild, config);

            return channel;
        }

        JSONObject feedback = config.getJSONObject("feedback");
        if (feedback.isEmpty() || feedback.isNull("category") || feedback.isNull("channel")) {
            Category category = ChannelUtil.createCategory(guild, "Feedback");
            category.getManager().putRolePermissionOverride(publicRoleId, null, denied).queue();

            TextChannel channel = ChannelUtil.createTextChannel(category, "user-feedback");
            channel.getManager().putRolePermissionOverride(publicRoleId, null, denied).queue();

            feedback.put("category", category.getId());
            feedback.put("channel", channel.getId());
            config.put("feedback", feedback);
            writeInGuildMainConfig(guild, config);

            return channel;
        }

        Channel targetChannel = guild.getGuildChannelById(feedback.getString("channel"));
        if (targetChannel == null) {
            Category category = guild.getCategoryById(feedback.getString("category"));
            if (category == null) {
                category = ChannelUtil.createCategory(guild, "Feedback");
                category.getManager().putRolePermissionOverride(publicRoleId, null, denied).queue();

                TextChannel channel = ChannelUtil.createTextChannel(category, "user-feedback");
                channel.getManager().putRolePermissionOverride(publicRoleId, null, denied).queue();

                feedback.put("category", category.getId());
                feedback.put("channel", channel.getId());
                config.put("feedback", feedback);
                writeInGuildMainConfig(guild, config);

                return channel;
            }

            TextChannel channel = ChannelUtil.createTextChannel(category, "user-feedback");

            feedback.put("channel", channel.getId());
            config.put("feedback", feedback);
            writeInGuildMainConfig(guild, config);

            return channel;
        }

        MessageChannel messagechannel = ChannelUtil.rightChannel(targetChannel);
        if (messagechannel == null) {
            Category category = guild.getCategoryById(feedback.getString("category"));
            if (category == null) {
                category = ChannelUtil.createCategory(guild, "Feedback");
                category.getManager().putRolePermissionOverride(publicRoleId, null, denied).queue();

                TextChannel channel = ChannelUtil.createTextChannel(category, "user-feedback");
                channel.getManager().putRolePermissionOverride(publicRoleId, null, denied).queue();

                feedback.put("category", category.getId());
                feedback.put("channel", channel.getId());
                config.put("feedback", feedback);
                writeInGuildMainConfig(guild, config);

                return channel;
            }

            TextChannel channel = ChannelUtil.createTextChannel(category, "user-feedback");

            feedback.put("channel", channel.getId());
            config.put("feedback", feedback);
            writeInGuildMainConfig(guild, config);

            return channel;
        }

        return messagechannel;
    }
}
