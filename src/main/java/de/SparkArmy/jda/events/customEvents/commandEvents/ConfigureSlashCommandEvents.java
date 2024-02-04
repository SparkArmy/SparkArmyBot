package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.annotations.interactions.*;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.jda.utils.ConfigureUtils;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.jda.utils.MediaOnlyPermissions;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

public class ConfigureSlashCommandEvents {

    private final ConfigController controller;
    private final ConfigureUtils configureUtils;

    public ConfigureSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
        this.configureUtils = dispatcher.getApi().getConfigureUtils();
    }

    private ResourceBundle bundle(DiscordLocale locale) {
        return Util.getResourceBundle("configure", locale);
    }

    private ResourceBundle standardPhrases(DiscordLocale locale) {
        return Util.getResourceBundle("standardPhrases", locale);
    }

    @JDASlashCommand(name = "configure")
    public void configureInitialSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String subcommandGroupName = event.getSubcommandGroup();
        String subcommandName = event.getSubcommandName();

        if (subcommandGroupName == null || subcommandName == null) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (subcommandGroupName) {
            case "channel" -> {
                switch (subcommandName) {
                    case "log-channels" -> channelLogChannelConfigureSubcommand(event, bundle, guild, standardPhrases);
                    case "media-only-channel" ->
                            channelMediaOnlyChannelConfigureSubcommand(event, bundle, standardPhrases);
                    case "archive-category" ->
                            channelArchiveCategoryConfigureSubcommand(event, bundle, standardPhrases);
                    case "feedback-channel" ->
                            channelFeedbackChannelConfigureSubcommand(event, bundle, standardPhrases);
                }
            }
            case "roles" -> {
                switch (subcommandName) {
                    case "mod-roles" -> rolesModRolesConfigureSubcommand(event, bundle, standardPhrases);
                    case "punishment-roles" ->
                            rolesPunishmentRolesConfigureSubcommand(event, bundle, standardPhrases, guild);
                }
            }
            case "regex" -> {
                switch (subcommandName) {
                    case "blacklist" -> regexBlacklistConfigureSubcommand(event, bundle, standardPhrases);
                    case "manage" -> regexManageConfigureSubcommand(event, bundle, standardPhrases);
                }
            }
            case "ticket" -> {
                switch (subcommandName) {
                    case "category" -> modMailCategoryConfigureSubcommand(event, bundle);
                    case "blacklist" -> modMailBlacklistConfigureSubcommand(event, bundle, guild, standardPhrases);
                    case "ping-roles" -> modMailPingRolesConfigureSubcommand(event, bundle, guild, standardPhrases);
                    case "message" -> modMailMessageConfigureSubcommand(event, bundle);
                }
            }
        }
    }

    @JDAAutoComplete(commandName = "configure")
    public void configureSlashCommandAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String subcommandGroupName = event.getSubcommandGroup();
        String subcommandName = event.getSubcommandName();

        if (subcommandGroupName == null || subcommandName == null) return;

        if (subcommandGroupName.equals("channel") && subcommandName.equals("log-channels")) {
            event.replyChoiceStrings(LogChannelType.getLogChannelTypes().stream().filter(x -> x.getId() > 0).map(LogChannelType::getName).toList()).queue();
        }
    }

    private void modMailMessageConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle) {
        TextInput.Builder message = TextInput.create("message",
                bundle.getString("configureEvents.modMail.message.modMailMessageConfigureSubcommand.modal.inputs.message.lable"),
                TextInputStyle.PARAGRAPH);
        message.setRequiredRange(20, 4000);
        message.setRequired(true);
        message.setValue(bundle.getString("configureEvents.modMail.message.modMailMessageConfigureSubcommand.modal.inputs.message.defaultValue"));
        message.setPlaceholder(bundle.getString("configureEvents.modMail.message.modMailMessageConfigureSubcommand.modal.inputs.message.placeholder"));

        Modal.Builder messageModal = Modal.create(
                String.format("modMailMessageConfigureModal;%s", event.getUser().getId()),
                bundle.getString("configureEvents.modMail.message.modMailMessageConfigureSubcommand.modal.title"));
        messageModal.addActionRow(message.build());
        event.replyModal(messageModal.build()).queue();
    }

    @JDAModal(startWith = "modMailMessageConfigureModal")
    public void modMailMessageConfigureModalEvent(@NotNull ModalInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getModalId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        event.deferReply(true).queue();

        ModalMapping messageMapping = event.getValue("message");
        if (messageMapping == null) return;
        String message = messageMapping.getAsString();

        Button ticketCreateButton = Button.of(ButtonStyle.SUCCESS, "modMailCreateTicket", "Ticket");

        event.getChannel()
                .sendMessage(message)
                .addActionRow(ticketCreateButton)
                .flatMap(x -> event.getHook().editOriginal(bundle.getString("configureEvents.modMail.message.modMailMessageConfigureModalEvent.reply")))
                .queue();
    }

    private void modMailPingRolesConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferReply(true).queue();
        Role targetRole = event.getOption("role", OptionMapping::getAsRole);

        if (targetRole == null) {
            EmbedBuilder showPingedRolesEmbed = new EmbedBuilder();
            showPingedRolesEmbed.setTitle(bundle.getString("configureEvents.modMail.pingRoles.modMailPingRolesConfigureSubcommand.showPingedRolesEmbed.title"));

            List<Long> roleIds = controller.getGuildModMailPingRoles(guild);

            StringBuilder stringBuilder = new StringBuilder();

            if (roleIds.isEmpty()) {
                stringBuilder.append(standardPhrases.getString("replies.noEntries"));
            } else {
                for (long id : roleIds) {
                    stringBuilder.append(String.format("<@%d>, ", id));
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1).deleteCharAt(stringBuilder.length() - 1);
            }

            showPingedRolesEmbed.setDescription(stringBuilder);

            event.getHook().editOriginalEmbeds(showPingedRolesEmbed.build()).queue();
            return;
        }

        long returnValue;
        String responseString;
        if (controller.isRoleGuildModMailPingRole(targetRole) == 0) {
            returnValue = controller.addGuildModMailPingRole(targetRole);
            if (returnValue == 0) {
                responseString = bundle.getString("configureEvents.modMail.pingRoles.modMailPingRolesConfigureSubcommand.roleAdded");
            } else {
                responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), returnValue);
            }
        } else {
            returnValue = controller.removeGuildModMailPingRole(targetRole);
            if (returnValue == 0) {
                responseString = bundle.getString("configureEvents.modMail.pingRoles.modMailPingRolesConfigureSubcommand.roleRemoved");
            } else {
                responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), returnValue);
            }
        }

        event.getHook().editOriginal(responseString).queue();
    }

    private void modMailBlacklistConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferReply(true).queue();
        User blacklistUser = event.getOption("user", OptionMapping::getAsUser);
        if (blacklistUser == null) {
            EmbedBuilder showBlacklistedModMailUsersEmbed = new EmbedBuilder();
            showBlacklistedModMailUsersEmbed.setTitle(bundle.getString("configureEvents.modMail.blacklist.modMailBlacklistConfigureSubcommand.showBlacklistedModMailUsersEmbed.title"));
            showBlacklistedModMailUsersEmbed.setDescription(bundle.getString("configureEvents.modMail.blacklist.modMailBlacklistConfigureSubcommand.showBlacklistedModMailUsersEmbed.description"));

            List<Long> userIds = controller.getGuildModMailBlacklistedUsers(guild);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            if (!userIds.isEmpty()) {
                for (long id : userIds) {
                    stringBuilder.append(String.format("<@%d>, ", id));
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1).deleteCharAt(stringBuilder.length() - 1);
            } else {
                stringBuilder.append(standardPhrases.getString("replies.noEntries"));
            }
            showBlacklistedModMailUsersEmbed.appendDescription(stringBuilder);

            event.getHook()
                    .editOriginalEmbeds(showBlacklistedModMailUsersEmbed.build())
                    .queue();
            return;
        }

        long returnValue;
        String responseString;
        if (controller.isUserOnGuildModMailBlacklist(guild, blacklistUser) == 0) {
            returnValue = controller.addUserToGuildModMailBlacklist(guild, blacklistUser);
            if (returnValue == 0) {
                responseString = bundle.getString("configureEvents.modMail.blacklist.modMailBlacklistConfigureSubcommand.userAdded");
            } else {
                responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), returnValue);
            }
        } else {
            returnValue = controller.removeUserFromGuildModMailBlacklist(guild, blacklistUser);
            if (returnValue == 0) {
                responseString = bundle.getString("configureEvents.modMail.blacklist.modMailBlacklistConfigureSubcommand.userRemoved");
            } else {
                responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), returnValue);
            }
        }

        event.getHook().editOriginal(responseString).queue();
    }

    private void modMailCategoryConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle) {
        event.deferReply(true).queue();

        EmbedBuilder modMailChannelConfigEmbed = new EmbedBuilder();
        modMailChannelConfigEmbed.setTitle(bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.modMailChannelConfigEmbed.title"));
        modMailChannelConfigEmbed.setDescription(bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.modMailChannelConfigEmbed.description"));

        modMailChannelConfigEmbed.addField(
                bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.modMailChannelConfigEmbed.fields.category.name"),
                bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.modMailChannelConfigEmbed.fields.category.value"),
                true);
        modMailChannelConfigEmbed.addField(
                bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.modMailChannelConfigEmbed.fields.archiveChannel.name"),
                bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.modMailChannelConfigEmbed.fields.archiveChannel.value"),
                true);
        modMailChannelConfigEmbed.addField(
                bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.modMailChannelConfigEmbed.fields.logChannel.name"),
                bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.modMailChannelConfigEmbed.fields.logChannel.value"),
                true);

        String userId = event.getUser().getId();
        Button setCategoryButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("modMailCategoryButtonEvents_setCategory;%s", userId),
                bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.buttons.setCategoryButton"));
        Button setArchiveChannelButton = Button.of(
                ButtonStyle.SECONDARY,
                        String.format("modMailCategoryButtonEvents_setArchive;%s", userId),
                        bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.buttons.setArchiveChannelButton"))
                .asDisabled();
        Button setLogChannelButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("modMailCategoryButtonEvents_setLog;%s", userId),
                bundle.getString("configureEvents.modMail.category.modMailCategoryConfigureSubcommand.buttons.setLogChannelButton"));

        event.getHook()
                .editOriginalEmbeds(modMailChannelConfigEmbed.build())
                .setActionRow(setCategoryButton, setArchiveChannelButton, setLogChannelButton)
                .queue();
    }

    @JDAButton(startWith = "modMailCategoryButtonEvents_")
    public void modMailCategoryButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "modMailCategoryButtonEvents_setCategory" ->
                    modMailCategorySetCategoryButtonEvent(event, bundle, standardPhrases);
            case "modMailCategoryButtonEvents_disableModMail" ->
                    modMailCategoryDisableModMailButtonEvent(event, bundle, guild, standardPhrases);
            case "modMailCategoryButtonEvents_setArchive" ->
                    modMailCategorySetArchiveButtonEvent(event, bundle, standardPhrases);
            case "modMailCategoryButtonEvents_clearArchive" ->
                    modMailCategoryClearArchiveButtonEvent(event, bundle, guild, standardPhrases);
            case "modMailCategoryButtonEvents_setLog" ->
                    modMailCategorySetLogButtonEvent(event, bundle, standardPhrases);
            case "modMailCategoryButtonEvents_clearLog" ->
                    modMailCategoryClearLogButtonEvent(event, bundle, guild, standardPhrases);
        }
    }

    private void modMailCategoryClearLogButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        long responseCode = controller.setGuildModMailArchiveChannel(guild, null);
        String responseString;
        if (responseCode == -15) {
            responseString = bundle.getString("configureEvents.modMail.category.noCategorySet");
        } else if (responseCode == 0) {
            responseString = bundle.getString("configureEvents.modMail.category.modMailCategoryClearLogButtonEvent.successResponse");
        } else {
            responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void modMailCategorySetLogButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        EmbedBuilder setLogChannelEmbed = new EmbedBuilder();
        setLogChannelEmbed.setTitle(bundle.getString("configureEvents.modMail.category.modMailCategorySetLogButtonEvent.setArchiveChannelEmbed.title"));
        setLogChannelEmbed.setDescription(bundle.getString("configureEvents.modMail.category.modMailCategorySetLogButtonEvent.setArchiveChannelEmbed.description"));

        String userId = event.getUser().getId();
        EntitySelectMenu.Builder archiveSelect = EntitySelectMenu.create(
                String.format("modMailCategoryEntitySelectAction_setLog;%s", userId),
                EntitySelectMenu.SelectTarget.CHANNEL);
        archiveSelect.setChannelTypes(ChannelType.TEXT);

        Button clearLogChannel = Button.of(
                ButtonStyle.DANGER,
                String.format("modMailCategoryButtonEvents_clearLog;%s", userId),
                standardPhrases.getString("buttons.clear"));

        event.getHook()
                .editOriginalEmbeds(setLogChannelEmbed.build())
                .setComponents(
                        ActionRow.of(archiveSelect.build()),
                        ActionRow.of(clearLogChannel))
                .queue();

    }

    private void modMailCategoryClearArchiveButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        long responseCode = controller.setGuildModMailArchiveChannel(guild, null);
        String responseString;
        if (responseCode == -15) {
            responseString = bundle.getString("configureEvents.modMail.category.noCategorySet");
        } else if (responseCode == 0) {
            responseString = bundle.getString("configureEvents.modMail.category.modMailCategoryClearArchiveButtonEvent.successResponse");
        } else {
            responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);

        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void modMailCategorySetArchiveButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        EmbedBuilder setArchiveChannelEmbed = new EmbedBuilder();
        setArchiveChannelEmbed.setTitle(bundle.getString("configureEvents.modMail.category.modMailCategorySetArchiveButtonEvent.setArchiveChannelEmbed.title"));
        setArchiveChannelEmbed.setDescription(bundle.getString("configureEvents.modMail.category.modMailCategorySetArchiveButtonEvent.setArchiveChannelEmbed.description"));

        String userId = event.getUser().getId();
        EntitySelectMenu.Builder archiveSelect = EntitySelectMenu.create(
                String.format("modMailCategoryEntitySelectAction_setArchive;%s", userId),
                EntitySelectMenu.SelectTarget.CHANNEL);
        archiveSelect.setChannelTypes(ChannelType.TEXT);

        Button clearArchiveChannel = Button.of(
                ButtonStyle.DANGER,
                String.format("modMailCategoryButtonEvents_clearArchive;%s", userId),
                standardPhrases.getString("buttons.clear"));

        event.getHook()
                .editOriginalEmbeds(setArchiveChannelEmbed.build())
                .setComponents(
                        ActionRow.of(archiveSelect.build()),
                        ActionRow.of(clearArchiveChannel))
                .queue();
    }

    private void modMailCategoryDisableModMailButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        long responseCode = controller.disableGuildModMail(guild);
        String responseString;
        if (responseCode != 0) {
            responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else {
            responseString = bundle.getString("configureEvents.modMail.category.modMailCategoryDisableModMailButtonEvent.successResponse");
        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void modMailCategorySetCategoryButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        EmbedBuilder setCategoryEmbed = new EmbedBuilder();
        setCategoryEmbed.setTitle(bundle.getString("configureEvents.modMail.category.modMailCategorySetCategoryButtonEvent.setCategoryEmbed.title"));
        setCategoryEmbed.setDescription(bundle.getString("configureEvents.modMail.category.modMailCategorySetCategoryButtonEvent.setCategoryEmbed.description"));

        String userId = event.getUser().getId();

        EntitySelectMenu.Builder categorySelect = EntitySelectMenu.create(
                String.format("modMailCategoryEntitySelectAction_setCategory;%s", userId),
                EntitySelectMenu.SelectTarget.CHANNEL
        );
        categorySelect.setChannelTypes(ChannelType.CATEGORY);

        Button disableModMail = Button.of(
                ButtonStyle.DANGER,
                String.format("modMailCategoryButtonEvents_disableModMail;%s", userId),
                standardPhrases.getString("buttons.disable"));

        event.getHook()
                .editOriginalEmbeds(setCategoryEmbed.build())
                .setComponents(
                        ActionRow.of(categorySelect.build()),
                        ActionRow.of(disableModMail))
                .queue();
    }

    @JDAEntityMenu(startWith = "modMailCategoryEntitySelectAction_")
    public void modMailCategoryEntitySelectAction(@NotNull EntitySelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "modMailCategoryEntitySelectAction_setCategory" ->
                    modMailCategorySetCategoryEntitySelectEvent(event, bundle, standardPhrases);
            case "modMailCategoryEntitySelectAction_setArchive" ->
                    modMailCategorySetArchiveEntitySelectEvent(event, bundle, guild, standardPhrases);
            case "modMailCategoryEntitySelectAction_setLog" ->
                    modMailCategorySetLogEntitySelectEvent(event, bundle, guild, standardPhrases);
        }
    }

    private void modMailCategorySetLogEntitySelectEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        TextChannel textChannel = (TextChannel) event.getMentions().getChannels().getFirst();
        if (textChannel == null) return;

        long responseCode = controller.setGuildModMailLogChannel(guild, textChannel);
        String responseString;

        if (responseCode == -15) {
            responseString = bundle.getString("configureEvents.modMail.category.noCategorySet");
        } else if (responseCode > 0) {
            responseString = bundle.getString("configureEvents.modMail.category.modMailCategorySetLogEntitySelectEvent.successResponse");
        } else if (responseCode < 0) {
            responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else {
            responseString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void modMailCategorySetArchiveEntitySelectEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        TextChannel textChannel = (TextChannel) event.getMentions().getChannels().getFirst();
        if (textChannel == null) return;

        long responseCode = controller.setGuildModMailArchiveChannel(guild, textChannel);
        String responseString;

        if (responseCode == -15) {
            responseString = bundle.getString("configureEvents.modMail.category.noCategorySet");
        } else if (responseCode > 0) {
            responseString = bundle.getString("configureEvents.modMail.category.modMailCategorySetArchiveEntitySelectEvent.successResponse");
        } else if (responseCode < 0) {
            responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else {
            responseString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void modMailCategorySetCategoryEntitySelectEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        Category category = (Category) event.getMentions().getChannels().getFirst();
        if (category == null) return;

        long responseCode = controller.setGuildModMailCategory(category);
        String responseString;

        if (responseCode < 0) {
            responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else if (responseCode > 0) {
            responseString = bundle.getString("configureEvents.modMail.category.modMailCategorySetCategoryEntitySelectEvent.successResponse");
        } else {
            responseString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void regexManageConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull ResourceBundle standardPhrases) {
        event.deferReply(true).queue();
        EmbedBuilder manageRegexActionEmbed = new EmbedBuilder();
        manageRegexActionEmbed.setTitle(bundle.getString("configureEvents.regex.manage.regexManageConfigureSubcommand.manageRegexActionEmbed.title"));
        manageRegexActionEmbed.setDescription(bundle.getString("configureEvents.regex.manage.regexManageConfigureSubcommand.manageRegexActionEmbed.description"));

        manageRegexActionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.add"),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureSubcommand.manageRegexActionEmbed.fields.addField.value"),
                true);
        manageRegexActionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.edit"),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureSubcommand.manageRegexActionEmbed.fields.editField.value"),
                true);
        manageRegexActionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.remove"),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureSubcommand.manageRegexActionEmbed.fields.removeField.value"),
                true);

        String userId = event.getUser().getId();

        Button addRegexButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("regexManageConfigureButtonEvents_AddAction;%s", userId),
                standardPhrases.getString("buttons.add"));
        Button editRegexButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexManageConfigureButtonEvents_EditAction;%s", userId),
                standardPhrases.getString("buttons.edit"));
        Button removeRegexButton = Button.of(
                ButtonStyle.DANGER,
                String.format("regexManageConfigureButtonEvents_RemoveAction;%s", userId),
                standardPhrases.getString("buttons.remove"));

        event.getHook()
                .editOriginalEmbeds(manageRegexActionEmbed.build())
                .setActionRow(addRegexButton, editRegexButton, removeRegexButton)
                .queue();
    }

    @JDAButton(startWith = "regexManageConfigureButtonEvents_")
    public void regexManageConfigureButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "regexManageConfigureButtonEvents_AddAction" -> regexManageConfigureAddButtonEvent(event, bundle);
            case "regexManageConfigureButtonEvents_EditAction" ->
                    regexManageConfigureEditButtonEvent(event, bundle, guild, standardPhrases);
            case "regexManageConfigureButtonEvents_RemoveAction" ->
                    regexManageConfigureRemoveButtonEvent(event, bundle, guild, standardPhrases);
            case "regexManageConfigureButtonEvents_Save" ->
                    regexManageConfigureSaveButtonEvent(event, bundle, guild, splitComponentId, standardPhrases);
            case "regexManageConfigureButtonEvents_Test" ->
                    regexManageConfigureTestButtonEvent(event, bundle, splitComponentId);
            case "regexManageConfigureButtonEvents_Edit" ->
                    regexManageConfigureEditRegexButtonEvent(event, bundle, splitComponentId);
            case "regexManageConfigureButtonEvents_Next" ->
                    regexManageConfigureNextButtonEvent(event, bundle, guild, splitComponentId, standardPhrases);
            case "regexManageConfigureButtonEvents_Before" ->
                    regexManageConfigureBeforeButtonEvent(event, bundle, guild, splitComponentId, standardPhrases);
        }
    }

    private void regexManageConfigureRemoveButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        JSONObject regexEntries = controller.getGuildRegexEntries(guild);

        if (regexEntries.isEmpty()) {
            event.getHook()
                    .editOriginal(standardPhrases.getString("replies.noEntries"))
                    .setEmbeds()
                    .setComponents()
                    .queue();
        } else {
            EmbedBuilder removeOverviewEmbed = new EmbedBuilder();
            removeOverviewEmbed.setTitle(bundle.getString("configureEvents.regex.manage.regexManageConfigureRemoveButtonEvent.removeOverviewEmbed.title"));
            removeOverviewEmbed.setDescription(bundle.getString("configureEvents.regex.manage.regexManageConfigureRemoveButtonEvent.removeOverviewEmbed.description"));

            StringSelectMenu.Builder removeRegexMenu = StringSelectMenu.create(
                    String.format("regexManageConfigureStringMenuEvents_Remove;%s", event.getUser().getId()));

            setEmbedAndMenuFieldsForRegexEntries(removeOverviewEmbed, removeRegexMenu, bundle, 0, regexEntries);

            Button nextRegexManageButton = Button.of(
                    ButtonStyle.SECONDARY,
                    String.format("regexManageConfigureButtonEvents_Next;%s;%d", event.getUser().getId(), 25),
                    standardPhrases.getString("buttons.next"));

            if (regexEntries.keySet().size() > 25) {
                event.getHook()
                        .editOriginalEmbeds(removeOverviewEmbed.build())
                        .setComponents(
                                ActionRow.of(removeRegexMenu.build()),
                                ActionRow.of(nextRegexManageButton))
                        .queue();
            } else {
                event.getHook()
                        .editOriginalEmbeds(removeOverviewEmbed.build())
                        .setActionRow(removeRegexMenu.build())
                        .queue();
            }
        }
    }

    private void regexManageConfigureBeforeButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String[] splitComponentId, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        JSONObject regexEntries = controller.getGuildRegexEntries(guild);

        if (regexEntries.isEmpty()) {
            event.getHook()
                    .editOriginal(standardPhrases.getString("replies.noEntries"))
                    .setEmbeds()
                    .setComponents()
                    .queue();
            return;
        }

        MessageEmbed originalEmbed = event.getMessage().getEmbeds().getFirst();

        EmbedBuilder beforeActionEmbed = new EmbedBuilder(originalEmbed);
        beforeActionEmbed.clearFields();

        String stringMenuId = event.getMessage().getActionRows().getFirst().getActionComponents().getFirst().getId();

        if (stringMenuId == null) return;

        StringSelectMenu.Builder stringMenu = StringSelectMenu.create(stringMenuId);

        int count = Integer.parseInt(splitComponentId[2]);

        setEmbedAndMenuFieldsForRegexEntries(beforeActionEmbed, stringMenu, bundle, count, regexEntries);

        String userId = event.getUser().getId();

        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexBlacklistConfigureButtonEvents_NextButton;%s;%d", userId, Math.min(regexEntries.keySet().size() - count, 25)),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureBeforeButtonEvent.buttons.nextButton"));
        Button beforeButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexBlacklistConfigureButtonEvents_BeforeButton;%s;%d", userId, count - 25),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureBeforeButtonEvent.buttons.beforeButton"));

        if (count - 25 > 1) {
            event.getHook()
                    .editOriginalEmbeds(beforeActionEmbed.build())
                    .setComponents(
                            ActionRow.of(stringMenu.build()),
                            ActionRow.of(beforeButton, nextButton))
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds(beforeActionEmbed.build())
                    .setComponents(
                            ActionRow.of(stringMenu.build()),
                            ActionRow.of(beforeButton))
                    .queue();
        }
    }

    private void regexManageConfigureNextButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String[] splitComponentId, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        JSONObject regexEntries = controller.getGuildRegexEntries(guild);
        if (regexEntries.isEmpty()) {
            event.getHook()
                    .editOriginal(standardPhrases.getString("replies.noEntries"))
                    .setEmbeds()
                    .setComponents()
                    .queue();
            return;
        }

        MessageEmbed originalEmbed = event.getMessage().getEmbeds().getFirst();

        EmbedBuilder nextActionEmbed = new EmbedBuilder(originalEmbed);
        nextActionEmbed.clearFields();

        String stringMenuId = event.getMessage().getActionRows().getFirst().getActionComponents().getFirst().getId();

        if (stringMenuId == null) return;

        StringSelectMenu.Builder stringMenu = StringSelectMenu.create(stringMenuId);

        int count = Integer.parseInt(splitComponentId[2]);

        setEmbedAndMenuFieldsForRegexEntries(nextActionEmbed, stringMenu, bundle, count, regexEntries);

        String userId = event.getUser().getId();

        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexManageConfigureButtonEvents_Next;%s;%d", userId, Math.min(regexEntries.keySet().size() - count, 25)),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureNextButtonEvent.buttons.nextButton"));
        Button beforeButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexManageConfigureButtonEvents_Before;%s;%d", userId, count - 25),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureNextButtonEvent.buttons.beforeButton"));


        if (regexEntries.length() - count > 1) {
            event.getHook()
                    .editOriginalEmbeds(nextActionEmbed.build())
                    .setComponents(
                            ActionRow.of(stringMenu.build()),
                            ActionRow.of(beforeButton, nextButton))
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds(nextActionEmbed.build())
                    .setComponents(
                            ActionRow.of(stringMenu.build()),
                            ActionRow.of(beforeButton))
                    .queue();
        }
    }

    private void regexManageConfigureEditRegexButtonEvent(ButtonInteractionEvent event, ResourceBundle bundle, String @NotNull [] splitComponentId) {
        String paramString = String.format("%s;%s", splitComponentId[1], splitComponentId[2]);
        if (configureUtils.isRegexNotInMap(paramString)) {
            event.deferEdit()
                    .setEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.regex.manage.seasonIsInvalid"))
                    .queue();
        } else {
            JSONObject entry = configureUtils.getRegexByKey(paramString);
            TextInput.Builder regexPhrase = TextInput.create(
                    "regexPhrase",
                    bundle.getString("configureEvents.regex.manage.regexManageConfigureEditRegexButtonEvent.editRegexModal.regexPhrase.lable"),
                    TextInputStyle.PARAGRAPH);
            regexPhrase.setPlaceholder(bundle.getString("configureEvents.regex.manage.regexManageConfigureEditRegexButtonEvent.editRegexModal.regexPhrase.placeholder"));
            regexPhrase.setRequired(true);
            regexPhrase.setRequiredRange(1, 3000);
            regexPhrase.setValue(entry.getString("regex"));

            TextInput.Builder regexName = TextInput.create(
                    "regexName",
                    bundle.getString("configureEvents.regex.manage.regexManageConfigureEditRegexButtonEvent.editRegexModal.regexName.lable"),
                    TextInputStyle.SHORT);
            regexName.setPlaceholder(bundle.getString("configureEvents.regex.manage.regexManageConfigureEditRegexButtonEvent.editRegexModal.regexName.placeholder"));
            regexName.setRequired(true);
            regexName.setRequiredRange(1, 100);
            regexName.setValue(entry.getString("name"));

            String modalId;
            if (entry.isNull("id")) {
                modalId = String.format("regexManageModalEvents_AddOrEditModal;%s", event.getUser().getId());
            } else {
                modalId = String.format("regexManageModalEvents_AddOrEditModal;%s;%d", event.getUser().getId(), entry.getLong("id"));
            }

            Modal.Builder editRegexModal = Modal.create(
                    modalId,
                    bundle.getString("configureEvents.regex.manage.regexManageConfigureEditRegexButtonEvent.editRegexModal.title"));
            editRegexModal.addActionRow(regexPhrase.build());
            editRegexModal.addActionRow(regexName.build());

            event.replyModal(editRegexModal.build())
                    .map(x -> {
                        configureUtils.removeRegexByKey(paramString);
                        return null;
                    })
                    .queue();
        }

    }

    private void regexManageConfigureEditButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        JSONObject entries = controller.getGuildRegexEntries(guild);

        if (entries.isEmpty()) {
            event.getHook()
                    .editOriginal(bundle.getString("configureEvents.regex.manage.regexManageConfigureEditButtonEvent.entriesAreEmpty"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
            return;
        }

        EmbedBuilder editOverviewEmbed = new EmbedBuilder();
        editOverviewEmbed.setTitle(bundle.getString("configureEvents.regex.manage.regexManageConfigureEditButtonEvent.editOverviewEmbed.title"));
        editOverviewEmbed.setDescription(bundle.getString("configureEvents.regex.manage.regexManageConfigureEditButtonEvent.editOverviewEmbed.description"));

        StringSelectMenu.Builder editRegexMenu = StringSelectMenu.create(
                String.format("regexManageConfigureStringMenuEvents_Edit;%s", event.getUser().getId()));

        setEmbedAndMenuFieldsForRegexEntries(editOverviewEmbed, editRegexMenu, bundle, 0, entries);

        Button nextRegexManageButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexManageConfigureButtonEvents_Next;%s;%d", event.getUser().getId(), 25),
                standardPhrases.getString("buttons.next"));


        if (entries.keySet().size() > 25) {
            event.getHook()
                    .editOriginalEmbeds(editOverviewEmbed.build())
                    .setComponents(
                            ActionRow.of(editRegexMenu.build()),
                            ActionRow.of(nextRegexManageButton)
                    )
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds(editOverviewEmbed.build())
                    .setActionRow(editRegexMenu.build())
                    .queue();
        }
    }

    private void regexManageConfigureTestButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, String @NotNull [] splitComponentId) {
        String paramString = String.format("%s;%s", splitComponentId[1], splitComponentId[2]);
        if (configureUtils.isRegexNotInMap(paramString)) {
            event.deferEdit()
                    .setEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.regex.manage.seasonIsInvalid"))
                    .queue();
        } else {
            TextInput.Builder regexTest = TextInput.create(
                    "regexTest",
                    bundle.getString("configureEvents.regex.manage.regexManageConfigureTestButtonEvent.testModal.regexTest.title"),
                    TextInputStyle.PARAGRAPH);
            regexTest.setRequiredRange(1, 4000);
            regexTest.setRequired(true);
            regexTest.setPlaceholder(bundle.getString("configureEvents.regex.manage.regexManageConfigureTestButtonEvent.testModal.regexTest.placeholder"));

            Modal.Builder testModal = Modal.create(
                    String.format("regexManageModalEvents_TestModal;%s", paramString),
                    bundle.getString("configureEvents.regex.manage.regexManageConfigureTestButtonEvent.testModal.regexTest.title"));

            testModal.addActionRow(regexTest.build());

            event.replyModal(testModal.build()).queue();
        }
    }

    private void regexManageConfigureSaveButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String @NotNull [] splitComponentId, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        String paramString = String.format("%s;%s", splitComponentId[1], splitComponentId[2]);
        if (configureUtils.isRegexNotInMap(paramString)) {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.regex.manage.seasonIsInvalid"))
                    .queue();
        } else {
            JSONObject regexData = configureUtils.getRegexByKey(paramString);
            String regexString = regexData.getString("regex");
            String regexName = regexData.getString("name");
            long id = regexData.optLong("id", 0);
            long returnValue = controller.addOrEditRegexToGuildRegexTable(regexString, regexName, guild, id);

            if (returnValue < 0) {
                event.getHook()
                        .editOriginalEmbeds()
                        .setComponents()
                        .setContent(
                                String.format(
                                        standardPhrases.getString("replies.dbErrorReply"),
                                        returnValue))
                        .map(x -> {
                            configureUtils.removeRegexByKey(paramString);
                            return null;
                        })
                        .queue();
            } else if (returnValue > 0) {
                event.getHook()
                        .editOriginalEmbeds()
                        .setComponents()
                        .setContent(
                                String.format(
                                        bundle.getString("configureEvents.regex.manage.regexManageConfigureSaveButtonEvent.returnValueIs0"),
                                        regexString))
                        .map((x -> {
                            configureUtils.removeRegexByKey(paramString);
                            return null;
                        }))
                        .queue();
            } else {
                event.getHook()
                        .editOriginalEmbeds()
                        .setComponents()
                        .setContent(
                                standardPhrases.getString("replies.noDataEdit"))
                        .map((x -> {
                            configureUtils.removeRegexByKey(paramString);
                            return null;
                        }))
                        .queue();
            }
        }
    }

    private void regexManageConfigureAddButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle) {
        TextInput.Builder regexPhrase = TextInput.create(
                "regexPhrase",
                bundle.getString("configureEvents.regex.manage.regexManageConfigureAddButtonEvent.addRegexModal.regexPhrase.lable"),
                TextInputStyle.PARAGRAPH);
        regexPhrase.setPlaceholder(bundle.getString("configureEvents.regex.manage.regexManageConfigureAddButtonEvent.addRegexModal.regexPhrase.placeholder"));
        regexPhrase.setRequired(true);
        regexPhrase.setRequiredRange(1, 3000);

        TextInput.Builder regexName = TextInput.create(
                "regexName",
                bundle.getString("configureEvents.regex.manage.regexManageConfigureAddButtonEvent.addRegexModal.regexName.lable"),
                TextInputStyle.SHORT);
        regexName.setPlaceholder(bundle.getString("configureEvents.regex.manage.regexManageConfigureAddButtonEvent.addRegexModal.regexName.placeholder"));
        regexName.setRequired(true);
        regexName.setRequiredRange(1, 100);

        Modal.Builder addRegexModal = Modal.create(
                String.format("regexManageModalEvents_AddOrEditModal;%s", event.getUser().getId()),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureAddButtonEvent.addRegexModal.title"));
        addRegexModal.addActionRow(regexPhrase.build());
        addRegexModal.addActionRow(regexName.build());

        event.replyModal(addRegexModal.build()).queue();
    }

    @JDAModal(startWith = "regexManageModalEvents_")
    public void regexManageModalEvents(@NotNull ModalInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getModalId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "regexManageModalEvents_AddOrEditModal" ->
                    regexManageAddOrEditModalEvent(event, bundle, splitComponentId, standardPhrases);
            case "regexManageModalEvents_TestModal" -> regexManageTestModalEvent(event, bundle, splitComponentId);
        }
    }

    private void regexManageTestModalEvent(@NotNull ModalInteractionEvent event, ResourceBundle bundle, String @NotNull [] splitComponentId) {
        event.deferReply(true).queue();
        String paramString = String.format("%s;%s", splitComponentId[1], splitComponentId[2]);
        if (configureUtils.isRegexNotInMap(paramString)) {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.regex.manage.seasonIsInvalid"))
                    .queue();
        } else {
            JSONObject regexData = configureUtils.getRegexByKey(paramString);
            String regexString = regexData.getString("regex");

            ModalMapping regexTestMapping = event.getValue("regexTest");
            if (regexTestMapping == null) return;
            String testString = regexTestMapping.getAsString();

            try {
                event.getHook().editOriginal(
                                String.format(
                                        bundle.getString("configureEvents.regex.manage.regexManageTestModalEvent.replySeasonIsValid"),
                                        testString.matches(regexString)))
                        .queue();
            } catch (PatternSyntaxException e) {
                event.getHook().editOriginal(
                                String.format(
                                        bundle.getString("configureEvents.regex.manage.regexManageTestModalEvent.errorTestMessage"),
                                        e.getMessage()))
                        .queue();
            }
        }
    }

    private void regexManageAddOrEditModalEvent(@NotNull ModalInteractionEvent event, ResourceBundle bundle, String[] splitComponentId, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        ModalMapping phraseMapping = event.getValue("regexPhrase");
        ModalMapping nameMapping = event.getValue("regexName");
        if (phraseMapping == null || nameMapping == null) return;
        String regexPhrase = phraseMapping.getAsString();
        String regexName = nameMapping.getAsString();


        EmbedBuilder addShowEmbed = new EmbedBuilder();
        addShowEmbed.setTitle(bundle.getString("configureEvents.regex.manage.regexManageAddOrEditModalEvent.addOrEditShowEmbed.title"));

        String userId = event.getUser().getId();

        String paramString = String.format("%s;%s", userId, LocalDateTime.now());

        JSONObject regexData = new JSONObject();
        regexData.put("regex", regexPhrase);
        regexData.put("name", regexName);


        if (splitComponentId.length == 3) {
            regexData.put("id", Long.parseLong(splitComponentId[2]));
        }

        configureUtils.addRegexToMap(paramString, regexData);


        addShowEmbed.setDescription(String.format(bundle.getString("configureEvents.regex.manage.regexManageAddOrEditModalEvent.addOrEditShowEmbed.description"), regexPhrase));
        addShowEmbed.addField(
                standardPhrases.getString("embeds.fields.name.save"),
                bundle.getString("configureEvents.regex.manage.regexManageAddOrEditModalEvent.addOrEditShowEmbed.fields.save.value"),
                true);
        addShowEmbed.addField(
                standardPhrases.getString("embeds.fields.name.test"),
                bundle.getString("configureEvents.regex.manage.regexManageAddOrEditModalEvent.addOrEditShowEmbed.fields.test.value"),
                true);
        addShowEmbed.addField(
                standardPhrases.getString("embeds.fields.name.edit"),
                bundle.getString("configureEvents.regex.manage.regexManageAddOrEditModalEvent.addOrEditShowEmbed.fields.edit.value"),
                true);

        Button saveRegexButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("regexManageConfigureButtonEvents_Save;%s", paramString),
                standardPhrases.getString("buttons.save"));
        Button testRegexButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexManageConfigureButtonEvents_Test;%s", paramString),
                standardPhrases.getString("buttons.test"));
        Button editRegexButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexManageConfigureButtonEvents_Edit;%s", paramString),
                standardPhrases.getString("buttons.edit"));

        event.getHook()
                .editOriginalEmbeds(addShowEmbed.build())
                .setActionRow(saveRegexButton, testRegexButton, editRegexButton)
                .delay(15, TimeUnit.MINUTES)
                .map(x -> {
                    configureUtils.removeRegexByKey(paramString);
                    return null;
                })
                .queue();
    }

    private void setEmbedAndMenuFieldsForRegexEntries(EmbedBuilder actionEmbed, StringSelectMenu.Builder stringMenuBuilder, ResourceBundle bundle, int countFrom, @NotNull JSONObject entries) {
        int i = 0;
        for (String keyString : entries.keySet().stream().sorted().toList()) {
            countFrom--;
            if (countFrom < 0) {
                i++;
                JSONObject entry = entries.getJSONObject(keyString);
                actionEmbed.addField(
                        String.format(bundle.getString("configureEvents.regex.manage.setEmbedAndMenuFieldsForRegexEntries.actionEmbed.fieldName"), entry.getString("name")),
                        String.format(bundle.getString("configureEvents.regex.manage.setEmbedAndMenuFieldsForRegexEntries.actionEmbed.fieldValuePreset"), entry.getString("regex")),
                        false);
                stringMenuBuilder.addOption(entry.getString("name"), String.valueOf(entry.getLong("id")));
                if (i == 25) break;
            }
        }
    }

    @JDAStringMenu(startWith = "regexManageConfigureStringMenuEvents_")
    public void regexManageConfigureStringMenuEvents(@NotNull StringSelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "regexManageConfigureStringMenuEvents_Edit" -> regexManageConfigureEditStringMenuEvent(event, bundle);
            case "regexManageConfigureStringMenuEvents_Remove" ->
                    regexManageConfigureRemoveStringMenuEvent(event, bundle, standardPhrases);
        }
    }

    private void regexManageConfigureRemoveStringMenuEvent(@NotNull StringSelectInteractionEvent event, ResourceBundle bundle, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        long id = Long.parseLong(event.getValues().getFirst());
        JSONObject entry = controller.getRegexEntryById(String.valueOf(id));
        if (entry.isEmpty()) {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.regex.manage.regexManageConfigureRemoveStringMenuEvent.entryIsEmpty"))
                    .queue();
            return;
        }

        long responseCode = controller.removeGuildRegexEntry(id);

        String replyString;
        if (responseCode < 0) {
            replyString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else if (responseCode > 0) {
            replyString = bundle.getString("configureEvents.regex.manage.regexManageConfigureRemoveStringMenuEvent.successResponse");
        } else {
            replyString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(replyString)
                .queue();
    }

    private void regexManageConfigureEditStringMenuEvent(@NotNull StringSelectInteractionEvent event, @NotNull ResourceBundle bundle) {
        long id = Long.parseLong(event.getValues().getFirst());
        JSONObject entry = controller.getRegexEntryById(String.valueOf(id));
        if (entry.isEmpty()) {
            event.deferEdit()
                    .setComponents()
                    .setEmbeds()
                    .setContent(bundle.getString("configureEvents.regex.manage.regexManageConfigureEditStringMenuEvent.entryIsEmpty"))
                    .queue();
            return;
        }

        TextInput.Builder regexPhrase = TextInput.create(
                "regexPhrase",
                bundle.getString("configureEvents.regex.manage.regexManageConfigureEditStringMenuEvent.editRegexModal.regexPhrase.lable"),
                TextInputStyle.PARAGRAPH);
        regexPhrase.setPlaceholder(bundle.getString("configureEvents.regex.manage.regexManageConfigureEditStringMenuEvent.editRegexModal.regexPhrase.placeholder"));
        regexPhrase.setRequired(true);
        regexPhrase.setRequiredRange(1, 3000);
        regexPhrase.setValue(entry.getString("regex"));

        TextInput.Builder regexName = TextInput.create(
                "regexName",
                bundle.getString("configureEvents.regex.manage.regexManageConfigureEditStringMenuEvent.editRegexModal.regexName.lable"),
                TextInputStyle.SHORT);
        regexName.setPlaceholder(bundle.getString("configureEvents.regex.manage.regexManageConfigureEditStringMenuEvent.editRegexModal.regexName.placeholder"));
        regexName.setRequired(true);
        regexName.setRequiredRange(1, 100);
        regexName.setValue(entry.getString("name"));

        Modal.Builder editRegexModal = Modal.create(
                String.format("regexManageModalEvents_AddOrEditModal;%s;%d", event.getUser().getId(), id),
                bundle.getString("configureEvents.regex.manage.regexManageConfigureEditStringMenuEvent.editRegexModal.title"));
        editRegexModal.addActionRow(regexPhrase.build());
        editRegexModal.addActionRow(regexName.build());

        event.replyModal(editRegexModal.build()).queue();
    }

    private void regexBlacklistConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull ResourceBundle standardPhrases) {
        event.deferReply(true).queue();
        EmbedBuilder blacklistActionEmbed = new EmbedBuilder();
        blacklistActionEmbed.setTitle(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureSubcommand.blacklistActionEmbed.title"));
        blacklistActionEmbed.setDescription(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureSubcommand.blacklistActionEmbed.description"));

        blacklistActionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.add"),
                bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureSubcommand.blacklistActionEmbed.fields.addField.value"),
                true);
        blacklistActionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.edit"),
                bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureSubcommand.blacklistActionEmbed.fields.editField.value"),
                true);
        blacklistActionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.remove"),
                bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureSubcommand.blacklistActionEmbed.fields.removeField.value"),
                true);

        String userId = event.getUser().getId();

        Button addPhraseButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("regexBlacklistConfigureButtonEvents_AddButton;%s", userId),
                standardPhrases.getString("buttons.add"));
        Button editPhraseButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexBlacklistConfigureButtonEvents_EditButton;%s", userId),
                standardPhrases.getString("buttons.edit"));
        Button removePhraseButton = Button.of(
                ButtonStyle.DANGER,
                String.format("regexBlacklistConfigureButtonEvents_RemoveButton;%s", userId),
                standardPhrases.getString("buttons.remove"));


        event.getHook()
                .editOriginalEmbeds(blacklistActionEmbed.build())
                .setActionRow(addPhraseButton, editPhraseButton, removePhraseButton)
                .queue();
    }

    @JDAButton(startWith = "regexBlacklistConfigureButtonEvents_")
    public void regexBlacklistConfigureButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "regexBlacklistConfigureButtonEvents_AddButton" ->
                    regexBlacklistConfigureAddButtonEvent(event, bundle);
            case "regexBlacklistConfigureButtonEvents_EditButton" ->
                    regexBlacklistConfigureEditButtonEvent(event, bundle, guild, standardPhrases);
            case "regexBlacklistConfigureButtonEvents_RemoveButton" ->
                    regexBlacklistConfigureRemoveButtonEvent(event, bundle, guild, standardPhrases);
            case "regexBlacklistConfigureButtonEvents_NextButton" ->
                    regexBlacklistConfigureNextButtonEvent(event, bundle, guild, splitComponentId, standardPhrases);
            case "regexBlacklistConfigureButtonEvents_BeforeButton" ->
                    regexBlacklistConfigureBeforeButtonEvent(event, bundle, guild, splitComponentId, standardPhrases);
        }
    }

    private void regexBlacklistConfigureBeforeButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String[] splitComponentId, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        JSONObject blacklistEntries = controller.getGuildBlacklistPhrases(guild);

        if (blacklistEntries.isEmpty()) {
            event.getHook()
                    .editOriginal(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureNextButtonEvent.entriesAreEmpty"))
                    .setEmbeds()
                    .setComponents()
                    .queue();
            return;
        }

        MessageEmbed originalEmbed = event.getMessage().getEmbeds().getFirst();

        EmbedBuilder beforeActionEmbed = new EmbedBuilder(originalEmbed);
        beforeActionEmbed.clearFields();

        String stringMenuId = event.getMessage().getActionRows().getFirst().getActionComponents().getFirst().getId();

        if (stringMenuId == null) return;

        StringSelectMenu.Builder stringMenu = StringSelectMenu.create(stringMenuId);

        int count = Integer.parseInt(splitComponentId[2]);

        setEmbedAndMenuFieldsForBlacklistEntries(beforeActionEmbed, stringMenu, count, blacklistEntries, bundle);

        String userId = event.getUser().getId();

        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexBlacklistConfigureButtonEvents_NextButton;%s;%d", userId, Math.min(blacklistEntries.keySet().size() - count, 25)),
                standardPhrases.getString("buttons.next"));
        Button beforeButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexBlacklistConfigureButtonEvents_BeforeButton;%s;%d", userId, count - 25),
                standardPhrases.getString("buttons.before"));


        if (count - 25 > 1) {
            event.getHook()
                    .editOriginalEmbeds(beforeActionEmbed.build())
                    .setComponents(
                            ActionRow.of(stringMenu.build()),
                            ActionRow.of(beforeButton, nextButton))
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds(beforeActionEmbed.build())
                    .setComponents(
                            ActionRow.of(stringMenu.build()),
                            ActionRow.of(beforeButton))
                    .queue();
        }
    }

    private void regexBlacklistConfigureNextButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String[] splitComponentId, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        JSONObject blacklistEntries = controller.getGuildBlacklistPhrases(guild);

        if (blacklistEntries.isEmpty()) {
            event.getHook()
                    .editOriginal(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureNextButtonEvent.entriesAreEmpty"))
                    .setEmbeds()
                    .setComponents()
                    .queue();
            return;
        }

        MessageEmbed originalEmbed = event.getMessage().getEmbeds().getFirst();

        EmbedBuilder nextActionEmbed = new EmbedBuilder(originalEmbed);
        nextActionEmbed.clearFields();

        String stringMenuId = event.getMessage().getActionRows().getFirst().getActionComponents().getFirst().getId();

        if (stringMenuId == null) return;

        StringSelectMenu.Builder stringMenu = StringSelectMenu.create(stringMenuId);

        int count = Integer.parseInt(splitComponentId[2]);

        setEmbedAndMenuFieldsForBlacklistEntries(nextActionEmbed, stringMenu, count, blacklistEntries, bundle);

        String userId = event.getUser().getId();

        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexBlacklistConfigureButtonEvents_NextButton;%s;%d", userId, Math.min(blacklistEntries.keySet().size() - count, 25)),
                standardPhrases.getString("buttons.next"));
        Button beforeButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexBlacklistConfigureButtonEvents_BeforeButton;%s;%d", userId, count - 25),
                standardPhrases.getString("buttons.before"));


        if (blacklistEntries.length() - count > 1) {
            event.getHook()
                    .editOriginalEmbeds(nextActionEmbed.build())
                    .setComponents(
                            ActionRow.of(stringMenu.build()),
                            ActionRow.of(beforeButton, nextButton))
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds(nextActionEmbed.build())
                    .setComponents(
                            ActionRow.of(stringMenu.build()),
                            ActionRow.of(beforeButton))
                    .queue();
        }
    }

    private void regexBlacklistConfigureRemoveButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        JSONObject blacklistEntries = controller.getGuildBlacklistPhrases(guild);

        if (blacklistEntries.isEmpty()) {
            event.getHook()
                    .editOriginal(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureRemoveButtonEvent.entriesAreEmpty"))
                    .setEmbeds()
                    .setComponents()
                    .queue();
            return;
        }

        EmbedBuilder removeActionEmbed = new EmbedBuilder();
        removeActionEmbed.setTitle(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureRemoveButtonEvent.removeActionEmbed.title"));
        removeActionEmbed.setDescription(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureRemoveButtonEvent.removeActionEmbed.description"));

        String userId = event.getUser().getId();

        StringSelectMenu.Builder editMenu = StringSelectMenu.create(String.format("regexBlacklistConfigureStringMenuEvents_RemoveAction;%s", userId));

        sendInitialBacklistEntryEmbed(event, bundle, blacklistEntries, removeActionEmbed, userId, editMenu, standardPhrases);
    }

    private void regexBlacklistConfigureEditButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        JSONObject blacklistEntries = controller.getGuildBlacklistPhrases(guild);

        if (blacklistEntries.isEmpty()) {
            event.getHook()
                    .editOriginal(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureEditButtonEvent.entriesAreEmpty"))
                    .setEmbeds()
                    .setComponents()
                    .queue();
            return;
        }

        EmbedBuilder editActionEmbed = new EmbedBuilder();
        editActionEmbed.setTitle(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureEditButtonEvent.editActionEmbed.title"));
        editActionEmbed.setDescription(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureEditButtonEvent.editActionEmbed.description"));

        String userId = event.getUser().getId();

        StringSelectMenu.Builder editMenu = StringSelectMenu.create(String.format("regexBlacklistConfigureStringMenuEvents_EditAction;%s", userId));

        sendInitialBacklistEntryEmbed(event, bundle, blacklistEntries, editActionEmbed, userId, editMenu, standardPhrases);
    }

    private void regexBlacklistConfigureAddButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle) {
        TextInput.Builder phraseTextField = TextInput.create(
                "phraseTextField",
                bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureAddButtonEvent.phraseAddModel.phraseTextField.title"),
                TextInputStyle.SHORT);
        phraseTextField.setRequiredRange(1, 100);
        phraseTextField.setRequired(true);
        phraseTextField.setPlaceholder(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureAddButtonEvent.phraseAddModel.phraseTextField.placeholder"));

        Modal.Builder phraseAddModel = Modal.create(
                String.format("regexBlacklistConfigureModalEvents_AddModal;%s", event.getUser().getId()),
                bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureAddButtonEvent.phraseAddModel.title"));

        phraseAddModel.addActionRow(phraseTextField.build());

        event.replyModal(phraseAddModel.build()).queue();
    }

    private void sendInitialBacklistEntryEmbed(ButtonInteractionEvent event, ResourceBundle bundle, JSONObject blacklistEntries, EmbedBuilder removeActionEmbed, String userId, StringSelectMenu.Builder editMenu, @NotNull ResourceBundle standardPhrases) {
        setEmbedAndMenuFieldsForBlacklistEntries(removeActionEmbed, editMenu, 0, blacklistEntries, bundle);

        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("regexBlacklistConfigureButtonEvents_NextButton;%s;%d", userId, 25),
                standardPhrases.getString("buttons.next"));

        if (blacklistEntries.length() > 25) {
            event.getHook()
                    .editOriginalEmbeds(removeActionEmbed.build())
                    .setComponents(
                            ActionRow.of(editMenu.build()),
                            ActionRow.of(nextButton))
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds(removeActionEmbed.build())
                    .setComponents(ActionRow.of(editMenu.build()))
                    .queue();
        }
    }

    private void setEmbedAndMenuFieldsForBlacklistEntries(EmbedBuilder actionEmbed, StringSelectMenu.Builder menuBuilder, int countFrom, @NotNull JSONObject entries, ResourceBundle bundle) {
        int i = 0;
        for (String key : entries.keySet().stream().sorted().toList()) {
            countFrom--;
            if (countFrom < 0) {
                i++;
                actionEmbed.addField(String.format(
                        bundle.getString("configureEvents.regex.blacklist.setEmbedAndMenuFieldsForBlacklistEntries.actionEmbed.fieldPreset"),
                        i
                ), entries.getString(key), false);
                menuBuilder.addOption(String.valueOf(i), key);
                if (i == 25) break;
            }
        }
    }

    @JDAStringMenu(startWith = "regexBlacklistConfigureStringMenuEvents_")
    public void regexBlacklistConfigureStringMenuEvents(@NotNull StringSelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "regexBlacklistConfigureStringMenuEvents_EditAction" ->
                    regexBlacklistConfigureEditStringEvent(event, bundle);
            case "regexBlacklistConfigureStringMenuEvents_RemoveAction" ->
                    regexBlacklistConfigureRemoveStringEvent(event, bundle, standardPhrases);
        }
    }

    private void regexBlacklistConfigureRemoveStringEvent(@NotNull StringSelectInteractionEvent event, ResourceBundle bundle, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        long responseCode = controller.deletePhraseFromGuildTextBlacklist(Long.parseLong(event.getValues().getFirst()));

        String replyString;

        if (responseCode < 0) {
            replyString = String.format(
                    standardPhrases.getString("replies.dbErrorReply"),
                    responseCode);
        } else if (responseCode > 0) {
            replyString = bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureRemoveStringEvent.successfullyDeleted");
        } else {
            replyString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook()
                .editOriginal(replyString)
                .setEmbeds()
                .setComponents()
                .queue();
    }

    private void regexBlacklistConfigureEditStringEvent(@NotNull StringSelectInteractionEvent event, @NotNull ResourceBundle bundle) {
        TextInput.Builder phraseTextField = TextInput.create(
                "phraseTextField",
                bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureEditStringEvent.phraseEditModal.phraseTextField.title"),
                TextInputStyle.SHORT);
        phraseTextField.setRequiredRange(1, 100);
        phraseTextField.setRequired(true);
        phraseTextField.setPlaceholder(bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureEditStringEvent.phraseEditModal.phraseTextField.placeholder"));

        long id = Long.parseLong(event.getValues().getFirst());
        String value = controller.getSpecificBlacklistPhrase(id);
        if (value != null) phraseTextField.setValue(value);

        Modal.Builder phraseAddModal = Modal.create(
                String.format("regexBlacklistConfigureModalEvents_EditModal;%s;%d", event.getUser().getId(), id),
                bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureEditStringEvent.phraseEditModal.title"));

        phraseAddModal.addActionRow(phraseTextField.build());

        event.replyModal(phraseAddModal.build()).queue();
    }

    @JDAModal(startWith = "regexBlacklistConfigureModalEvents_")
    public void regexBlacklistConfigureModalEvents(@NotNull ModalInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getModalId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "regexBlacklistConfigureModalEvents_AddModal" ->
                    regexBlacklistConfigureAddModalEvent(event, bundle, guild, standardPhrases);
            case "regexBlacklistConfigureModalEvents_EditModal" ->
                    regexBlacklistConfigureEditModalEvent(event, bundle, splitComponentId, standardPhrases, guild);
        }
    }

    private void regexBlacklistConfigureEditModalEvent(@NotNull ModalInteractionEvent event, ResourceBundle bundle, String[] splitComponentId, ResourceBundle standardPhrases, Guild guild) {
        event.deferEdit().queue();
        ModalMapping phraseTextMapping = event.getValue("phraseTextField");
        if (phraseTextMapping == null) return;
        String phraseText = phraseTextMapping.getAsString();

        long responseCode = controller.updatePhraseFromGuildTextBlacklist(phraseText, Long.parseLong(splitComponentId[2]), guild);

        String replyText;

        if (responseCode > 0) {
            replyText = String.format(
                    bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureEditModalEvent.successfullyUpdatet"),
                    phraseText);
        } else if (responseCode < 0) {
            replyText = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else {
            replyText = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook()
                .editOriginal(replyText)
                .setComponents()
                .setEmbeds()
                .queue();
    }

    private void regexBlacklistConfigureAddModalEvent(@NotNull ModalInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        ModalMapping phraseTextMapping = event.getValue("phraseTextField");
        if (phraseTextMapping == null) return;
        String phraseText = phraseTextMapping.getAsString();

        long responseCode = controller.addPhraseToGuildTextBlacklist(phraseText, guild);

        String replyText;

        if (responseCode > 0) {
            replyText = String.format(
                    bundle.getString("configureEvents.regex.blacklist.regexBlacklistConfigureAddModalEvent.successfullyAdded"),
                    phraseText);
        } else if (responseCode < 0) {
            replyText = String.format(
                    standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else {
            replyText = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook()
                .editOriginal(replyText)
                .setComponents()
                .setEmbeds()
                .queue();
    }

    private void rolesPunishmentRolesConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, ResourceBundle standardPhrases, Guild guild) {
        event.deferReply(true).queue();
        Role warnRole = event.getOption("warn-role", OptionMapping::getAsRole);
        Role muteRole = event.getOption("mute-role", OptionMapping::getAsRole);
        boolean warnDisabled = event.getOption("warn-disabled", false, OptionMapping::getAsBoolean);
        boolean muteDisabled = event.getOption("mute-disabled", false, OptionMapping::getAsBoolean);

        String replyString;

        long warnRoleResponse = 0;
        long muteRoleResponse = 0;
        if (warnRole == null && muteRole == null) {
            replyString = bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.bothRolesAreNull");
        } else if (warnRole != null && muteRole != null) {
            warnRoleResponse = controller.setGuildWarnRole(warnRole);
            muteRoleResponse = controller.setGuildMuteRole(muteRole);
            replyString = String.format(
                    bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.bothRolesNonNull"),
                    warnRole.getAsMention(), muteRole.getAsMention());
        } else if (warnRole == null) {
            muteRoleResponse = controller.setGuildMuteRole(muteRole);
            replyString = String.format(
                    bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.warnRoleIsNull"),
                    muteRole.getAsMention());
        } else {
            warnRoleResponse = controller.setGuildWarnRole(warnRole);
            replyString = String.format(
                    bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.muteRoleIsNull"),
                    warnRole.getAsMention());
        }

        if (warnDisabled) {
            warnRoleResponse = controller.disableGuildWarnRole(guild);
        }
        if (muteDisabled) {
            muteRoleResponse = controller.disableGuildMuteRole(guild);
        }

        if (warnRoleResponse + muteRoleResponse < 0) {
            replyString = String.format(
                    bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.errorResponse"),
                    warnRoleResponse, muteRoleResponse);
        } else if (warnRoleResponse + muteRoleResponse == 0) {
            replyString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook().editOriginal(replyString).queue();
    }

    private void rolesModRolesConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, ResourceBundle standardPhrases) {
        event.deferReply(true).queue();
        Role addRolle = event.getOption("add", OptionMapping::getAsRole);
        Role removeRole = event.getOption("remove", OptionMapping::getAsRole);

        if (addRolle == null && removeRole == null) {
            EmbedBuilder overviewEmbed = new EmbedBuilder();
            overviewEmbed.setTitle(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.title"));
            overviewEmbed.setDescription(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.description"));

            overviewEmbed.addField(
                    standardPhrases.getString("embeds.fields.name.add"),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.addDescription.value"),
                    true);
            overviewEmbed.addField(
                    standardPhrases.getString("embeds.fields.name.remove"),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.removeDescription.value"),
                    true);
            overviewEmbed.addField(
                    standardPhrases.getString("embeds.fields.name.show"),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.showDescription.value"),
                    true);

            String userId = event.getUser().getId();

            Button addModRoleButton = Button.of(
                    ButtonStyle.SUCCESS,
                    String.format("rolesModRolesConfigureButtonEvents_AddRole;%s", userId),
                    standardPhrases.getString("buttons.add"));

            Button removeModRoleButton = Button.of(
                    ButtonStyle.SUCCESS,
                    String.format("rolesModRolesConfigureButtonEvents_RemoveRole;%s", userId),
                    standardPhrases.getString("buttons.remove"));

            Button showModRoleButton = Button.of(
                    ButtonStyle.SUCCESS,
                    String.format("rolesModRolesConfigureButtonEvents_ShowRole;%s", userId),
                    standardPhrases.getString("buttons.show"));

            event.getHook()
                    .editOriginalEmbeds(overviewEmbed.build())
                    .setActionRow(addModRoleButton, removeModRoleButton, showModRoleButton)
                    .queue();
            return;
        }

        sendModRoleConfigureResponse(event.getHook(), addRolle, removeRole, bundle, standardPhrases);
    }

    private void sendModRoleConfigureResponse(InteractionHook hook, Role addRole, Role removeRole, ResourceBundle bundle, ResourceBundle standardPhrases) {
        String responseString;

        long addResponse = 0;
        long removeResponse = 0;

        if (addRole != null && removeRole != null) {
            responseString = String.format(
                    bundle.getString("configureEvents.roles.modRoles.sendModRoleConfigureResponse.twoOptionMappings"),
                    addRole.getAsMention(), removeRole.getAsMention());
            addResponse = controller.addGuildModerationRole(addRole);
            removeResponse = controller.removeGuildModerationRole(removeRole);
        } else if (addRole == null) {
            responseString = String.format(
                    bundle.getString("configureEvents.roles.modRoles.sendModRoleConfigureResponse.addRoleIsNull"),
                    removeRole.getAsMention());
            removeResponse = controller.removeGuildModerationRole(removeRole);
        } else {
            responseString = String.format(
                    bundle.getString("configureEvents.roles.modRoles.sendModRoleConfigureResponse.removeRoleIsNull"),
                    addRole.getAsMention());
            addResponse = controller.addGuildModerationRole(addRole);
        }

        if (addResponse + removeResponse < 0) {
            responseString = String.format(
                    bundle.getString("configureEvents.roles.modRoles.sendModRoleConfigureResponse.errorToAddOrRemove"),
                    addResponse, removeResponse);
        } else if (addResponse + removeResponse == 0) {
            responseString = standardPhrases.getString("replies.noDataEdit");
        }

        hook.editOriginal(responseString)
                .setComponents()
                .setEmbeds()
                .queue();
    }

    @JDAButton(startWith = "rolesModRolesConfigureButtonEvents_")
    public void rolesModRolesConfigureButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "rolesModRolesConfigureButtonEvents_AddRole" ->
                    rolesModRolesConfigureAddRoleButtonEvent(event, bundle);
            case "rolesModRolesConfigureButtonEvents_RemoveRole" ->
                    rolesModRolesConfigureRemoveButtonEvent(event, bundle);
            case "rolesModRolesConfigureButtonEvents_ShowRole" ->
                    rolesModRolesConfigureShowButtonEvent(event, bundle, guild);
        }
    }

    private void rolesModRolesConfigureShowButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle, Guild guild) {
        event.deferEdit().queue();
        List<Long> roleIds = controller.getGuildModerationRoles(guild);

        EmbedBuilder showModRolesEmbed = new EmbedBuilder();
        showModRolesEmbed.setTitle(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureShowButtonEvent.showModRolesEmbed.title"));
        String description;
        if (!roleIds.isEmpty()) {
            StringBuilder descriptionStringBuilder = new StringBuilder();

            for (long id : roleIds) {
                Role role = guild.getRoleById(id);
                descriptionStringBuilder.append(role != null ? String.format("%s,", role.getName()) : String.format("<@%d> ,", id));
            }
            descriptionStringBuilder.deleteCharAt(descriptionStringBuilder.length() - 1);
            description = String.format(
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureShowButtonEvent.showModRolesEmbed.description"),
                    descriptionStringBuilder);
        } else {
            description = String.format(
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureShowButtonEvent.showModRolesEmbed.description"),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureShowButtonEvent.showModRolesEmbed.noRolesStored"));
        }

        showModRolesEmbed.setDescription(description);

        event.getHook()
                .editOriginalEmbeds(showModRolesEmbed.build())
                .setComponents()
                .queue();

    }

    private void rolesModRolesConfigureRemoveButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle) {
        event.deferEdit().queue();
        EmbedBuilder removeModRoleEmbed = new EmbedBuilder();
        removeModRoleEmbed.setTitle(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureAddRoleButtonEvent.addModRoleEmbed.title"));
        removeModRoleEmbed.setDescription(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureAddRoleButtonEvent.addModRoleEmbed.description"));

        EntitySelectMenu.Builder removeModRoleMenu = EntitySelectMenu.create(
                String.format("rolesModRolesConfigureEntityMenus_removeRoleMenu;%s", event.getUser().getId()),
                EntitySelectMenu.SelectTarget.ROLE);

        event.getHook()
                .editOriginalEmbeds(removeModRoleEmbed.build())
                .setActionRow(removeModRoleMenu.build())
                .queue();
    }

    private void rolesModRolesConfigureAddRoleButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle) {
        event.deferEdit().queue();
        EmbedBuilder addModRoleEmbed = new EmbedBuilder();
        addModRoleEmbed.setTitle(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureRemoveButtonEvent.removeModRoleEmbed.title"));
        addModRoleEmbed.setDescription(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureRemoveButtonEvent.removeModRoleEmbed.description"));

        EntitySelectMenu.Builder addModRoleMenu = EntitySelectMenu.create(
                String.format("rolesModRolesConfigureEntityMenus_addRoleMenu;%s", event.getUser().getId()),
                EntitySelectMenu.SelectTarget.ROLE);

        event.getHook()
                .editOriginalEmbeds(addModRoleEmbed.build())
                .setActionRow(addModRoleMenu.build())
                .queue();
    }

    @JDAEntityMenu(startWith = "rolesModRolesConfigureEntityMenus_")
    public void rolesModRolesConfigureEntityMenuEvents(@NotNull EntitySelectInteractionEvent event) {
        event.deferEdit().queue();
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "rolesModRolesConfigureEntityMenus_removeRoleMenu" ->
                    sendModRoleConfigureResponse(event.getHook(), null, event.getMentions().getRoles().getFirst(), bundle, standardPhrases);
            case "rolesModRolesConfigureEntityMenus_addRoleMenu" ->
                    sendModRoleConfigureResponse(event.getHook(), event.getMentions().getRoles().getFirst(), null, bundle, standardPhrases);
        }
    }

    private void channelArchiveCategoryConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull ResourceBundle standardPhrases) {
        EmbedBuilder manageArchiveCategoryEmbed = new EmbedBuilder();
        manageArchiveCategoryEmbed.setTitle(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.title"));
        manageArchiveCategoryEmbed.setDescription(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.description"));

        manageArchiveCategoryEmbed.addField(
                standardPhrases.getString("embeds.fields.name.edit"),
                bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.editCategory.value"),
                true);
        manageArchiveCategoryEmbed.addField(
                standardPhrases.getString("embeds.fields.name.clear"),
                bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.clear.value"),
                true);

        String userId = event.getUser().getId();

        Button editArchiveCategoryButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("channelArchiveCategoryConfigureButtons_editButton;%s", userId),
                standardPhrases.getString("buttons.edit"));
        Button clearArchiveCategoryButton = Button.of(
                ButtonStyle.DANGER,
                String.format("channelArchiveCategoryConfigureButtons_clearButton;%s", userId),
                standardPhrases.getString("buttons.clear"));

        ActionRow actionRow = ActionRow.of(editArchiveCategoryButton, clearArchiveCategoryButton);

        event.replyEmbeds(manageArchiveCategoryEmbed.build())
                .setComponents(actionRow)
                .setEphemeral(true)
                .queue();

    }

    @JDAButton(startWith = "channelArchiveCategoryConfigureButtons_")
    public void channelArchiveCategoryConfigureButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "channelArchiveCategoryConfigureButtons_editButton" ->
                    channelArchiveCategoryConfigureEditButtonEvent(event, bundle, guild);
            case "channelArchiveCategoryConfigureButtons_clearButton" ->
                    channelArchiveCategoryConfigureClearButtonEvent(event, bundle, guild);
        }
    }

    private void channelArchiveCategoryConfigureClearButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild) {
        event.deferEdit().queue();
        long value = controller.clearGuildArchiveCategory(guild);

        String contentString;

        if (value == 0) {
            contentString = bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureClearButtonEvent.valueEquals0");
        } else {
            contentString = String.format(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureClearButtonEvent.valueLower0"), value);
        }

        event.getHook()
                .editOriginalEmbeds()
                .setContent(contentString)
                .setComponents()
                .queue();
    }

    private void channelArchiveCategoryConfigureEditButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull Guild guild) {
        event.deferEdit().queue();
        EmbedBuilder channelArchiveEditEmbed = new EmbedBuilder();
        channelArchiveEditEmbed.setTitle(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureEditButtonEvent.embed.title"));
        channelArchiveEditEmbed.setDescription(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureEditButtonEvent.embed.description"));

        long archiveCategoryId = controller.getGuildArchiveCategory(guild);

        if (archiveCategoryId < 0) {
            event.reply(
                            String.format(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureEditButtonEvent.errorToGetCategory"),
                                    archiveCategoryId))
                    .queue();
            return;
        } else if (archiveCategoryId > 0) {

            Category archiveCategory = guild.getCategoryById(archiveCategoryId);

            if (archiveCategory != null) {
                channelArchiveEditEmbed.addField(
                        bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.name"),
                        String.format("<@%s>", archiveCategory.getId()),
                        true);
            } else {
                channelArchiveEditEmbed.addField(
                        bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.name"),
                        bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.valueIfCategoryNull"),
                        true);
            }
        } else {
            channelArchiveEditEmbed.addField(
                    bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.name"),
                    bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.valueIfCategoryNull"),
                    true);
        }

        EntitySelectMenu.Builder archiveCategoryPicker = EntitySelectMenu.create(
                String.format("channelArchiveConfigureMenus_archiveChannelPicker;%s", event.getUser().getId()),
                EntitySelectMenu.SelectTarget.CHANNEL);
        archiveCategoryPicker.setChannelTypes(ChannelType.CATEGORY);

        event.getHook()
                .editOriginalEmbeds(channelArchiveEditEmbed.build())
                .setActionRow(archiveCategoryPicker.build())
                .queue();
    }

    @JDAEntityMenu(startWith = "channelArchiveConfigureMenus_")
    public void channelArchiveConfigureMenuEvents(@NotNull EntitySelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        if (splitComponentId[0].equals("channelArchiveConfigureMenus_archiveChannelPicker")) {
            channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent(event, bundle, guild, standardPhrases(event.getUserLocale()));
        }
    }

    private void channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardBundle) {
        event.deferEdit().queue();

        GuildChannel category = event.getMentions().getChannels().getFirst();

        long value = controller.setGuildArchiveCategory(category, guild);

        String contentString;

        if (value > 0) {
            contentString = String.format(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent.valueEquals0"), category.getName());
        } else if (value == 0) {
            contentString = String.format(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent.valueLower0"), value);
        } else {
            contentString = String.format(standardBundle.getString("replies.dbErrorReply"), value);
        }
        event.getHook()
                .editOriginalEmbeds()
                .setContent(contentString)
                .setComponents()
                .queue();
    }

    private void channelLogChannelConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        String typeMapping = event.getOption("type", OptionMapping::getAsString);
        if (typeMapping == null) return;
        LogChannelType logChannelType = LogChannelType.getLogChannelTypeByName(typeMapping);
        if (logChannelType.equals(LogChannelType.UNKNOW)) {
            event.reply(bundle.getString("configureEvents.channel.logChannel.channelLogChannelConfigureSubcommand.logChannelTypeUnknown")).queue();
            return;
        }
        boolean toRemove = event.getOption("remove", false, OptionMapping::getAsBoolean);

        event.deferReply(true).queue();

        TextChannel channel = (TextChannel) event.getOption("target-channel", OptionMapping::getAsChannel);
        if (channel == null) {
            EmbedBuilder displaySpecificLogChannelEmbed = new EmbedBuilder();
            displaySpecificLogChannelEmbed.setTitle(
                    bundle.getString("configureEvents.channel.logChannel.channelLogChannelConfigureSubcommand.displaySpecificLogChannelEmbed.title"));

            long channelId = controller.getGuildLoggingChannel(logChannelType, guild);

            if (channelId == 0) {
                displaySpecificLogChannelEmbed.setDescription(
                        String.format(bundle.getString("configureEvents.channel.logChannel.channelLogChannelConfigureSubcommand.displaySpecificLogChannelEmbed.description.hasNoChannel"),
                                logChannelType.getName()));
            } else {
                displaySpecificLogChannelEmbed.setDescription(
                        String.format(bundle.getString("configureEvents.channel.logChannel.channelLogChannelConfigureSubcommand.displaySpecificLogChannelEmbed.description.hasChannel"),
                                logChannelType.getName(), channelId));
            }

            event.getHook().editOriginalEmbeds(displaySpecificLogChannelEmbed.build()).queue();
            return;
        }

        if (toRemove) {
            long value = controller.removeGuildLoggingChannel(logChannelType, channel);
            if (value < 0) {
                event.getHook().editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), value)).queue();
            } else if (value > 0) {
                channel.retrieveWebhooks()
                        .map(x -> x.stream().filter(y -> y.getName().equals(typeMapping)).toList())
                        .flatMap(x -> {
                            List<AuditableRestAction<Void>> webhooks = new ArrayList<>();
                            x.forEach(y -> webhooks.add(channel.deleteWebhookById(y.getId())));
                            return RestAction.allOf(webhooks);
                        })
                        .flatMap(x -> event.getHook().editOriginal(String.format(bundle.getString("configureEvents.channel.logChannel.channelLogChannelConfigureSubcommand.logChannelRemoved"))))
                        .queue();
            } else {
                event.getHook().editOriginal(standardPhrases.getString("replies.noDataEdit")).queue();
            }
            return;
        }

        long existChannelId = controller.getGuildLoggingChannel(logChannelType, guild);

        if (existChannelId > 0) {
            if (existChannelId == channel.getIdLong()) {
                event.getHook().editOriginal(String.format(standardPhrases.getString("replies.noDataEdit"))).queue();
            } else {
                channel.createWebhook(typeMapping)
                        .map(x -> controller.setGuildLoggingChannel(logChannelType, channel, guild, x.getUrl()))
                        .flatMap(x -> {
                            if (x > 0) {
                                return event.getHook().editOriginal(String.format(bundle.getString("configureEvents.channel.logChannel.channelLogChannelConfigureSubcommand.logChannelSet"),
                                        channel.getId(), logChannelType.getName()));
                            } else if (x < 0) {
                                return event.getHook().editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), x));
                            } else {
                                return event.getHook().editOriginal(standardPhrases.getString("replies.noDataEdit"));
                            }
                        })
                        .queue();
            }
        } else if (existChannelId < 0) {
            event.getHook().editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), existChannelId)).queue();
        } else {
            channel.createWebhook(typeMapping)
                    .map(x -> controller.setGuildLoggingChannel(logChannelType, channel, guild, x.getUrl()))
                    .flatMap(x -> {
                        if (x > 0) {
                            return event.getHook().editOriginal(String.format(bundle.getString("configureEvents.channel.logChannel.channelLogChannelConfigureSubcommand.logChannelSet"),
                                    channel.getId(), logChannelType.getName()));
                        } else if (x < 0) {
                            return event.getHook().editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), x));
                        } else {
                            return event.getHook().editOriginal(standardPhrases.getString("replies.noDataEdit"));
                        }
                    })
                    .queue();
        }
    }

    private void channelMediaOnlyChannelConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull ResourceBundle standardPhrases) {
        event.deferReply(true).queue();
        String userId = event.getUser().getId();
        EmbedBuilder actionEmbed = new EmbedBuilder();
        actionEmbed.setTitle(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.title"));
        actionEmbed.setDescription(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.description"));
        actionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.add"),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.addDescription.value"),
                true);
        actionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.edit"),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.editDescription.value"),
                true);
        actionEmbed.addField(
                standardPhrases.getString("embeds.fields.name.remove"),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.removeDescription.value"),
                true);

        Button addMediaOnlyChannelButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("channelMediaOnlyConfigureButtons_AddButton;%s,%d", userId, 0),
                standardPhrases.getString("buttons.add"));
        Button editMediaOnlyChannelButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_editButton;%s,%d", userId, 0),
                standardPhrases.getString("buttons.edit"));
        Button removeMediaOnlyChannelButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_RemoveButton;%s,%d", userId, 0),
                standardPhrases.getString("buttons.remove"));

        ActionRow actionEmbedActionRow = ActionRow.of(
                addMediaOnlyChannelButton,
                editMediaOnlyChannelButton,
                removeMediaOnlyChannelButton
        );

        event.getHook()
                .editOriginalEmbeds(actionEmbed.build())
                .setComponents(actionEmbedActionRow)
                .queue();
    }

    @JDAButton(startWith = "channelMediaOnlyConfigureButtons_")
    public void channelMediaOnlyChannelConfigureButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();

        String[] dataIds = splitComponentId[1].split(",");

        if (!userId.equals(dataIds[0])) return;


        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "channelMediaOnlyConfigureButtons_AddButton" -> channelMediaOnlyChannelAddButtonEvent(event, bundle);
            case "channelMediaOnlyConfigureButtons_editButton" -> {
                int count = Integer.parseInt(dataIds[1]);
                channelMediaOnlyChannelEditButtonEvent(event, bundle, guild, count);
            }
            case "channelMediaOnlyConfigureButtons_RemoveButton" -> {
                int count = Integer.parseInt(dataIds[1]);
                channelMediaOnlyChannelRemoveButtonEvent(event, bundle, guild, count);
            }
            case "channelMediaOnlyConfigureButtons_NextButton" -> {
                int count = Integer.parseInt(dataIds[1]);
                channelMediaOnlyChannelNextButtonEvent(event, bundle, guild, count, dataIds[2]);
            }
            case "channelMediaOnlyConfigureButtons_BeforeButton" -> {
                int count = Integer.parseInt(dataIds[1]);
                channelMediaOnlyChannelBeforeButtonEvent(event, bundle, guild, count, dataIds[2]);
            }
            case "channelMediaOnlyConfigureButtons_TextP",
                    "channelMediaOnlyConfigureButtons_AttachP",
                    "channelMediaOnlyConfigureButtons_fileP",
                    "channelMediaOnlyConfigureButtons_linkP" ->
                    channelMediaOnlyChangePermissionEvent(event, bundle, guild, splitComponentId, standardPhrases);
            case "channelMediaOnlyConfigureButtons_save" ->
                    channelMediaOnlyChannelSaveButtonEvent(event, bundle, guild, splitComponentId, standardPhrases);
        }
    }

    private void channelMediaOnlyChannelSaveButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String[] splitComponentId, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        List<MediaOnlyPermissions> permissions = getMediaOnlyPermissionsFromButtonId(splitComponentId);
        long channelId = Long.parseLong(splitComponentId[1].split(",")[1]);
        long returnValue = controller.addOrEditGuildMediaOnlyChannel(channelId, guild, permissions);

        String responseString;

        if (returnValue < 0) {
            responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), returnValue);
        } else if (returnValue > 0) {
            responseString = bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelSaveButtonEvent.success");
        } else {
            responseString = bundle.getString(standardPhrases.getString("replies.noDataEdit"));
        }

        event.getHook()
                .editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void channelMediaOnlyChangePermissionEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String @NotNull [] splitComponentId, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        long channelId = Long.parseLong(splitComponentId[1].split(",")[1]);

        List<MediaOnlyPermissions> permissions = getMediaOnlyPermissionsFromButtonId(splitComponentId);

        sendSpecificMediaOnlyChannelEmbed(event.getHook(), channelId, bundle, permissions, guild, standardPhrases);
    }

    private @NotNull List<MediaOnlyPermissions> getMediaOnlyPermissionsFromButtonId(String @NotNull [] splitComponentId) {
        List<MediaOnlyPermissions> permissions = new ArrayList<>();

        String[] splitPermissions = splitComponentId[2].split(",");

        if (splitPermissions[0].equals("1")) {
            permissions.add(MediaOnlyPermissions.TEXT);
        }
        if (splitPermissions[1].equals("1")) {
            permissions.add(MediaOnlyPermissions.ATTACHMENT);
        }
        if (splitPermissions[2].equals("1")) {
            permissions.add(MediaOnlyPermissions.FILES);
        }
        if (splitPermissions[3].equals("1")) {
            permissions.add(MediaOnlyPermissions.LINKS);
        }

        switch (splitComponentId[0]) {
            case "channelMediaOnlyConfigureButtons_TextP" -> {
                if (splitPermissions[0].equals("1")) permissions.remove(MediaOnlyPermissions.TEXT);
                else permissions.add(MediaOnlyPermissions.TEXT);
            }
            case "channelMediaOnlyConfigureButtons_AttachP" -> {
                if (splitPermissions[1].equals("1")) permissions.remove(MediaOnlyPermissions.ATTACHMENT);
                else permissions.add(MediaOnlyPermissions.ATTACHMENT);
            }
            case "channelMediaOnlyConfigureButtons_fileP" -> {
                if (splitPermissions[2].equals("1")) permissions.remove(MediaOnlyPermissions.FILES);
                else permissions.add(MediaOnlyPermissions.FILES);
            }
            case "channelMediaOnlyConfigureButtons_linkP" -> {
                if (splitPermissions[3].equals("1")) permissions.remove(MediaOnlyPermissions.LINKS);
                else permissions.add(MediaOnlyPermissions.LINKS);
            }
        }

        return permissions;
    }

    private void channelMediaOnlyChannelBeforeButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, int count, @NotNull String action) {
        event.deferEdit().queue();
        EmbedBuilder beforeMediaOnlyChannelEmbed = new EmbedBuilder(event.getMessage().getEmbeds().getFirst());
        beforeMediaOnlyChannelEmbed.clearFields();
        StringSelectMenu.Builder beforeMediaOnlyChannelMenu;
        if (action.equals("edit")) {
            beforeMediaOnlyChannelMenu = StringSelectMenu.create(String.format("channelMediaOnlyConfigureStringMenus_EditMenu;%s", event.getUser().getId()));
        } else if (action.equals("remove")) {
            beforeMediaOnlyChannelMenu = StringSelectMenu.create(String.format("channelMediaOnlyConfigureStringMenus_RemoveMenu;%s", event.getUser().getId()));
        } else {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelBeforeButtonEvent.actionNotPassed"))
                    .queue();
            return;
        }

        JSONObject guildMediaOnlyChannels = controller.getGuildMediaOnlyChannels(guild);
        setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus(beforeMediaOnlyChannelEmbed, beforeMediaOnlyChannelMenu, count, guild, guildMediaOnlyChannels, bundle);

        ActionRow firstRow = ActionRow.of(beforeMediaOnlyChannelMenu.build());
        ActionRow secondRow;

        Button beforeButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_BeforeButton;%s,%d,edit",
                        event.getUser().getId(),
                        count - 25),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelBeforeButtonEvent.buttons.beforeButton"));
        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_NextButton;%s,%d,edit",
                        event.getUser().getId(),
                        Math.min(guildMediaOnlyChannels.keySet().size() - count, 25)),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelBeforeButtonEvent.buttons.nextButton"));


        if (count - 25 > 0) {
            secondRow = ActionRow.of(beforeButton, nextButton);
        } else {
            secondRow = ActionRow.of(nextButton);
        }

        event.getHook()
                .editOriginalEmbeds(beforeMediaOnlyChannelEmbed.build())
                .setComponents(firstRow, secondRow)
                .queue();
    }

    private void channelMediaOnlyChannelNextButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, int count, @NotNull String action) {
        event.deferEdit().queue();
        EmbedBuilder nextMediaOnlyChannelEmbed = new EmbedBuilder(event.getMessage().getEmbeds().getFirst());
        nextMediaOnlyChannelEmbed.clearFields();
        StringSelectMenu.Builder nextMediaOnlyChannelMenu;
        if (action.equals("edit")) {
            nextMediaOnlyChannelMenu = StringSelectMenu.create(String.format("channelMediaOnlyConfigureStringMenus_EditMenu;%s", event.getUser().getId()));
        } else if (action.equals("remove")) {
            nextMediaOnlyChannelMenu = StringSelectMenu.create(String.format("channelMediaOnlyConfigureStringMenus_RemoveMenu;%s", event.getUser().getId()));
        } else {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelNextButtonEvent.actionNotPassed"))
                    .queue();
            return;
        }

        JSONObject guildMediaOnlyChannels = controller.getGuildMediaOnlyChannels(guild);
        setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus(nextMediaOnlyChannelEmbed, nextMediaOnlyChannelMenu, count, guild, guildMediaOnlyChannels, bundle);

        ActionRow firstRow = ActionRow.of(nextMediaOnlyChannelMenu.build());
        ActionRow secondRow;

        Button beforeButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_BeforeButton;%s,%d,edit",
                        event.getUser().getId(),
                        count - 25),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelNextButtonEvent.buttons.beforeButton"));
        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_NextButton;%s,%d,edit",
                        event.getUser().getId(),
                        Math.min(guildMediaOnlyChannels.keySet().size() - count, 25)),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelNextButtonEvent.buttons.nextButton"));


        if (guildMediaOnlyChannels.length() > 25) {
            secondRow = ActionRow.of(beforeButton, nextButton);
        } else {
            secondRow = ActionRow.of(beforeButton);
        }

        event.getHook()
                .editOriginalEmbeds(nextMediaOnlyChannelEmbed.build())
                .setComponents(firstRow, secondRow)
                .queue();
    }

    private void channelMediaOnlyChannelAddButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle) {
        event.deferEdit().queue();
        EmbedBuilder addMediaOnlyChannelEmbed = new EmbedBuilder();
        addMediaOnlyChannelEmbed.setTitle(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelAddButtonEvent.addMediaOnlyChannelEmbed.title"));
        addMediaOnlyChannelEmbed.setDescription(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelAddButtonEvent.addMediaOnlyChannelEmbed.description"));


        EntitySelectMenu.Builder addMediaOnlyChannelMenu = EntitySelectMenu.create(
                String.format("channelMediaOnlyConfigureEntityMenus_AddMenu;%s", event.getUser().getId()),
                EntitySelectMenu.SelectTarget.CHANNEL);
        addMediaOnlyChannelMenu.setChannelTypes(ChannelType.TEXT, ChannelType.VOICE);

        event.getHook()
                .editOriginalEmbeds(addMediaOnlyChannelEmbed.build())
                .setActionRow(addMediaOnlyChannelMenu.build())
                .queue();
    }

    private void channelMediaOnlyChannelEditButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle, Guild guild, int count) {
        event.deferEdit().queue();
        EmbedBuilder editMediaOnlyChannelEmbed = new EmbedBuilder();
        editMediaOnlyChannelEmbed.setTitle(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelEditButtonEvent.editMediaOnlyChannelEmbed.title"));
        editMediaOnlyChannelEmbed.setDescription(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelEditButtonEvent.editMediaOnlyChannelEmbed.description"));

        JSONObject guildMediaOnlyChannels = controller.getGuildMediaOnlyChannels(guild);

        StringSelectMenu.Builder editMediaOnlyChannelsMenu = StringSelectMenu.create(String.format("channelMediaOnlyConfigureStringMenus_EditMenu;%s", event.getUser().getId()));

        setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus(editMediaOnlyChannelEmbed, editMediaOnlyChannelsMenu, count, guild, guildMediaOnlyChannels, bundle);

        if (guildMediaOnlyChannels.keySet().isEmpty()) {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelEditButtonEvent.noChannelsStored"))
                    .queue();
            return;
        }

        ActionRow firstRow = ActionRow.of(editMediaOnlyChannelsMenu.build());

        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_NextButton;%s,%d,edit",
                        event.getUser().getId(),
                        Math.min(guildMediaOnlyChannels.keySet().size() - count, 25)),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelEditButtonEvent.nextButton"));

        sendMediaOnlyChannelActionEmbed(event, editMediaOnlyChannelEmbed, guildMediaOnlyChannels.length(), firstRow, nextButton);
    }

    private void channelMediaOnlyChannelRemoveButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle, Guild guild, int count) {
        event.deferEdit().queue();
        EmbedBuilder removeMediaOnlyChannelEmbed = new EmbedBuilder();
        removeMediaOnlyChannelEmbed.setTitle(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelRemoveButtonEvent.removeMediaOnlyChannelEmbed.title"));
        removeMediaOnlyChannelEmbed.setDescription(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelRemoveButtonEvent.removeMediaOnlyChannelEmbed.description"));

        JSONObject guildMediaOnlyChannels = controller.getGuildMediaOnlyChannels(guild);

        StringSelectMenu.Builder removeMediaOnlyChannelsMenu = StringSelectMenu.create(String.format("channelMediaOnlyConfigureStringMenus_RemoveMenu;%s", event.getUser().getId()));

        setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus(removeMediaOnlyChannelEmbed, removeMediaOnlyChannelsMenu, count, guild, guildMediaOnlyChannels, bundle);

        if (guildMediaOnlyChannels.keySet().isEmpty()) {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelRemoveButtonEvent.noChannelsStored"))
                    .queue();
            return;
        }

        ActionRow firstRow = ActionRow.of(removeMediaOnlyChannelsMenu.build());

        Button nextButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_NextButton;%s,%d,edit",
                        event.getUser().getId(),
                        Math.min(guildMediaOnlyChannels.keySet().size() - count, 25)),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelRemoveButtonEvent.nextButton"));

        sendMediaOnlyChannelActionEmbed(event, removeMediaOnlyChannelEmbed, guildMediaOnlyChannels.length(), firstRow, nextButton);
    }

    private void sendMediaOnlyChannelActionEmbed(@NotNull ButtonInteractionEvent event, EmbedBuilder mediaOnlyChannelActionEmbed, int guildMediaOnlyChannelsLength, ActionRow firstRow, Button nextButton) {
        InteractionHook hook = event.getHook();
        if (guildMediaOnlyChannelsLength > 25) {
            ActionRow secondRow = ActionRow.of(nextButton);
            hook.editOriginalEmbeds(mediaOnlyChannelActionEmbed.build())
                    .setComponents(firstRow, secondRow)
                    .queue();
        } else {
            hook.editOriginalEmbeds(mediaOnlyChannelActionEmbed.build())
                    .setComponents(firstRow)
                    .queue();
        }
    }

    private void setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus(EmbedBuilder actionEmbed, StringSelectMenu.Builder menuBuilder, int countFrom, Guild guild, @NotNull JSONObject mediaOnlyChannels, ResourceBundle bundle) {
        int i = 0;
        for (String key : mediaOnlyChannels.keySet().stream().sorted().toList()) {
            countFrom--;
            if (countFrom < 0) {
                i++;
                GuildChannel channel = guild.getGuildChannelById(key);
                JSONObject entry = mediaOnlyChannels.getJSONObject(key);
                actionEmbed.addField(
                        channel != null ? channel.getName() : String.format("ID: %s", key),
                        String.format("""
                                        %s: %s
                                        %s: %s
                                        %s: %s
                                        %s: %s
                                        """,
                                bundle.getString("configureEvents.channel.mediaOnlyChannel.setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus.attachmentPerms"),
                                entry.getBoolean("permAttachment"),
                                bundle.getString("configureEvents.channel.mediaOnlyChannel.setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus.filePerms"),
                                entry.getBoolean("permFiles"),
                                bundle.getString("configureEvents.channel.mediaOnlyChannel.setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus.linkPerms"),
                                entry.getBoolean("permLinks"),
                                bundle.getString("configureEvents.channel.mediaOnlyChannel.setEmbedAndMenuFieldsForMediaOnlyChannelEmbedsAndMenus.textPerms"),
                                entry.getBoolean("permText")
                        ),
                        false);
                menuBuilder.addOption(String.format("<@%s>", key), key);
                if (i == 25) break;
            }
        }
    }

    @JDAEntityMenu(startWith = "channelMediaOnlyConfigureEntityMenus_")
    public void channelMediaOnlyChannelConfigureEntityMenuEvents(@NotNull EntitySelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        if (splitComponentId[0].equals("channelMediaOnlyConfigureEntityMenus_AddMenu")) {
            channelMediaOnlyChannelAddMenuEvent(event, bundle, guild, standardPhrases);
        }
    }

    private void channelMediaOnlyChannelAddMenuEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        long channelId = event.getMentions().getChannels().getFirst().getIdLong();

        sendSpecificMediaOnlyChannelEmbed(event.getHook(), channelId, bundle, new ArrayList<>(), guild, standardPhrases);
    }

    @JDAStringMenu(startWith = "channelMediaOnlyConfigureStringMenus_")
    public void channelMediaOnlyChannelConfigureStringMenuEvents(@NotNull StringSelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "channelMediaOnlyConfigureStringMenus_EditMenu" ->
                    channelMediaOnlyChannelEditMenuEvent(event, bundle, guild, standardPhrases);
            case "channelMediaOnlyConfigureStringMenus_RemoveMenu" ->
                    channelMediaOnlyChannelRemoveEditMenuEvent(event, bundle, standardPhrases);
        }
    }

    private void channelMediaOnlyChannelRemoveEditMenuEvent(@NotNull StringSelectInteractionEvent event, ResourceBundle bundle, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        long channelId = Long.parseLong(event.getValues().getFirst());

        long response = controller.removeGuildMediaOnlyChannel(channelId);

        String responseString;

        if (response < 0) {
            responseString = String.format(standardPhrases.getString("replies.dbErrorReply"), response);
        } else if (response > 0) {
            responseString = bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelRemoveEditMenuEvent.success");
        } else {
            responseString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook()
                .editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void channelMediaOnlyChannelEditMenuEvent(@NotNull StringSelectInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        long channelId = Long.parseLong(event.getValues().getFirst());
        JSONObject storedPermissions = controller.getGuildMediaOnlyChannelPermissions(channelId);
        List<MediaOnlyPermissions> permissions = new ArrayList<>();
        if (storedPermissions.getBoolean("permText")) {
            permissions.add(MediaOnlyPermissions.TEXT);
        }
        if (storedPermissions.getBoolean("permAttachment")) {
            permissions.add(MediaOnlyPermissions.ATTACHMENT);
        }
        if (storedPermissions.getBoolean("permFiles")) {
            permissions.add(MediaOnlyPermissions.FILES);
        }
        if (storedPermissions.getBoolean("permLinks")) {
            permissions.add(MediaOnlyPermissions.LINKS);
        }

        sendSpecificMediaOnlyChannelEmbed(event.getHook(), channelId, bundle, permissions, guild, standardPhrases);
    }

    private void sendSpecificMediaOnlyChannelEmbed(@NotNull InteractionHook hook, long channelId, @NotNull ResourceBundle bundle, @NotNull List<MediaOnlyPermissions> permissions, @NotNull Guild guild, @NotNull ResourceBundle standardPhrases) {
        EmbedBuilder specificMediaOnlyChannelEmbed = new EmbedBuilder();
        GuildChannel channel = guild.getGuildChannelById(channelId);
        specificMediaOnlyChannelEmbed.setTitle(bundle.getString("configureEvents.channel.mediaOnlyChannel.sendSpecificMediaOnlyChannelEmbed.specificMediaOnlyChannelEmbed.title"));
        specificMediaOnlyChannelEmbed.setDescription(
                String.format(
                        bundle.getString("configureEvents.channel.mediaOnlyChannel.sendSpecificMediaOnlyChannelEmbed.specificMediaOnlyChannelEmbed.description"),
                        channel != null ? channel.getName() : channelId)
        );

        boolean textPerms = permissions.contains(MediaOnlyPermissions.TEXT);
        boolean attachmentPerms = permissions.contains(MediaOnlyPermissions.ATTACHMENT);
        boolean filePerms = permissions.contains(MediaOnlyPermissions.FILES);
        boolean linkPerms = permissions.contains(MediaOnlyPermissions.LINKS);

        String userId = hook.getInteraction().getUser().getId();

        Button mediaOnlyTextPermsButton = Button.of(
                textPerms ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                String.format("channelMediaOnlyConfigureButtons_TextP;%s,%d;%d,%d,%d,%d",
                        userId, channelId,
                        textPerms ? 1 : 0,
                        attachmentPerms ? 1 : 0,
                        filePerms ? 1 : 0,
                        linkPerms ? 1 : 0),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.sendSpecificMediaOnlyChannelEmbed.buttons.mediaOnlyTextPermsButton"));

        Button mediaOnlyAttachmentPermsButton = Button.of(
                attachmentPerms ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                String.format("channelMediaOnlyConfigureButtons_AttachP;%s,%d;%d,%d,%d,%d",
                        userId, channelId,
                        textPerms ? 1 : 0,
                        attachmentPerms ? 1 : 0,
                        filePerms ? 1 : 0,
                        linkPerms ? 1 : 0),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.sendSpecificMediaOnlyChannelEmbed.buttons.mediaOnlyAttachmentPermsButton"));

        Button mediaOnlyFilePermsButton = Button.of(
                filePerms ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                String.format("channelMediaOnlyConfigureButtons_fileP;%s,%d;%d,%d,%d,%d",
                        userId, channelId,
                        textPerms ? 1 : 0,
                        attachmentPerms ? 1 : 0,
                        filePerms ? 1 : 0,
                        linkPerms ? 1 : 0),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.sendSpecificMediaOnlyChannelEmbed.buttons.mediaOnlyFilePermsButton"));

        Button mediaOnlyLinkPermsButton = Button.of(
                linkPerms ? ButtonStyle.SUCCESS : ButtonStyle.DANGER,
                String.format("channelMediaOnlyConfigureButtons_linkP;%s,%d;%d,%d,%d,%d",
                        userId, channelId,
                        textPerms ? 1 : 0,
                        attachmentPerms ? 1 : 0,
                        filePerms ? 1 : 0,
                        linkPerms ? 1 : 0),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.sendSpecificMediaOnlyChannelEmbed.buttons.mediaOnlyLinkPermsButton"));

        ActionRow editButtons = ActionRow.of(
                mediaOnlyTextPermsButton,
                mediaOnlyAttachmentPermsButton,
                mediaOnlyFilePermsButton,
                mediaOnlyLinkPermsButton);

        Button okButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("channelMediaOnlyConfigureButtons_save;%s,%d;%d,%d,%d,%d",
                        userId, channelId,
                        textPerms ? 1 : 0,
                        attachmentPerms ? 1 : 0,
                        filePerms ? 1 : 0,
                        linkPerms ? 1 : 0),
                standardPhrases.getString("buttons.save"));

        ActionRow secondRow = ActionRow.of(okButton);

        hook.editOriginalEmbeds(specificMediaOnlyChannelEmbed.build())
                .setComponents(editButtons, secondRow)
                .queue();
    }

    private void channelFeedbackChannelConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle, @NotNull ResourceBundle standardPhrases) {
        event.deferReply(true).queue();

        EmbedBuilder overviewEmbed = new EmbedBuilder();
        overviewEmbed.setTitle(bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackChannelConfigureSubcommand.overviewEmbed.title"));
        overviewEmbed.setDescription(bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackChannelConfigureSubcommand.overviewEmbed.description"));

        overviewEmbed.addField(
                bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackChannelConfigureSubcommand.overviewEmbed.fields.set.name"),
                bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackChannelConfigureSubcommand.overviewEmbed.fields.set.value"),
                true);
        overviewEmbed.addField(
                standardPhrases.getString("embeds.fields.name.remove"),
                bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackChannelConfigureSubcommand.overviewEmbed.fields.remove.value"),
                true);

        String userId = event.getUser().getId();
        Button setCategoryButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("channelFeedbackCategoryConfigureButtonEvents_Set;%s", userId),
                bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackChannelConfigureSubcommand.buttons.setCategoryButton"));
        Button removeCategoryButton = Button.of(
                ButtonStyle.DANGER,
                String.format("channelFeedbackCategoryConfigureButtonEvents_Remove;%s", userId),
                standardPhrases.getString("buttons.remove"));

        event.getHook()
                .editOriginalEmbeds(overviewEmbed.build())
                .setActionRow(setCategoryButton, removeCategoryButton)
                .queue();
    }

    @JDAButton(startWith = "channelFeedbackCategoryConfigureButtonEvents_")
    public void channelFeedbackChannelConfigureButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "channelFeedbackCategoryConfigureButtonEvents_Set" ->
                    channelFeedbackChannelConfigureSetButtonEvent(event, bundle);
            case "channelFeedbackCategoryConfigureButtonEvents_Remove" ->
                    channelFeedbackChannelConfigureRemoveButtonEvent(event, bundle, guild, standardPhrases);
        }
    }

    private void channelFeedbackChannelConfigureRemoveButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, ResourceBundle standardPhrases) {
        event.deferEdit().queue();
        long responseCode = controller.removeGuildFeedbackChannel(guild);

        String replyString;
        if (responseCode < 0) {
            replyString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else if (responseCode > 0) {
            replyString = bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackChannelConfigureRemoveButtonEvent.successReply");
        } else {
            replyString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(replyString)
                .queue();
    }

    private void channelFeedbackChannelConfigureSetButtonEvent(@NotNull ButtonInteractionEvent event, @NotNull ResourceBundle bundle) {
        event.deferEdit().queue();

        EmbedBuilder setFeedbackCategoryEmbed = new EmbedBuilder();
        setFeedbackCategoryEmbed.setTitle(bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackCategoryChannelSetButtonEvent.setFeedbackCategoryEmbed.title"));
        setFeedbackCategoryEmbed.setDescription(bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackCategoryChannelSetButtonEvent.setFeedbackCategoryEmbed.description"));

        EntitySelectMenu.Builder feedbackCategorySelect = EntitySelectMenu.create(
                String.format("channelFeedbackCategoryConfigureEntityMenuEvents_set;%s", event.getUser().getId()),
                EntitySelectMenu.SelectTarget.CHANNEL);
        feedbackCategorySelect.setChannelTypes(ChannelType.TEXT);

        event.getHook().editOriginalEmbeds(setFeedbackCategoryEmbed.build())
                .setActionRow(feedbackCategorySelect.build())
                .queue();
    }

    @JDAEntityMenu(startWith = "channelFeedbackCategoryConfigureEntityMenuEvents_")
    public void channelFeedbackChannelConfigureEntityMenuEvents(@NotNull EntitySelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        if (splitComponentId[0].equals("channelFeedbackCategoryConfigureEntityMenuEvents_set")) {
            channelFeedbackChannelConfigureSetEntityEvent(event, bundle, standardPhrases);
        }
    }

    private void channelFeedbackChannelConfigureSetEntityEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, ResourceBundle standardPhrases) {
        event.deferEdit().queue();

        long responseCode = controller.setGuildFeedbackChannel(event.getMentions().getChannels().getFirst());
        String replyString;

        if (responseCode < 0) {
            replyString = String.format(standardPhrases.getString("replies.dbErrorReply"), responseCode);
        } else if (responseCode > 0) {
            replyString = bundle.getString("configureEvents.channel.feedback-channel.channelFeedbackChannelConfigureSetEntityEvent.successReply");
        } else {
            replyString = standardPhrases.getString("replies.noDataEdit");
        }

        event.getHook().editOriginalEmbeds()
                .setComponents()
                .setContent(replyString)
                .queue();
    }
}
