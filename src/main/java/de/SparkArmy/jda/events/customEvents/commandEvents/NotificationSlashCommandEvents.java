package de.SparkArmy.jda.events.customEvents.commandEvents;

import com.github.twitch4j.helix.domain.User;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.events.annotations.*;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.twitch.TwitchApi;
import de.SparkArmy.twitter.TwitterApi;
import de.SparkArmy.twitter.utils.TwitterUser;
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
            case TWITTER -> {
                TwitterApi twitterApi = controller.getMain().getTwitterApi();
                TwitterUser twitterUser = twitterApi.getUserDataByUsername(userName);
                showAddResultEmbed.addField(twitterUser.getUsername(),
                        """
                                ID: %s
                                URL: https://twitter.com/%s
                                """.formatted(twitterUser.getId(), twitterUser.getName()),
                        false);
            }
        }
        String buttonPattern = "notification_addServiceResultEmbed_%s;%s;%s";
        Button okButton = Button.of(ButtonStyle.SUCCESS, String.format(buttonPattern, ActionType.OK.name, componentOwnerId, serviceString), "Ok");
        Button editButton = Button.of(ButtonStyle.SECONDARY, String.format(buttonPattern, ActionType.EDIT.name, componentOwnerId, serviceString), "Edit");
        Button cancelButton = Button.of(ButtonStyle.DANGER, String.format(buttonPattern, ActionType.CANCEL, componentOwnerId, serviceString), "Cancel");
        ActionRow actionRow = ActionRow.of(okButton, editButton, cancelButton);
        event.getHook().editOriginalEmbeds(showAddResultEmbed.build()).setComponents(actionRow).queue();
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
