package de.SparkArmy.jda.events.customEvents.commandEvents;

import com.github.twitch4j.helix.domain.User;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.DatabaseAction;
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
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.*;

public class NotificationSlashCommandEvents {
    private final ConfigController controller;
    private final DatabaseAction db;

    private final Color notificationEmbedColor = new Color(0x941D9E);

    //TODO update buttons and standard replies to "standardPhrases" bundle

    public NotificationSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
        this.db = new DatabaseAction();
    }

    // Get the resource bundle for this command
    final ResourceBundle bundle(DiscordLocale locale) {
        return Util.getResourceBundle("notification", locale);
    }

    private final List<String> values = Arrays.stream(NotificationService.values()).toList().stream().map(NotificationService::getServiceName).toList();

    // Auto Complete for the Notification Service
    @JDAAutoComplete(commandName = "notification")
    public void notificationPlatformAutocomplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        event.replyChoiceStrings(values).queue();
    }


    // Initial Slash Command Event
    @JDASlashCommand(name = "notification")
    public void notificationInitialSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = bundle(event.getUserLocale());
        String service = event.getOption("platform", OptionMapping::getAsString);
        // Check if event from guild -> command is guild only
        if (!event.isFromGuild()) return;
        // Check if option null -> Create an Embed to select the notification service
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

            // Change from buttons to a SelectMenu if values higher then tree
            if (values.size() > 3) {
                actionRow = ActionRow.of(stringSelectPlatformMenu.build());
            } else {
                actionRow = ActionRow.of(platformButtons);
            }

            event.replyEmbeds(selectPlatformEmbed.build())
                    .setComponents(actionRow)
                    .setEphemeral(true).queue();

        } else {
            // Call method to send a specific Embed
            NotificationService notificationService = NotificationService.getNotificationServiceByName(service);
            if (notificationService == null) return;
            event.deferReply(true).queue();
            sendSpecificPlatformEmbed(notificationService, event.getHook());
        }
    }


    // Button related events
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

    @JDAButton(startWith = "notification_")
    public void dispatchNotificationButtonEvents(@NotNull ButtonInteractionEvent event) {
        // Get component-ID and split
        String componentId = event.getComponentId();
        String[] splitComponentIds = componentId.split(";");
        // Get String of the component-owner-id
        String componentOwnerId = splitComponentIds[1];

        // compare eventUserId and componentOwnerId
        // Return if not equal
        if (!componentOwnerId.equals(event.getUser().getId())) return;
        // Dispatch button notification events by id
        switch (splitComponentIds[0]) {
            case "notification_initialSlashCommand_selectPlatformEmbedButton" ->
                    dispatchSelectPlatformButtonClickEvent(event, splitComponentIds);
            case "notification_sendSpecificPlatformEmbed_platformEmbed_add", "notification_addServiceResultEmbed_edit" ->
                    addOrEditOrRemoveButtonEvent(event, ActionType.ADD, splitComponentIds);
            case "notification_sendSpecificPlatformEmbed_platformEmbed_edit" ->
                    addOrEditOrRemoveButtonEvent(event, ActionType.EDIT, splitComponentIds);
            case "notification_sendSpecificPlatformEmbed_platformEmbed_remove" ->
                    addOrEditOrRemoveButtonEvent(event, ActionType.REMOVE, splitComponentIds);
            case "noteCommand_editNotificationEmbed_before" ->
                    showAnnouncementChannelList(event, ActionType.EDIT, ClickType.BEFORE, splitComponentIds);
            case "noteCommand_editNotificationEmbed_next" ->
                    showAnnouncementChannelList(event, ActionType.EDIT, ClickType.NEXT, splitComponentIds);
            case "noteCommand_removeNotificationEmbed_before" ->
                    showAnnouncementChannelList(event, ActionType.REMOVE, ClickType.BEFORE, splitComponentIds);
            case "noteCommand_removeNotificationEmbed_next" ->
                    showAnnouncementChannelList(event, ActionType.REMOVE, ClickType.NEXT, splitComponentIds);
            case "notification_addServiceResultEmbed_ok" -> addServiceModalButtonOkClickEvent(event, splitComponentIds);
            case "notification_editNotificationMessage" -> editNotificationMessageClickEvent(event, splitComponentIds);
            case "notification_notificationChannelSelect_ok" ->
                    notificationChannelSelectOkClickEvent(event, splitComponentIds);
            default -> {
            }
        }
    }

    // Replies an embed with the standard notification Message, a button to edit this message and a select menu for the channel
    private void addServiceModalButtonOkClickEvent(@NotNull ButtonInteractionEvent event, String @NotNull [] ids) {
        event.deferEdit().queue();
        InteractionHook hook = event.getHook();
        String componentOwnerId = ids[1];
        String serviceString = ids[2];
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;

        // Get the embed from the message and check if on component null
        List<MessageEmbed> messageEmbeds = event.getMessage().getEmbeds();
        if (messageEmbeds.isEmpty()) return;
        MessageEmbed showAddResultEmbed = messageEmbeds.getFirst();
        if (showAddResultEmbed.getFields().isEmpty()) return;
        String userName = showAddResultEmbed.getFields().getFirst().getName();
        String value = showAddResultEmbed.getFields().getFirst().getValue();
        if (value == null || userName == null) return;

        // Get channel-id
        String userId = value.split("\n")[0].split(" ")[1];

        ResourceBundle bundle = bundle(event.getUserLocale());
        if (db.putDataInContentCreatorTable(notificationService, userName, userId) < 0) { //TODO Change to a long based method -> Print the db error
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

    private void editNotificationMessageClickEvent(@NotNull ButtonInteractionEvent event, String @NotNull [] ids) {
        // Get component-id and check the ids from buttonUser and buttonId
        String componentOwnerId = ids[1];
        String userChannelId = ids[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        TextInput.Builder notificationMessageInput = TextInput.create(
                "notificationMessageInput",
                bundle.getString("notificationEvents.editNotificationMessageClickEvent.modal.textInput.label"),
                TextInputStyle.PARAGRAPH);
        notificationMessageInput.setMinLength(10);
        notificationMessageInput.setRequired(true);
        if (!event.getMessage().getEmbeds().isEmpty()) {
            MessageEmbed msgEmbed = event.getMessage().getEmbeds().getFirst();
            if (!msgEmbed.getFields().isEmpty()) {
                notificationMessageInput.setValue(msgEmbed.getFields().getFirst().getValue());
            }
        }

        Modal.Builder editNotificationModal = Modal.create(
                "notification_editNotificationMessageModal;%s;%s".formatted(componentOwnerId, userChannelId),
                bundle.getString("notificationEvents.editNotificationMessageClickEvent.modal.title"));
        editNotificationModal.addActionRow(notificationMessageInput.build());

        event.replyModal(editNotificationModal.build()).queue();
    }

    // Replies an Embed where you can select a NotificationService
    private void dispatchSelectPlatformButtonClickEvent(@NotNull ButtonInteractionEvent event, String @NotNull [] ids) {
        String serviceString = ids[2];
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;
        event.deferEdit().queue();
        sendSpecificPlatformEmbed(notificationService, event.getHook());
    }

    // Replies an Embed where you can select an action for the NotificationService
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

    // Replies with a modal where you type in a userName
    private void addOrEditOrRemoveButtonEvent(@NotNull ButtonInteractionEvent event, ActionType actionType, String @NotNull [] ids) {
        String serviceString = ids[2];
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
                String.format(modalId, actionType.getName(), ids[1], serviceString),
                bundle.getString("notificationEvents.editOrRemoveNotificationServiceButtonEvent.modal.title"));


        editServiceModal.addActionRow(userId.build());

        event.replyModal(editServiceModal.build()).queue();
    }


    // Modal related Events
    @JDAModal(startWith = "notification_")
    public void dispatchNotificationModalEvents(@NotNull ModalInteractionEvent event) {
        // Get component-ID and split
        String componentId = event.getModalId();
        String[] splitComponentIds = componentId.split(";");
        // Get String of the component-owner-id
        String componentOwnerId = splitComponentIds[1];

        // compare eventUserId and componentOwnerId
        // Return if not equal
        if (!componentOwnerId.equals(event.getUser().getId())) return;

        // Dispatch modal events by id
        switch (splitComponentIds[0]) {
            case "notification_addServiceModal" ->
                    initialNotificationModalEvent(event, ActionType.ADD, splitComponentIds);
            case "notification_editServiceModal" ->
                    initialNotificationModalEvent(event, ActionType.EDIT, splitComponentIds);
            case "notification_removeServiceModal" ->
                    initialNotificationModalEvent(event, ActionType.REMOVE, splitComponentIds);
            case "notification_editNotificationMessageModal" -> editNotificationMessageModalEvent(event);
            case "notification_channelMsgEdit" -> editChannelMessageModalEvent(event, splitComponentIds);
        }

    }

    // Replies an Embed for the specific use case
    private void initialNotificationModalEvent(@NotNull ModalInteractionEvent event, ActionType actionType, String @NotNull [] ids) {
        String componentOwnerId = ids[1];
        String serviceString = ids[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;
        NotificationService notificationService = NotificationService.getNotificationServiceByName(serviceString);
        if (notificationService == null) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        event.deferEdit().queue();

        ModalMapping userNameMapping = event.getValues().getFirst();
        if (userNameMapping == null) return;


        String userName = userNameMapping.getAsString();

        // Replies an Embed with 3 action buttons
        if (actionType.equals(ActionType.ADD)) {
            String buttonPattern = "notification_addServiceResultEmbed_%s;%s;%s";
            Button okButton = Button.of(ButtonStyle.SUCCESS, String.format(buttonPattern, ActionType.OK.name, componentOwnerId, serviceString), "Ok");
            Button editButton = Button.of(ButtonStyle.SECONDARY, String.format(buttonPattern, ActionType.EDIT.name, componentOwnerId, serviceString), "Edit");
            ActionRow actionRow = ActionRow.of(okButton, editButton);
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

            event.getHook().editOriginalEmbeds(showAddResultEmbed.build()).setComponents(actionRow).queue();
            return;
        }

        // If ActionType not ADD replies an embed with an SelectMenu

        String userId;
        switch (notificationService) {
            case TWITCH -> userId = controller.getMain().getTwitchApi().getUserInformation(userName).getFirst().getId();
            case YOUTUBE -> userId = controller.getMain().getYouTubeApi().getUserId(userName);
            default -> userId = "";
        }

        JSONArray tableData = db.getDataFromSubscribedChannelTableByContentCreatorId(userId);

        if (tableData.isEmpty()) {
            return;
        }

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

    // Write the data in the database
    private void notificationChannelSelectOkClickEvent(@NotNull ButtonInteractionEvent event, String @NotNull [] ids) {
        event.deferEdit().queue();
        InteractionHook hook = event.getHook();
        // Get component-id and check the ids from buttonUser and buttonId
        String componentOwnerId = ids[1];
        String userChannelId = ids[2];
        if (!event.getUser().getId().equals(componentOwnerId)) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        // Get Embed
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().getFirst();
        if (messageEmbed == null) return;
        List<MessageEmbed.Field> fields = messageEmbed.getFields();
        if (fields.isEmpty()) return;

        MessageEmbed.Field notificationMessage = fields.getFirst();
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

        long addValue = db.putDataInSubscribedChannelTable(guildChannels, userChannelId, notificationMessage.getValue());

        if (addValue > 0) {
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

    private void showAnnouncementChannelList(@NotNull ButtonInteractionEvent event, @NotNull ActionType actionType, @NotNull ClickType clickType, String @NotNull [] ids) {
        String componentOwnerId = ids[1];
        String targetId = ids[2];
        int count = Integer.parseInt(ids[3]);

        ResourceBundle bundle = bundle(event.getUserLocale());

        event.deferEdit().queue();

        JSONArray tableData = db.getDataFromSubscribedChannelTableByContentCreatorId(targetId);

        EmbedBuilder initialShowAnnouncementChannelEmbed = new EmbedBuilder();
        StringSelectMenu.Builder stringMenu = StringSelectMenu.create(
                String.format("notification_showAnnouncementEmbed_%sMenu;%s;%s", actionType.getName(), componentOwnerId, targetId));

        addFieldsToEmbeds(event.getGuild(), initialShowAnnouncementChannelEmbed, stringMenu, count, tableData);

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

    // Event in EDIT routine
    // Update message in database
    private void editChannelMessageModalEvent(@NotNull ModalInteractionEvent event, String @NotNull [] ids) {
        String targetId = ids[2];
        String channelId = ids[3];

        event.deferEdit().queue();

        ResourceBundle bundle = bundle(event.getUserLocale());

        ModalMapping modalMapping = event.getValue("notification_notificationChannelEditModal_textInput_messageInput");
        if (modalMapping == null) return;


        if (db.updateDataInSubscribedChannelTable(modalMapping.getAsString(), Long.parseLong(channelId), targetId) > 0) { // TODO Change db error reply
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("notificationEvents.editChannelMessageModalEvent.successfullyEdit"))
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("notificationEvents.editChannelMessageModalEvent.dbError"))
                    .queue();
        }
    }

    // Event in ADD routine
    // Update message in embed
    public void editNotificationMessageModalEvent(@NotNull ModalInteractionEvent event) {
        event.deferEdit().queue();

        ModalMapping notificationMessageMapping = event.getValues().getFirst();
        if (notificationMessageMapping == null) return;
        String notificationMessage = notificationMessageMapping.getAsString();

        Message originalMessage = event.getMessage();
        if (originalMessage == null) {
            return;
        }

        EmbedBuilder modifiedEmbed = new EmbedBuilder();
        MessageEmbed.Field notificationMessageField = getFieldAndRemoveEmbedFields(event.getMessage(), modifiedEmbed);
        if (notificationMessageField == null) {
            return;
        }
        if (notificationMessageField.getName() == null) {
            return;
        }
        modifiedEmbed.addField(notificationMessageField.getName(), notificationMessage, notificationMessageField.isInline());

        event.getHook().editOriginalEmbeds(modifiedEmbed.build()).queue();
    }

    @JDAStringMenu(startWith = "notification_showAnnouncementEmbed_removeMenu")
    public void notificationChannelRemoveMenuEvent(@NotNull StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();
        String[] splitComponentId = componentId.split(";");
        String componentOwnerId = splitComponentId[1];
        String targetId = splitComponentId[2];

        if (!event.getUser().getId().equals(componentOwnerId)) return;

        event.deferEdit().queue();

        if (db.removeDataFromSubscribedChannelTable(event.getValues(), targetId) > 0) { // TODO Change db error reply
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle(event.getUserLocale()).getString("notificationEvents.showAnnouncementEmbedRemoveMenu.removed"))
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle(event.getUserLocale()).getString("notificationEvents.showAnnouncementEmbedRemoveMenu.dbError"))
                    .queue();

        }


    }

    @JDAStringMenu(startWith = "notification_showAnnouncementEmbed_editMenu")
    public void notificationChannelEditMenuEvent(@NotNull StringSelectInteractionEvent event) {
        String componentId = event.getComponentId();
        String[] splitComponentId = componentId.split(";");
        String componentOwnerId = splitComponentId[1];
        String targetId = splitComponentId[2];

        if (!event.getUser().getId().equals(componentOwnerId)) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        TextInput.Builder messageInput = TextInput.create(
                "notification_notificationChannelEditModal_textInput_messageInput",
                bundle.getString("notificationEvents.showAnnouncementEmbedEditMenu.modal.textInput.messageInput.label"),
                TextInputStyle.PARAGRAPH
        );
        messageInput.setPlaceholder(bundle.getString("notificationEvents.showAnnouncementEmbedEditMenu.modal.textInput.messageInput.placeholder"));
        messageInput.setMinLength(10);

        JSONArray tableData = db.getDataFromSubscribedChannelTableByContentCreatorId(targetId);

        String oldMessage = "";
        String channelId = "";

        // Get Message String for specific channel
        for (Object o : tableData) {
            JSONObject jsonObject = (JSONObject) o;
            String cId = String.valueOf(jsonObject.getLong("messageChannelId"));
            if (event.getValues().getFirst().equals(cId)) {
                channelId = cId;
                oldMessage = jsonObject.getString("messageText");
            }
        }
        if (!oldMessage.isBlank()) messageInput.setValue(oldMessage);

        Modal.Builder messageModificationModal = Modal.create(
                String.format("notification_channelMsgEdit;%s;%s;%s", componentOwnerId, targetId, channelId),
                bundle.getString("notificationEvents.showAnnouncementEmbedEditMenu.modal.title"));

        messageModificationModal.addActionRow(messageInput.build());

        event.replyModal(messageModificationModal.build()).queue();
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

        EmbedBuilder notificationChannelSelectEmbed = new EmbedBuilder();

        notificationChannelSelectEmbed.addField(getFieldAndRemoveEmbedFields(event.getMessage(), notificationChannelSelectEmbed));

        for (GuildChannel channel : event.getMentions().getChannels()) {
            long existValue = db.existRowInSubscribedChannelTable(channel.getIdLong(), userChannelId);
            if (existValue == 0) {
                notificationChannelSelectEmbed.addField(
                        channel.getName(),
                        """
                                ID: %s
                                URL: %s
                                """.formatted(channel.getId(), channel.getJumpUrl()),
                        true);
            } else if (existValue > 0) {
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


    // Helper Methods
    private @Nullable MessageEmbed.Field getFieldAndRemoveEmbedFields(@NotNull Message msg, EmbedBuilder embedBuilder) {
        MessageEmbed originalEmbed = msg.getEmbeds().getFirst();
        if (originalEmbed == null) return null;

        if (originalEmbed.getFields().isEmpty()) return null;
        MessageEmbed.Field field = originalEmbed.getFields().getFirst();
        if (field == null) return null;

        embedBuilder.copyFrom(originalEmbed);
        embedBuilder.clearFields();

        return field;

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
