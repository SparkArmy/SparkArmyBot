package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.annotations.interactions.*;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.jda.utils.MediaOnlyPermissions;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
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
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ConfigureSlashCommandEvents {

    private final ConfigController controller;

    public ConfigureSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
    }

    private ResourceBundle bundle(DiscordLocale locale) {
        return Util.getResourceBundle("configure", locale);
    }

    @JDASlashCommand(name = "configure")
    public void configureInitialSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String subcommandGroupName = event.getSubcommandGroup();
        String subcommandName = event.getSubcommandName();

        if (subcommandGroupName == null || subcommandName == null) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        switch (subcommandGroupName) {
            case "channel" -> {
                switch (subcommandName) {
                    case "log-channels" -> channelLogchannelConfigureSubcommand(event, bundle, guild);
                    case "media-only-channel" -> channelMediaOnlyChannelConfigureSubcommand(event, bundle, guild);
                    case "archive-category" -> channelArchiveCategoryConfigureSubcommand(event, bundle);
                }
            }
            case "roles" -> {
                switch (subcommandName) {
                    case "mod-roles" -> rolesModRolesConfigureSubcommand(event, bundle, guild);
                    case "punishment-roles" -> rolesPunishmentRolesConfigureSubcommand(event, bundle, guild);
                }
            }
            case "regex" -> {
                switch (subcommandName) {
                    case "blacklist" -> regexBlacklistConfigureSubcommand(event);
                    case "manage" -> regexManageConfigureSubcommand(event);
                }
            }
            case "modmail" -> {
                switch (subcommandName) {
                    case "category" -> modmailCategoryConfigureSubcommand(event);
                    case "roles" -> modmailRolesConfigureSubcommand(event);
                    case "archive-settings" -> modmailArchiveSettingsConfigureSubcommand(event);
                    case "blacklist" -> modmailBlacklistConfigureSubcommand(event);
                    case "ping-roles" -> modmailPingRolesConfigureSubcommand(event);
                }
            }
            case "feedback" -> {
                switch (subcommandName) {
                    case "category" -> feedbackCategoryConfigureSubcommand(event);
                    case "roles" -> feedbackRolesConfigureSubcommand(event);
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

    private void feedbackRolesConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void feedbackCategoryConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void modmailPingRolesConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void modmailBlacklistConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void modmailArchiveSettingsConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void modmailRolesConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void modmailCategoryConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void regexManageConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void regexBlacklistConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void rolesPunishmentRolesConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, Guild guild) {
        event.deferReply(true).queue();
        Role warnRole = event.getOption("warn-role", OptionMapping::getAsRole);
        Role muteRole = event.getOption("mute-role", OptionMapping::getAsRole);

        String replyString;

        long warnRoleResponse = 0;
        long muteRoleResponse = 0;
        if (warnRole == null && muteRole == null) {
            replyString = bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.bothRolesAreNull");
        } else if (warnRole != null && muteRole != null) {
            warnRoleResponse = controller.setGuildWarnRole(warnRole, guild);
            muteRoleResponse = controller.setGuildMuteRole(muteRole, guild);
            replyString = String.format(
                    bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.bothRolesNonNull"),
                    warnRole.getAsMention(), muteRole.getAsMention());
        } else if (warnRole == null) {
            muteRoleResponse = controller.setGuildMuteRole(muteRole, guild);
            replyString = String.format(
                    bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.warnRoleIsNull"),
                    muteRole.getAsMention());
        } else {
            warnRoleResponse = controller.setGuildWarnRole(warnRole, guild);
            replyString = String.format(
                    bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.muteRoleIsNull"),
                    warnRole.getAsMention());
        }

        if (warnRoleResponse + muteRoleResponse != 0) {
            replyString = String.format(
                    bundle.getString("configureEvents.roles.punishmentRoles.rolesPunishmentRolesConfigureSubcommand.errorResponse"),
                    warnRoleResponse, muteRoleResponse);
        }

        event.getHook().editOriginal(replyString).queue();
    }

    private void rolesModRolesConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, Guild guild) {
        event.deferReply(true).queue();
        Role addRolle = event.getOption("add", OptionMapping::getAsRole);
        Role removeRole = event.getOption("remove", OptionMapping::getAsRole);

        if (addRolle == null && removeRole == null) {
            EmbedBuilder overviewEmbed = new EmbedBuilder();
            overviewEmbed.setTitle(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.title"));
            overviewEmbed.setDescription(bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.description"));

            overviewEmbed.addField(
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.addDescription.name"),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.addDescription.value"),
                    true);
            overviewEmbed.addField(
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.removeDescription.name"),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.removeDescription.value"),
                    true);
            overviewEmbed.addField(
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.showDescription.name"),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.overviewEmbed.fields.showDescription.value"),
                    true);

            String userId = event.getUser().getId();

            Button addModRoleButton = Button.of(
                    ButtonStyle.SUCCESS,
                    String.format("rolesModRolesConfigureButtonEvents_AddRole;%s", userId),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.buttons.addModRoleButton"));

            Button removeModRoleButton = Button.of(
                    ButtonStyle.SUCCESS,
                    String.format("rolesModRolesConfigureButtonEvents_RemoveRole;%s", userId),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.buttons.removeModRoleButton"));

            Button showModRoleButton = Button.of(
                    ButtonStyle.SUCCESS,
                    String.format("rolesModRolesConfigureButtonEvents_ShowRole;%s", userId),
                    bundle.getString("configureEvents.roles.modRoles.rolesModRolesConfigureSubcommand.buttons.showModRoleButton"));

            event.getHook()
                    .editOriginalEmbeds(overviewEmbed.build())
                    .setActionRow(addModRoleButton, removeModRoleButton, showModRoleButton)
                    .queue();
            return;
        }

        sendModRoleConfigureResponse(event.getHook(), addRolle, removeRole, bundle, guild);
    }

    private void sendModRoleConfigureResponse(InteractionHook hook, Role addRole, Role removeRole, ResourceBundle bundle, Guild guild) {
        String responseString;

        long addResponse = 0;
        long removeResponse = 0;

        if (addRole != null && removeRole != null) {
            responseString = String.format(
                    bundle.getString("configureEvents.roles.modRoles.sendModRoleConfigureResponse.twoOptionMappings"),
                    addRole.getAsMention(), removeRole.getAsMention());
            addResponse = controller.addGuildModerationRole(addRole, guild);
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
            addResponse = controller.addGuildModerationRole(addRole, guild);
        }

        if (addResponse + removeResponse != 0) {
            responseString = String.format(
                    bundle.getString("configureEvents.roles.modRoles.sendModRoleConfigureResponse.errorToAddOrRemove"),
                    addResponse, removeResponse);
        }

        hook.editOriginal(responseString)
                .setComponents()
                .setEmbeds()
                .queue();
    }

    @SuppressWarnings("DuplicatedCode")
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
                descriptionStringBuilder.append(role != null ? role.getName() : String.format("<@%d>", id));
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

    @SuppressWarnings("DuplicatedCode")
    @JDAEntityMenu(startWith = "rolesModRolesConfigureEntityMenus_")
    public void rolesModRolesConfigureEntityMenuEvents(@NotNull EntitySelectInteractionEvent event) {
        event.deferEdit().queue();
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "rolesModRolesConfigureEntityMenus_removeRoleMenu" ->
                    sendModRoleConfigureResponse(event.getHook(), null, event.getMentions().getRoles().get(0), bundle, guild);
            case "rolesModRolesConfigureEntityMenus_addRoleMenu" ->
                    sendModRoleConfigureResponse(event.getHook(), event.getMentions().getRoles().get(0), null, bundle, guild);
        }
    }

    private void channelArchiveCategoryConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle) {
        EmbedBuilder manageArchiveCategoryEmbed = new EmbedBuilder();
        manageArchiveCategoryEmbed.setTitle(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.title"));
        manageArchiveCategoryEmbed.setDescription(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.description"));

        manageArchiveCategoryEmbed.addField(
                bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.editCategory.name"),
                bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.editCategory.value"),
                true);
        manageArchiveCategoryEmbed.addField(
                bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.clear.name"),
                bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.clear.value"),
                true);

        String userId = event.getUser().getId();

        Button editArchiveCategoryButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("channelArchiveCategoryConfigureButtons_editButton;%s", userId),
                bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.buttons.editArchiveCategoryButton"));
        Button clearArchiveCategoryButton = Button.of(
                ButtonStyle.DANGER,
                String.format("channelArchiveCategoryConfigureButtons_clearButton;%s", userId),
                bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureSubcommand.buttons.clearArchiveCategoryButton"));

        ActionRow actionRow = ActionRow.of(editArchiveCategoryButton, clearArchiveCategoryButton);

        event.replyEmbeds(manageArchiveCategoryEmbed.build())
                .setComponents(actionRow)
                .setEphemeral(true)
                .queue();

    }

    @SuppressWarnings("DuplicatedCode")
    @JDAButton(startWith = "channelArchiveCategoryConfigureButtons_")
    public void channelArchiveCategoryConfigureButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "channelArchiveCategoryConfigureButtons_editButton" -> channelArchiveCategoryConfigureEditButtonEvent(event, bundle, guild);
            case "channelArchiveCategoryConfigureButtons_clearButton" -> channelArchiveCategoryConfigureClearButtonEvent(event, bundle, guild);
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

    @SuppressWarnings("DuplicatedCode")
    @JDAEntityMenu(startWith = "channelArchiveConfigureMenus_")
    public void channelArchiveConfigureMenuEvents(@NotNull EntitySelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());


        if (splitComponentId[0].equals("channelArchiveConfigureMenus_archiveChannelPicker")) {
            channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent(event, bundle, guild);
        }
    }

    private void channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, Guild guild) {
        event.deferEdit().queue();

        GuildChannel category = event.getMentions().getChannels().get(0);

        long value = controller.setGuildArchiveCategory(category, guild);

        String contentString;

        if (value == 0) {
            contentString = String.format(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent.valueEquals0"), category.getName());
        } else {
            contentString = String.format(bundle.getString("configureEvents.channel.archiveCategory.channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent.valueLower0"), value);
        }
        event.getHook()
                .editOriginalEmbeds()
                .setContent(contentString)
                .setComponents()
                .queue();
    }

    private void channelLogchannelConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, Guild guild) {
        String typeMapping = event.getOption("type", OptionMapping::getAsString);
        LogChannelType logChannelType = LogChannelType.getLogChannelTypeByName(typeMapping);
        if (logChannelType.equals(LogChannelType.UNKNOW)) {
            event.reply(bundle.getString("configureEvents.channel.logchannel.channelLogchannelConfigureSubcommand.logChannelTypeUnkonwn")).queue();
            return;
        }

        event.deferReply(true).queue();

        Channel channel = event.getOption("target-channel", OptionMapping::getAsChannel);
        if (channel == null) {
            EmbedBuilder displaySpecificLogChannelEmbed = new EmbedBuilder();
            displaySpecificLogChannelEmbed.setTitle(
                    bundle.getString("configureEvents.channel.logchannel.channelLogchannelConfigureSubcommand.displaySpecificLogChannelEmbed.title"));

            long channelId = controller.getGuildLoggingChannel(logChannelType, guild);

            if (channelId == 0) {
                displaySpecificLogChannelEmbed.setDescription(
                        String.format(bundle.getString("configureEvents.channel.logchannel.channelLogchannelConfigureSubcommand.displaySpecificLogChannelEmbed.description.hasNoChannel"),
                                logChannelType.getName()));
            } else {
                displaySpecificLogChannelEmbed.setDescription(
                        String.format(bundle.getString("configureEvents.channel.logchannel.channelLogchannelConfigureSubcommand.displaySpecificLogChannelEmbed.description.hasChannel"),
                                logChannelType.getName(), channelId));
            }

            event.getHook().editOriginalEmbeds(displaySpecificLogChannelEmbed.build()).queue();
            return;
        }

        long code = controller.setGuildLoggingChannel(logChannelType, channel, guild);

        if (code == 0) {
            event.getHook().editOriginal(String.format(bundle.getString("configureEvents.channel.logchannel.channelLogchannelConfigureSubcommand.logchannelSet"),
                    channel.getId(), logChannelType.getName())).queue();
        } else {
            event.getHook().editOriginal(String.format(bundle.getString("configureEvents.channel.logchannel.channelLogchannelConfigureSubcommand.errorToSetChannel"),
                    code)).queue();
        }
    }

    private void channelMediaOnlyChannelConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle, Guild guild) {
        event.deferReply(true).queue();
        String userId = event.getUser().getId();
        EmbedBuilder actionEmbed = new EmbedBuilder();
        actionEmbed.setTitle(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.title"));
        actionEmbed.setDescription(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.description"));
        actionEmbed.addField(
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.addDescription.name"),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.addDescription.value"),
                true);
        actionEmbed.addField(
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.editDescription.name"),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.editDescription.value"),
                true);
        actionEmbed.addField(
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.removeDescription.name"),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.actionEmbed.fields.removeDescription.value"),
                true);

        Button addMediaOnlyChannelButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("channelMediaOnlyConfigureButtons_AddButton;%s,%d", userId, 0),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.buttons.addMediaOnlyChannelButton"));
        Button editMediaOnlyChannelButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_editButton;%s,%d", userId, 0),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.buttons.editMediaOnlyChannelButton"));
        Button removeMediaOnlyChannelButton = Button.of(
                ButtonStyle.SECONDARY,
                String.format("channelMediaOnlyConfigureButtons_RemoveButton;%s,%d", userId, 0),
                bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelConfigureSubcommand.buttons.removeMediaOnlyChannelButton"));

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

    @SuppressWarnings("DuplicatedCode")
    @JDAButton(startWith = "channelMediaOnlyConfigureButtons_")
    public void channelMediaOnlyChannelConfigureButtonEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();

        String[] dataIds = splitComponentId[1].split(",");

        if (!userId.equals(dataIds[0])) return;


        ResourceBundle bundle = bundle(event.getUserLocale());

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
                    channelMediaOnlyChangePermissionsEvent(event, bundle, guild, splitComponentId);
            case "channelMediaOnlyConfigureButtons_save" ->
                    channelMediaOnlyChannelSaveButtonEvent(event, bundle, guild, splitComponentId);
        }
    }

    private void channelMediaOnlyChannelSaveButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String[] splitComponentId) {
        event.deferEdit().queue();
        List<MediaOnlyPermissions> permissions = getMediaOnlyPermissionsFromButtonId(splitComponentId);
        long channelId = Long.parseLong(splitComponentId[1].split(",")[1]);
        long returnValue = controller.addOrEditGuildMediaOnlyChannel(channelId, guild, permissions);

        String responseString;

        if (returnValue != 0) {
            responseString = String.format(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelSaveButtonEvent.error"), returnValue);
        } else {
            responseString = bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelSaveButtonEvent.success");
        }

        event.getHook()
                .editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void channelMediaOnlyChangePermissionsEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, String @NotNull [] splitComponentId) {
        event.deferEdit().queue();

        long channelId = Long.parseLong(splitComponentId[1].split(",")[1]);

        List<MediaOnlyPermissions> permissions = getMediaOnlyPermissionsFromButtonId(splitComponentId);

        sendSpecificMediaOnlyChannelEmbed(event.getHook(), channelId, bundle, permissions, guild);
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

    @SuppressWarnings("DuplicatedCode")
    private void channelMediaOnlyChannelBeforeButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, int count, @NotNull String action) {
        event.deferEdit().queue();
        EmbedBuilder beforeMediaOnlyChannelEmbed = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
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

    @SuppressWarnings("DuplicatedCode")
    private void channelMediaOnlyChannelNextButtonEvent(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, Guild guild, int count, @NotNull String action) {
        event.deferEdit().queue();
        EmbedBuilder nextMediaOnlyChannelEmbed = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
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

    @SuppressWarnings("DuplicatedCode")
    @JDAEntityMenu(startWith = "channelMediaOnlyConfigureEntityMenus_")
    public void channelMediaOnlyChannelConfigureEntityMenuEvents(@NotNull EntitySelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        if (splitComponentId[0].equals("channelMediaOnlyConfigureEntityMenus_AddMenu")) {
            channelMediaOnlyChannelAddMenuEvent(event, bundle, guild);
        }
    }

    private void channelMediaOnlyChannelAddMenuEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, Guild guild) {
        event.deferEdit().queue();

        long channelId = event.getMentions().getChannels().get(0).getIdLong();

        sendSpecificMediaOnlyChannelEmbed(event.getHook(), channelId, bundle, new ArrayList<>(), guild);
    }

    @SuppressWarnings("DuplicatedCode")
    @JDAStringMenu(startWith = "channelMediaOnlyConfigureStringMenus_")
    public void channelMediaOnlyChannelConfigureStringMenuEvents(@NotNull StringSelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        String[] splitComponentId = event.getComponentId().split(";");

        String userId = event.getUser().getId();
        if (!userId.equals(splitComponentId[1])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "channelMediaOnlyConfigureStringMenus_EditMenu" ->
                    channelMediaOnlyChannelEditMenuEvent(event, bundle, guild);
            case "channelMediaOnlyConfigureStringMenus_RemoveMenu" ->
                    channelMediaOnlyChannelRemoveEditMenuEvent(event, bundle);
        }
    }

    private void channelMediaOnlyChannelRemoveEditMenuEvent(@NotNull StringSelectInteractionEvent event, ResourceBundle bundle) {
        event.deferEdit().queue();
        long channelId = Long.parseLong(event.getValues().get(0));

        long response = controller.removeGuildMediaOnlyChannel(channelId);

        String responseString;

        if (response != 0) {
            responseString = String.format(bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelRemoveEditMenuEvent.error"), response);
        } else {
            responseString = bundle.getString("configureEvents.channel.mediaOnlyChannel.channelMediaOnlyChannelRemoveEditMenuEvent.success");
        }

        event.getHook()
                .editOriginalEmbeds()
                .setComponents()
                .setContent(responseString)
                .queue();
    }

    private void channelMediaOnlyChannelEditMenuEvent(@NotNull StringSelectInteractionEvent event, ResourceBundle bundle, Guild guild) {
        event.deferEdit().queue();
        long channelId = Long.parseLong(event.getValues().get(0));
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

        sendSpecificMediaOnlyChannelEmbed(event.getHook(), channelId, bundle, permissions, guild);
    }

    private void sendSpecificMediaOnlyChannelEmbed(@NotNull InteractionHook hook, long channelId, @NotNull ResourceBundle bundle, @NotNull List<MediaOnlyPermissions> permissions, @NotNull Guild guild) {
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
                "OK");

        ActionRow secondRow = ActionRow.of(okButton);

        hook.editOriginalEmbeds(specificMediaOnlyChannelEmbed.build())
                .setComponents(editButtons, secondRow)
                .queue();
    }
}
