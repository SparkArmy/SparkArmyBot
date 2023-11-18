package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.annotations.interactions.JDAAutoComplete;
import de.SparkArmy.jda.events.annotations.interactions.JDAButton;
import de.SparkArmy.jda.events.annotations.interactions.JDAEntityMenu;
import de.SparkArmy.jda.events.annotations.interactions.JDASlashCommand;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.jda.utils.LogChannelType;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class ConfigureSlashCommandEvents {

    ConfigController controller;

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
                    case "media-only-channel" -> channelMediaOnlyChannelConfigureSubcommand(event);
                    case "archive-category" -> channelArchiveCategoryConfigureSubcommand(event, bundle);
                }
            }
            case "roles" -> {
                switch (subcommandName) {
                    case "mod-roles" -> rolesModRolesConfigureSubcommand(event);
                    case "punishment-roles" -> rolesPunishmentRolesConfigureSubcommand(event);
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

    private void rolesPunishmentRolesConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void rolesModRolesConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void channelMediaOnlyChannelConfigureSubcommand(SlashCommandInteractionEvent event) {
    }

    private void channelArchiveCategoryConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle) {
        EmbedBuilder manageArchiveCategoryEmbed = new EmbedBuilder();
        manageArchiveCategoryEmbed.setTitle(bundle.getString("configureEvents.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.title"));
        manageArchiveCategoryEmbed.setDescription(bundle.getString("configureEvents.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.description"));

        manageArchiveCategoryEmbed.addField(
                bundle.getString("configureEvents.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.editCategory.name"),
                bundle.getString("configureEvents.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.editCategory.value"),
                true);
        manageArchiveCategoryEmbed.addField(
                bundle.getString("configureEvents.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.clear.name"),
                bundle.getString("configureEvents.channelArchiveCategoryConfigureSubcommand.manageArchiveCategoryEmbed.fields.clear.value"),
                true);

        String userId = event.getUser().getId();

        Button editArchiveCategoryButton = Button.of(
                ButtonStyle.SUCCESS,
                String.format("channelArchiveCategoryConfigureButtons_editButton;%s", userId),
                bundle.getString("configureEvents.channelArchiveCategoryConfigureSubcommand.buttons.editArchiveCategoryButton"));
        Button clearArchiveCategoryButton = Button.of(
                ButtonStyle.DANGER,
                String.format("channelArchiveCategoryConfigureButtons_clearButton;%s", userId),
                bundle.getString("configureEvents.channelArchiveCategoryConfigureSubcommand.buttons.clearArchiveCategoryButton"));

        ActionRow actionRow = ActionRow.of(editArchiveCategoryButton, clearArchiveCategoryButton);

        event.replyEmbeds(manageArchiveCategoryEmbed.build())
                .setComponents(actionRow)
                .setEphemeral(true)
                .queue();

    }

    private void channelLogchannelConfigureSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle, Guild guild) {
        String typeMapping = event.getOption("type", OptionMapping::getAsString);
        LogChannelType logChannelType = LogChannelType.getLogChannelTypeByName(typeMapping);
        if (logChannelType.equals(LogChannelType.UNKNOW)) {
            event.reply(bundle.getString("configureEvents.channelLogchannelConfigureSubcommand.logChannelTypeUnkonwn")).queue();
            return;
        }

        event.deferReply(true).queue();

        Channel channel = event.getOption("target-channel", OptionMapping::getAsChannel);
        if (channel == null) {
            EmbedBuilder displaySpecificLogChannelEmbed = new EmbedBuilder();
            displaySpecificLogChannelEmbed.setTitle(
                    bundle.getString("configureEvents.channelLogchannelConfigureSubcommand.displaySpecificLogChannelEmbed.title"));

            long channelId = controller.getGuildLoggingChannel(logChannelType, guild);

            if (channelId == 0) {
                displaySpecificLogChannelEmbed.setDescription(
                        String.format(bundle.getString("configureEvents.channelLogchannelConfigureSubcommand.displaySpecificLogChannelEmbed.description.hasNoChannel"),
                                logChannelType.getName()));
            } else {
                displaySpecificLogChannelEmbed.setDescription(
                        String.format(bundle.getString("configureEvents.channelLogchannelConfigureSubcommand.displaySpecificLogChannelEmbed.description.hasChannel"),
                                logChannelType.getName(), channelId));
            }

            event.getHook().editOriginalEmbeds(displaySpecificLogChannelEmbed.build()).queue();
            return;
        }

        long code = controller.setGuildLoggingChannel(logChannelType, channel, guild);

        if (code == 0) {
            event.getHook().editOriginal(String.format(bundle.getString("configureEvents.channelLogchannelConfigureSubcommand.logchannelSet"),
                    channel.getId(), logChannelType.getName())).queue();
        } else {
            event.getHook().editOriginal(String.format(bundle.getString("configureEvents.channelLogchannelConfigureSubcommand.errorToSetChannel"),
                    code)).queue();
        }
    }

    @JDAButton(startWith = "channelArchiveCategoryConfigureButtons_")
    public void channelArchiveCategoryConfigureSubcommandButtonEvents(@NotNull ButtonInteractionEvent event) {
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
            contentString = bundle.getString("configureEvents.channelArchiveCategoryConfigureClearButtonEvent.valueEquals0");
        } else {
            contentString = String.format(bundle.getString("configureEvents.channelArchiveCategoryConfigureClearButtonEvent.valueLower0"), value);
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
        channelArchiveEditEmbed.setTitle(bundle.getString("configureEvents.channelArchiveCategoryConfigureEditButtonEvent.embed.title"));
        channelArchiveEditEmbed.setDescription(bundle.getString("configureEvents.channelArchiveCategoryConfigureEditButtonEvent.embed.description"));

        long archiveCategoryId = controller.getGuildArchiveCategory(guild);

        if (archiveCategoryId < 0) {
            event.reply(
                            String.format(bundle.getString("configureEvents.channelArchiveCategoryConfigureEditButtonEvent.errorToGetCategory"),
                                    archiveCategoryId))
                    .queue();
            return;
        } else if (archiveCategoryId > 0) {

            Category archiveCategory = guild.getCategoryById(archiveCategoryId);

            if (archiveCategory != null) {
                channelArchiveEditEmbed.addField(
                        bundle.getString("configureEvents.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.name"),
                        String.format("<@%s>", archiveCategory.getId()),
                        true);
            } else {
                channelArchiveEditEmbed.addField(
                        bundle.getString("configureEvents.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.name"),
                        bundle.getString("configureEvents.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.valueIfCategoryNull"),
                        true);
            }
        } else {
            channelArchiveEditEmbed.addField(
                    bundle.getString("configureEvents.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.name"),
                    bundle.getString("configureEvents.channelArchiveCategoryConfigureEditButtonEvent.embed.fields.category.valueIfCategoryNull"),
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
            channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent(event, bundle, guild);
        }
    }

    private void channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent(@NotNull EntitySelectInteractionEvent event, ResourceBundle bundle, Guild guild) {
        event.deferEdit().queue();

        GuildChannel category = event.getMentions().getChannels().get(0);

        long value = controller.setGuildArchiveCategory(category, guild);

        String contentString;

        if (value == 0) {
            contentString = String.format(bundle.getString("configureEvents.channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent.valueEquals0"), category.getName());
        } else {
            contentString = String.format(bundle.getString("configureEvents.channelArchiveCategoryConfigureArchiveChannelPickerMenuEvent.valueLower0"), value);
        }
        event.getHook()
                .editOriginalEmbeds()
                .setContent(contentString)
                .setComponents()
                .queue();
    }
}
