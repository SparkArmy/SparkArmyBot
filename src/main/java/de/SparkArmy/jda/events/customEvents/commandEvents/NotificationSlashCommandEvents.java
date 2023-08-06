package de.SparkArmy.jda.events.customEvents.commandEvents;

import com.github.twitch4j.helix.domain.User;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.events.annotations.interactions.*;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.twitch.TwitchApi;
import de.SparkArmy.utils.NotificationService;
import de.SparkArmy.utils.Util;
import de.SparkArmy.youtube.YouTubeApi;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.*;

public class NotificationSlashCommandEvents {

    private final ConfigController controller;
    private final Postgres db;

    private final Color notificationEmbedColor = new Color(0x941D9E);


    public NotificationSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
        this.db = controller.getMain().getPostgres();
    }

    final ResourceBundle bundle(DiscordLocale locale) {
        return Util.getResourceBundle("notification", locale);
    }

    private final List<String> values = Arrays.stream(NotificationService.values()).toList().stream().map(NotificationService::getServiceName).toList();

    @JDAAutoComplete(commandName = "notification")
    public void notificationPlatformAutocomplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        event.replyChoiceStrings(values).queue();
    }

    @JDASlashCommand(name = "notification")
    public void notificationInitialSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = bundle(event.getUserLocale());
        String service = event.getOption("platform", OptionMapping::getAsString);
        if (!event.isFromGuild()) return;
        if (service == null) {
            EmbedBuilder selectPlatformEmbed = new EmbedBuilder();
            selectPlatformEmbed.setTitle(bundle.getString("notificationEvents.notificationSlashCommand.selectPlatformEmbed.title"));
            selectPlatformEmbed.setDescription(bundle.getString("notificationEvents.notificationSlashCommand.selectPlatformEmbed.description"));
            selectPlatformEmbed.setColor(notificationEmbedColor);

            StringSelectMenu.Builder stringSelectPlatformMenu = StringSelectMenu.create("notification_initialSlashCommand_StringMenu;%s".formatted(event.getUser().getId()));
            Collection<Button> platformButtons = new ArrayList<>();

            for (String value : values) {
                selectPlatformEmbed.addField(
                        value.toUpperCase(),
                        String.format(bundle.getString("notificationEvents.notificationSlashCommand.selectPlatformEmbed.fields.description"), value),
                        false);

                stringSelectPlatformMenu.addOption(value, value);

                String buttonPattern = "notification_initialSlashCommand_selectPlatformEmbedButton;%s;%s";
                platformButtons.add(Button.of(ButtonStyle.SECONDARY, buttonPattern.formatted(event.getUser().getId(), value), value.toUpperCase()));
            }

            ActionRow actionRow;

            if (values.size() > 3) {
                actionRow = ActionRow.of(stringSelectPlatformMenu.build());
            } else {
                actionRow = ActionRow.of(platformButtons);
            }

            event.replyEmbeds(selectPlatformEmbed.build())
                    .setComponents(actionRow)
                    .setEphemeral(true).queue();

        } else {
            NotificationService notificationService = NotificationService.getNotificationServiceByName(service);
            if (notificationService == null) return;
            event.deferReply(true).queue();
            sendSpecificPlatformEmbed(notificationService, event.getHook());
        }
    }

    @JDAButton(startWith = "notification_initialSlashCommand_selectPlatformEmbedButton")
    public void dispatchSelectPlatformButtonClickEvent(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String serviceString = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;
        event.deferEdit().queue();
        sendSpecificPlatformEmbed(notificationService, event.getHook());
    }

    private void sendSpecificPlatformEmbed(@NotNull NotificationService platform, @NotNull InteractionHook hook) {
        ResourceBundle bundle = bundle(hook.getInteraction().getUserLocale());
        EmbedBuilder platformEmbed = new EmbedBuilder();
        platformEmbed.setTitle(String.format(bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.title"), platform.getServiceName()));
        platformEmbed.setDescription(bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.description"));
        platformEmbed.addField(
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.add.title"),
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.add.description"),
                true);
        platformEmbed.addField(
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.edit.title"),
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.edit.description"),
                true);
        platformEmbed.addField(
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.remove.title"),
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.remove.description"),
                true);
        platformEmbed.setColor(notificationEmbedColor);

        Collection<Button> buttons = new ArrayList<>();
        String buttonPattern = "notification_sendSpecificPlatformEmbed_platformEmbed_%s;%s;%s";
        buttons.add(Button.of(
                ButtonStyle.SECONDARY,
                buttonPattern.formatted(ActionType.ADD.name, hook.getInteraction().getUser().getId(), platform.getServiceName()),
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.add.title")));
        buttons.add(Button.of(
                ButtonStyle.SECONDARY,
                buttonPattern.formatted(ActionType.EDIT.name, hook.getInteraction().getUser().getId(), platform.getServiceName()),
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.edit.title")));
        buttons.add(Button.of(
                ButtonStyle.SECONDARY,
                buttonPattern.formatted(ActionType.REMOVE.name, hook.getInteraction().getUser().getId(), platform.getServiceName()),
                bundle.getString("notificationEvents.sendSpecificPlatformEmbed.platformEmbed.fields.remove.title")));

        hook.editOriginalEmbeds(platformEmbed.build()).setComponents(ActionRow.of(buttons)).queue();
    }

    @JDAButton(startWith = "notification_sendSpecificPlatformEmbed_platformEmbed_add")
    public void addNotificationServiceButtonEvent(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String serviceString = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        TextInput.Builder userName = TextInput.create(
                "notification_addService_userName",
                bundle.getString("notificationEvents.addNotificationServiceButtonEvent.modal.textInput.userName.title"),
                TextInputStyle.SHORT);
        userName.setPlaceholder(bundle.getString("notificationEvents.addNotificationServiceButtonEvent.modal.textInput.userName.placeholder"));
        userName.setMinLength(3);
        userName.setMaxLength(100);
        userName.setRequired(true);

        Modal.Builder modal = Modal.create(
                "notification_addServiceModal;%s;%s".formatted(componentOwnerId, serviceString),
                bundle.getString("notificationEvents.addNotificationServiceButtonEvent.modal.title"));
        modal.addActionRow(userName.build());

        event.replyModal(modal.build()).queue();
    }

    @JDAButton(startWith = "notification_sendSpecificPlatformEmbed_platformEmbed_edit")
    public void editNotificationServiceButtonEvent(@NotNull ButtonInteractionEvent event) {
        editOrRemoveButtonEvent(event, ActionType.EDIT);
    }

    @JDAButton(startWith = "notification_sendSpecificPlatformEmbed_platformEmbed_remove")
    public void removeNotificationServiceButtonEvent(ButtonInteractionEvent event) {
        editOrRemoveButtonEvent(event, ActionType.REMOVE);
    }

    private void editOrRemoveButtonEvent(@NotNull ButtonInteractionEvent event, ActionType actionType) {
        String componentId = event.getComponentId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String serviceString = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        String textInputId = "notification_%sServiceModal_userId".formatted(actionType.getName());
        String modalId = "notification_%sServiceModal;%s;%s";

        TextInput.Builder userId = TextInput.create(
                textInputId,
                bundle.getString("notificationEvents.editOrRemoveNotificationServiceButtonEvent.modal.textInputs.userId.header"),
                TextInputStyle.SHORT);
        userId.setRequired(true);
        userId.setPlaceholder(bundle.getString("notificationEvents.editOrRemoveNotificationServiceButtonEvent.modal.textInputs.userId.placeholder"));

        Modal.Builder editServiceModal = Modal.create(
                String.format(modalId, actionType.getName(), componentOwnerId, serviceString),
                bundle.getString("notificationEvents.editOrRemoveNotificationServiceButtonEvent.modal.title"));


        editServiceModal.addActionRow(userId.build());

        event.replyModal(editServiceModal.build()).queue();
    }


    @JDAModal(startWith = "notification_addServiceModal")
    public void addServiceModalEvent(@NotNull ModalInteractionEvent event) {
        String componentId = event.getModalId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String serviceString = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        ModalMapping userNameMapping = event.getValue("notification_addService_userName");

        if (userNameMapping == null) {
            event.reply(bundle.getString("notificationEvents.addServiceModalEvent.userIdOrUserNameIsNull")).setEphemeral(true).queue();
            return;
        }

        event.deferEdit().queue();

        String userName = userNameMapping.getAsString();

        EmbedBuilder showAddResultEmbed = new EmbedBuilder();
        showAddResultEmbed.setTitle(bundle.getString("notificationEvents.addServiceModalEvent.showAddResultEmbed.title"));
        showAddResultEmbed.setDescription(bundle.getString("notificationEvents.addServiceModalEvent.showAddResultEmbed.description"));

        switch (notificationService) {

            case YOUTUBE -> {
                YouTubeApi youTubeApi = controller.getMain().getYouTubeApi();
                String userId = youTubeApi.getUserId(userName);
                showAddResultEmbed.addField(userName,
                        """
                                ID: %s
                                URL: https://youtube.com/channel/%s
                                """.formatted(userId, userId),
                        false);
            }
            case TWITCH -> {
                TwitchApi twitchApi = controller.getMain().getTwitchApi();
                List<User> users = twitchApi.getUserInformation(userName);
                users.forEach(user -> showAddResultEmbed.addField(
                        user.getDisplayName(),
                        """
                                ID: %s
                                URL: https://twitch.tv/%s
                                """.formatted(user.getId(), user.getLogin()),
                        false));
            }
        }
        String buttonPattern = "notification_addServiceResultEmbed_%s;%s;%s";
        Button okButton = Button.of(ButtonStyle.SUCCESS, String.format(buttonPattern, ActionType.OK.name, componentOwnerId, serviceString), "Ok");
        Button editButton = Button.of(ButtonStyle.SECONDARY, String.format(buttonPattern, ActionType.EDIT.name, componentOwnerId, serviceString), "Edit");
        Button cancelButton = Button.of(ButtonStyle.DANGER, String.format(buttonPattern, ActionType.CANCEL, componentOwnerId, serviceString), "Cancel");
        ActionRow actionRow = ActionRow.of(okButton, editButton, cancelButton);
        event.getHook().editOriginalEmbeds(showAddResultEmbed.build()).setComponents(actionRow).queue();
    }

    @JDAModal(startWith = "notification_editServiceModal")
    public void editServiceModalEvent(ModalInteractionEvent event) {
        initialShowAnnouncementChannelList(event, ActionType.EDIT);
    }

    @JDAModal(startWith = "notification_removeServiceModal")
    public void removeServiceModalEvent(ModalInteractionEvent event) {
        initialShowAnnouncementChannelList(event, ActionType.REMOVE);
    }

    final @NotNull Button nextButton(@NotNull ResourceBundle bundle, String commandUserId, String targetId, int count, @NotNull ActionType actionType) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_%sNotificationEmbed_next;%s;%s;%d", actionType.getName(), commandUserId, targetId, count),
                bundle.getString("notificationEvents.showEmbedButtons.next.label"));
    }

    final @NotNull Button beforeButton(@NotNull ResourceBundle bundle, String commandUserId, String targetId, int count, @NotNull ActionType actionType) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_%sNotificationEmbed_before;%s;%s;%d", actionType.getName(), commandUserId, targetId, count),
                bundle.getString("notificationEvents.showEmbedButtons.before.label"));
    }

    private void initialShowAnnouncementChannelList(@NotNull ModalInteractionEvent event, ActionType actionType) {
        String componentId = event.getModalId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String serviceString = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        event.deferEdit().queue();

        ModalMapping userNameMapping = event.getValues().get(0);
        if (userNameMapping == null) return;

        String userId;

        switch (notificationService) {
            case TWITCH ->
                    userId = controller.getMain().getTwitchApi().getUserInformation(userNameMapping.getAsString()).get(0).getId();
            case YOUTUBE -> userId = controller.getMain().getYouTubeApi().getUserId(userNameMapping.getAsString());
            default -> userId = "";
        }

        JSONArray tableData = db.getDataFromSubscribedChannelTableByContentCreatorId(userId);

        EmbedBuilder initialShowAnnouncementChannelEmbed = new EmbedBuilder();
        StringSelectMenu.Builder stringMenu = StringSelectMenu.create(
                String.format("notification_showAnnouncementEmbed_%sMenu;%s;%s", actionType.getName(), componentOwnerId, userId));

        addFieldsToEmbeds(event.getGuild(), initialShowAnnouncementChannelEmbed, stringMenu, 0, tableData);

        if (actionType.equals(ActionType.REMOVE)) stringMenu.setRequiredRange(1, 25);

        if (tableData.length() > 25) {
            event.getHook().editOriginalEmbeds(initialShowAnnouncementChannelEmbed.build())
                    .setComponents(
                            ActionRow.of(nextButton(bundle, componentOwnerId, userId, 25, actionType)),
                            ActionRow.of(stringMenu.build()))
                    .queue();
        } else {
            event.getHook().editOriginalEmbeds(initialShowAnnouncementChannelEmbed.build())
                    .setComponents(ActionRow.of(stringMenu.build()))
                    .queue();
        }
    }

    @JDAButton(startWith = "noteCommand_editNotificationEmbed_before")
    public void editNotificationEmbedBeforeEvent(ButtonInteractionEvent event) {
        showAnnouncementChannelList(event, ActionType.EDIT, ClickType.BEFORE);
    }

    @JDAButton(startWith = "noteCommand_editNotificationEmbed_next")
    public void editNotificationEmbedNextEvent(ButtonInteractionEvent event) {
        showAnnouncementChannelList(event, ActionType.EDIT, ClickType.NEXT);
    }

    @JDAButton(startWith = "noteCommand_editNotificationEmbed_before")
    public void removeNotificationEmbedBeforeEvent(ButtonInteractionEvent event) {
        showAnnouncementChannelList(event, ActionType.REMOVE, ClickType.BEFORE);
    }

    @JDAButton(startWith = "noteCommand_editNotificationEmbed_next")
    public void removeNotificationEmbedNextEvent(ButtonInteractionEvent event) {
        showAnnouncementChannelList(event, ActionType.REMOVE, ClickType.NEXT);
    }

    private void showAnnouncementChannelList(@NotNull ButtonInteractionEvent event, ActionType actionType, ClickType clickType) {
        String componentId = event.getComponentId();
        String[] splitComponentId = componentId.split(";");
        String componentOwnerId = splitComponentId[1];
        String targetId = splitComponentId[2];
        int count = Integer.parseInt(splitComponentId[3]);

        if (!event.getUser().getId().equals(componentOwnerId)) return;
        ResourceBundle bundle = bundle(event.getUserLocale());

        event.deferEdit().queue();

        JSONArray tableData = db.getDataFromSubscribedChannelTableByContentCreatorId(targetId);

        EmbedBuilder initialShowAnnouncementChannelEmbed = new EmbedBuilder();
        StringSelectMenu.Builder stringMenu = StringSelectMenu.create(
                String.format("notification_showAnnouncementEmbed_%sMenu;%s;%s", actionType.getName(), componentOwnerId, targetId));

        addFieldsToEmbeds(event.getGuild(), initialShowAnnouncementChannelEmbed, stringMenu, 0, tableData);

        if (clickType.equals(ClickType.BEFORE)) {
            if (count - 25 > 0) {
                event.getHook().editOriginalEmbeds(initialShowAnnouncementChannelEmbed.build())
                        .setComponents(
                                ActionRow.of(
                                        beforeButton(bundle, componentOwnerId, targetId, count - 25, actionType),
                                        nextButton(bundle, componentOwnerId, targetId, Math.min(tableData.length() - count, 25), actionType)),
                                ActionRow.of(stringMenu.build()))
                        .queue();
            } else {
                event.getHook().editOriginalEmbeds(initialShowAnnouncementChannelEmbed.build())
                        .setComponents(
                                ActionRow.of(nextButton(bundle, componentOwnerId, targetId, Math.min(tableData.length() - count, 25), actionType)),
                                ActionRow.of(stringMenu.build()))
                        .queue();
            }
        } else {
            if (tableData.length() - count > 1) {
                event.getHook().editOriginalEmbeds(initialShowAnnouncementChannelEmbed.build())
                        .setComponents(
                                ActionRow.of(
                                        beforeButton(bundle, componentOwnerId, targetId, count - 25, actionType),
                                        nextButton(bundle, componentOwnerId, targetId, Math.min(tableData.length() - count, 25), actionType)),
                                ActionRow.of(stringMenu.build()))
                        .queue();
            } else {
                event.getHook().editOriginalEmbeds(initialShowAnnouncementChannelEmbed.build())
                        .setComponents(
                                ActionRow.of(beforeButton(bundle, componentOwnerId, targetId, count - 25, actionType)),
                                ActionRow.of(stringMenu.build()))
                        .queue();
            }
        }
    }

    @JDAStringMenu(startWith = "notification_showAnnouncementEmbed_removeMenu")
    public void notificationChannelRemoveMenuEvent(@NotNull StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();
        String[] splitComponentId = componentId.split(";");
        String componentOwnerId = splitComponentId[1];
        String targetId = splitComponentId[2];

        if (!event.getUser().getId().equals(componentOwnerId)) return;

        event.deferEdit().queue();

        db.removeDataFromSubscribedChannelTable(event.getValues(), targetId);

        event.getHook()
                .editOriginalEmbeds()
                .setComponents()
                .setContent(bundle(event.getUserLocale()).getString("notificationEvents.showAnnouncementEmbedRemoveMenu.removed"))
                .queue();
    }

    @JDAButton(startWith = "notification_addServiceResultEmbed_ok")
    public void addServiceModalButtonOkClickEvent(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        InteractionHook hook = event.getHook();
        // Get component-id and check the id from buttonUser
        String componentId = event.getComponentId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String serviceString = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;

        // Get the embed from the message and check if on component null
        List<MessageEmbed> messageEmbeds = event.getMessage().getEmbeds();
        if (messageEmbeds.isEmpty()) return;
        MessageEmbed showAddResultEmbed = messageEmbeds.get(0);
        if (showAddResultEmbed.getFields().isEmpty()) return;
        String userName = showAddResultEmbed.getFields().get(0).getName();
        String value = showAddResultEmbed.getFields().get(0).getValue();
        if (value == null || userName == null) return;

        // Get channel-id
        String userId = value.split("\n")[0].split(" ")[1];

        ResourceBundle bundle = bundle(event.getUserLocale());
        if (!db.putDataInContentCreatorTable(notificationService, userName, userId)) {
            hook.editOriginal(bundle.getString("notificationEvents.addServiceModalButtonOkClickEvent.putInContentCreatorTableFailed")).queue();
            return;
        }


        EmbedBuilder addServiceSelectChannelEmbed = new EmbedBuilder();
        addServiceSelectChannelEmbed.setTitle(bundle.getString("notificationEvents.addServiceModalButtonOkClickEvent.addServiceSelectEmbed.title"));
        addServiceSelectChannelEmbed.appendDescription(bundle.getString("notificationEvents.addServiceModalButtonOkClickEvent.addServiceSelectEmbed.description"));
        addServiceSelectChannelEmbed.addField(
                bundle.getString("notificationEvents.addServiceModalButtonOkClickEvent.addServiceSelectEmbed.fields.message.title"),
                "%s has published new content".formatted(userName), false);
        addServiceSelectChannelEmbed.setColor(notificationEmbedColor);

        Button editNotificationMessage = Button.of(
                ButtonStyle.SUCCESS,
                "notification_editNotificationMessage;%s;%s".formatted(componentOwnerId, userId),
                bundle.getString("notificationEvents.addServiceModalButtonOkClickEvent.buttons.editNotificationMessage"));

        EntitySelectMenu.Builder channelSelectMenu = EntitySelectMenu
                .create("notification_channelSelect;%s;%s".formatted(componentOwnerId, userId),
                        EntitySelectMenu.SelectTarget.CHANNEL);
        channelSelectMenu.setChannelTypes(
                ChannelType.TEXT, ChannelType.NEWS, ChannelType.GUILD_NEWS_THREAD,
                ChannelType.GUILD_PUBLIC_THREAD, ChannelType.GUILD_PRIVATE_THREAD, ChannelType.FORUM);
        channelSelectMenu.setMinValues(1);
        channelSelectMenu.setMaxValues(24);

        ActionRow actionRow1 = ActionRow.of(editNotificationMessage);
        ActionRow actionRow2 = ActionRow.of(channelSelectMenu.build());
        hook.editOriginalEmbeds(addServiceSelectChannelEmbed.build())
                .setComponents(actionRow1, actionRow2)
                .queue();
    }

    @JDAButton(startWith = "notification_editNotificationMessage")
    public void editNotificationMessageClickEvent(@NotNull ButtonInteractionEvent event) {
        // Get component-id and check the ids from buttonUser and buttonId
        String componentId = event.getComponentId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String userChannelId = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        TextInput.Builder notificationMessageInput = TextInput.create(
                "notificationMessageInput",
                bundle.getString("notificationEvents.editNotificationMessageClickEvent.modal.textInput.label"),
                TextInputStyle.PARAGRAPH);
        notificationMessageInput.setRequired(true);
        // TODO Add Value for NotificationMessage - get from embed
//        notificationMessageInput.setValue()

        Modal.Builder editNotificationModal = Modal.create(
                "notification_editNotificationMessageModal;%s;%s".formatted(componentOwnerId, userChannelId),
                bundle.getString("notificationEvents.editNotificationMessageClickEvent.modal.title"));
        editNotificationModal.addActionRow(notificationMessageInput.build());

        event.replyModal(editNotificationModal.build()).queue();
    }

    @JDAModal(startWith = "notification_editNotificationMessageModal")
    public void editNotificationMessageModalEvent(@NotNull ModalInteractionEvent event) {
        event.deferEdit().queue();
        String componentId = event.getModalId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
//        String userChannelId = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;

        ModalMapping notificationMessageMapping = event.getValue("notificationMessageInput");
        if (notificationMessageMapping == null) return;

        String notificationMessage = notificationMessageMapping.getAsString();

        Message originalMessage = event.getMessage();
        if (originalMessage == null) return;

        MessageEmbed originalEmbed = originalMessage.getEmbeds().get(0);
        if (originalEmbed == null) return;

        if (originalEmbed.getFields().isEmpty()) return;

        MessageEmbed.Field notificationMessageField = originalEmbed.getFields().get(0);
        if (notificationMessageField.getName() == null) return;

        EmbedBuilder modifiedEmbed = new EmbedBuilder(originalEmbed);
        modifiedEmbed.clearFields();
        modifiedEmbed.addField(notificationMessageField.getName(), notificationMessage, notificationMessageField.isInline());

        event.getHook().editOriginalEmbeds(modifiedEmbed.build()).queue();
    }

    @JDAEntityMenu(startWith = "notification_channelSelect")
    public void notificationChannelEntitySelectEvent(@NotNull EntitySelectInteractionEvent event) {
        event.deferEdit().queue();
        InteractionHook hook = event.getHook();
        // Get component-id and check the ids from buttonUser and buttonId
        String componentId = event.getComponentId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String userChannelId = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;

        MessageEmbed originalEmbed = event.getMessage().getEmbeds().get(0);
        if (originalEmbed == null) return;

        if (originalEmbed.getFields().isEmpty()) return;

        MessageEmbed.Field notificationMessageField = originalEmbed.getFields().get(0);
        if (notificationMessageField.getName() == null) return;


        EmbedBuilder notificationChannelSelectEmbed = new EmbedBuilder(originalEmbed);

        notificationChannelSelectEmbed.clearFields();

        notificationChannelSelectEmbed.addField(notificationMessageField);

        for (GuildChannel channel : event.getMentions().getChannels()) {
            if (!db.existRowInSubscribedChannelTable(channel.getIdLong(), userChannelId)) {
                notificationChannelSelectEmbed.addField(
                        channel.getName(),
                        """
                                ID: %s
                                URL: %s
                                """.formatted(channel.getId(), channel.getJumpUrl()),
                        true);
            } else {
                notificationChannelSelectEmbed.addField(
                        channel.getName(),
                        bundle(event.getUserLocale()).getString("notificationEvents.notificationChannelEntitySelectEvent.notificationSelectEmbed.channelExistDescription"),
                        true);
            }
        }

        Button okButton = Button.of(ButtonStyle.SUCCESS,
                "notification_notificationChannelSelect_%s;%s;%s"
                        .formatted(ActionType.OK.name, componentOwnerId, userChannelId), "OK");
        ActionRow actionRow_1 = ActionRow.of(okButton);
        ActionRow actionRow_2 = ActionRow.of(event.getComponent());

        hook.editOriginalEmbeds(notificationChannelSelectEmbed.build())
                .setComponents(actionRow_2, actionRow_1)
                .queue();
    }

    @JDAButton(startWith = "notification_notificationChannelSelect_ok")
    public void notificationChannelSelectOkClickEvent(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        InteractionHook hook = event.getHook();
        // Get component-id and check the ids from buttonUser and buttonId
        String componentId = event.getComponentId();
        String[] splitId = componentId.split(";");
        String componentOwnerId = splitId[1];
        String userChannelId = splitId[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        // Get Embed
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
        if (messageEmbed == null) return;
        List<MessageEmbed.Field> fields = messageEmbed.getFields();
        if (fields.isEmpty()) return;

        MessageEmbed.Field notificationMessage = fields.get(0);
        if (notificationMessage.getValue() == null) return;
        Collection<GuildChannel> guildChannels = new ArrayList<>();


        Guild guild = event.getGuild();
        if (guild == null) return;

        for (MessageEmbed.Field field : fields) {
            if (field.getValue() != null && field != notificationMessage) {
                String rawValue = field.getValue();
                if (rawValue.split("\n").length > 1) {
                    String[] splitValue = rawValue.split("\n")[0].split(" ");
                    GuildChannel guildChannel = guild.getGuildChannelById(splitValue[1]);
                    if (guildChannel != null) {
                        guildChannels.add(guildChannel);
                    }
                }
            }
        }

        if (guildChannels.isEmpty()) {
            hook.editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("notificationEvents.notificationChannelSelectOkClickEvent.guildChannelsEmpty"))
                    .queue();
            return;
        }

        if (db.putDataInSubscribedChannelTable(guildChannels, userChannelId, notificationMessage.getValue())) {
            controller.getMain().getTwitchApi().getChannelNotifications().updateListenedChannels();
            hook.editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("notificationEvents.notificationChannelSelectOkClickEvent.successReply"))
                    .queue();
        } else {
            hook.editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("notificationEvents.notificationChannelSelectOkClickEvent.failReply"))
                    .queue();
        }
    }

    private void addFieldsToEmbeds(Guild guild, EmbedBuilder embedBuilder, StringSelectMenu.Builder stringMenuBuilder, int countFrom, @NotNull JSONArray tableData) {
        int i = 0;

        for (Object o : tableData) {
            JSONObject jsonObject = (JSONObject) o;
            if (guild.getIdLong() == jsonObject.getLong("guildId")) {
                GuildChannel channel = guild.getGuildChannelById(jsonObject.getLong("messageChannelId"));
                if (channel != null) {
                    countFrom--;
                    if (countFrom < 0) {
                        i++;
                        MessageEmbed.Field field = new MessageEmbed.Field(
                                channel.getJumpUrl(),
                                """
                                        Message: %s
                                        """.formatted(jsonObject.getString("messageText")),
                                false);
                        stringMenuBuilder.addOption(channel.getName(), channel.getId());
                        embedBuilder.addField(field);
                        if (i == 25) break;
                    }
                }
            }
        }
    }

    private enum ClickType {
        NEXT,
        BEFORE
    }


    private enum ActionType {
        ADD("add"),
        EDIT("edit"),
        REMOVE("remove"),
        OK("ok"),
        CANCEL("cancel"),
        NEW("new");

        private final String name;

        ActionType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
