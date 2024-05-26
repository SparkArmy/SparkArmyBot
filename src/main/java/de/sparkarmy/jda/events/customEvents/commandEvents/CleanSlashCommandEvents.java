package de.sparkarmy.jda.events.customEvents.commandEvents;

import de.sparkarmy.db.DatabaseAction;
import de.sparkarmy.jda.annotations.events.JDAButtonInteractionEvent;
import de.sparkarmy.jda.annotations.events.JDAModalInteractionEvent;
import de.sparkarmy.jda.annotations.events.JDASlashCommandInteractionEvent;
import de.sparkarmy.jda.annotations.events.JDAStringSelectInteractionEvent;
import de.sparkarmy.jda.annotations.internal.JDAEvent;
import de.sparkarmy.jda.events.EventManager;
import de.sparkarmy.jda.events.iEvent.IJDAEvent;
import de.sparkarmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ResourceBundle;

public class CleanSlashCommandEvents implements IJDAEvent {

    private final DatabaseAction postgres;

    public CleanSlashCommandEvents(@NotNull EventManager ignoredManager) {
        this.postgres = new DatabaseAction();
    }

    private ResourceBundle bundle(DiscordLocale locale) {
        return Util.getResourceBundle("clean", locale);
    }

    private ResourceBundle standardBundle(DiscordLocale locale) {
        return Util.getResourceBundle("standardPhrases", locale);
    }

    @JDAEvent
    @JDASlashCommandInteractionEvent(name = "clean")
    public void initialCleanSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) return;
        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardBundle = standardBundle(event.getUserLocale());
        switch (subcommandName) {
            case "all" -> allCleanSubcommand(event, bundle);
            case "last" -> lastCleanSubcommand(event, bundle);
            case "add" -> addPeriodicCleanSubcommand(event, bundle, standardBundle);
            case "show" -> showPeriodicCleanSubcommand(event, bundle);
        }
    }

    private void allCleanSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle) {
        event.deferReply(true).queue();
        Integer amount = event.getOption("count", 100, OptionMapping::getAsInt);
        User user = event.getOption("user", OptionMapping::getAsUser);

        event.getChannel().getHistory().retrievePast(amount)
                .map(x -> {
                    if (user != null) {
                        return x.stream().filter(y -> y.getAuthor().equals(user)).toList();
                    } else return x;
                })
                .map(x -> event.getChannel().purgeMessages(x))
                .flatMap(x -> event.getHook().editOriginal(bundle.getString("cleanCommandEvents.allCleanSubcommand.executed")))
                .onErrorFlatMap(ErrorResponse.UNKNOWN_CHANNEL::test, x -> event.getHook().editOriginal(bundle.getString("cleanCommandEvents.allCleanSubcommand.unknownChannel")))
                .onErrorFlatMap(ErrorResponse.MISSING_PERMISSIONS::test, x -> event.getHook().editOriginal(bundle.getString("cleanCommandEvents.allCleanSubcommand.missingPermissions")))
                .queue();
    }

    private void lastCleanSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle) {
        event.deferReply(true).queue();
        Integer days = event.getOption("days", 5, OptionMapping::getAsInt);
        event.getChannel().getHistory().retrievePast(100)
                .map(x -> x.stream().filter(y -> y.getTimeCreated().isAfter(OffsetDateTime.now().minusDays(days))).toList())
                .map(x -> event.getChannel().purgeMessages(x))
                .flatMap(x -> event.getHook().editOriginal(bundle.getString("cleanCommandEvents.lastCleanSubcommand.executed")))
                .onErrorFlatMap(ErrorResponse.UNKNOWN_CHANNEL::test, x -> event.getHook().editOriginal(bundle.getString("cleanCommandEvents.allCleanSubcommand.unknownChannel")))
                .onErrorFlatMap(ErrorResponse.MISSING_PERMISSIONS::test, x -> event.getHook().editOriginal(bundle.getString("cleanCommandEvents.allCleanSubcommand.missingPermissions")))
                .queue();
    }

    private void addPeriodicCleanSubcommand(@NotNull SlashCommandInteractionEvent event, @NotNull ResourceBundle bundle, ResourceBundle standardBundle) {
        event.deferReply(true).queue();
        GuildChannel channel = event.getOption("channel", event.getGuildChannel(), OptionMapping::getAsChannel);
        Integer days = event.getOption("period", 7, OptionMapping::getAsInt);

        long added = postgres.putDataInPeriodicCleanTable(channel.getIdLong(), channel.getGuild().getIdLong(), days, event.getUser().getIdLong());
        if (added > 0) {
            event.getHook()
                    .editOriginal(bundle.getString("cleanCommandEvents.addPeriodicCleanSubcommand.executed"))
                    .queue();
        } else if (added == 0) {
            event.getHook()
                    .editOriginal(bundle.getString("cleanCommandEvents.addPeriodicCleanSubcommand.noChanges"))
                    .queue();
        } else {
            event.getHook()
                    .editOriginal(String.format(standardBundle.getString("replies.dbErrorReply"), added))
                    .queue();
        }
    }

    private void showPeriodicCleanSubcommand(@NotNull SlashCommandInteractionEvent event, ResourceBundle bundle) {
        // Check if Guild null
        Guild guild = event.getGuild();
        if (guild == null) return;
        // Reply to event and get  event-hook
        event.deferReply().setEphemeral(true).queue();
        InteractionHook hook = event.getHook();
        // Check if Member null
        Member member = event.getMember();
        if (member == null) return;

        // Check if Member has Administrator-Permissions
        if (!member.hasPermission(Permission.ADMINISTRATOR)) return;

        // Get Data from Database
        JSONObject tableData = postgres.getDataFromPeriodicCleanTable(guild.getIdLong());
        // Create an EmbedBuilder and StringSelectMenuBuilder
        EmbedBuilder showDataEmbed = new EmbedBuilder();
        showDataEmbed.setTitle(bundle.getString("cleanCommandEvents.showPeriodicCleanSubcommand.embed.title"));
        showDataEmbed.setColor(new Color(0x188647));
        // Call methods to add EmbedFields
        setEmbedFields(showDataEmbed, 0, tableData, bundle, event.getJDA(), false, null);
        // Set a description if no data available
        if (showDataEmbed.getFields().isEmpty()) {
            showDataEmbed.setDescription(bundle.getString("cleanCommandEvents.showPeriodicCleanSubcommand.embed.description"));
            hook.editOriginalEmbeds(showDataEmbed.build())
                    .queue();
            return;
        }

        ActionRow buttonActions;
        if (tableData.length() > 25) {
            buttonActions = ActionRow.of(
                    editButton(member.getId(), bundle, 0),
                    deleteButton(member.getId(), bundle, 0),
                    nextButton(member.getId(), bundle, 25).asDisabled()
            );
        } else {
            buttonActions = ActionRow.of(
                    editButton(member.getId(), bundle, 0),
                    deleteButton(member.getId(), bundle, 0)
            );
        }

        hook.editOriginalEmbeds(showDataEmbed.build())
                .setComponents(buttonActions)
                .queue();
    }

    private @NotNull Button deleteButton(String memberId, @NotNull ResourceBundle bundle, long count) {
        return Button.of(
                ButtonStyle.DANGER,
                String.format("cleanCommand_PeriodicCleanSubcommandActions_delete;%s,%d", memberId, count),
                bundle.getString("cleanCommandEvents.buttons.delete"));
    }

    private @NotNull Button editButton(String memberId, @NotNull ResourceBundle bundle, long count) {
        return Button.of(
                ButtonStyle.SECONDARY,
                String.format("cleanCommand_PeriodicCleanSubcommandActions_edit;%s,%d", memberId, count),
                bundle.getString("cleanCommandEvents.buttons.edit"));
    }

    private @NotNull Button nextButton(String memberId, @NotNull ResourceBundle bundle, long count) {
        return Button.of(
                ButtonStyle.SECONDARY,
                String.format("cleanCommand_PeriodicCleanSubcommandActions_next;%s,%d", memberId, count),
                bundle.getString("cleanCommandEvents.buttons.next"));
    }

    private @NotNull Button beforeButton(String memberId, @NotNull ResourceBundle bundle, long count) {
        return Button.of(
                ButtonStyle.SECONDARY,
                String.format("cleanCommand_PeriodicCleanSubcommandActions_before;%s,%d", memberId, count),
                bundle.getString("cleanCommandEvents.buttons.before"));
    }

    @JDAEvent
    @JDAButtonInteractionEvent(startWith = "cleanCommand_PeriodicCleanSubcommandActions_")
    public void buttonPeriodicSubcommandAction(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        String componentId = event.getComponentId();
        String[] splitComponentId = componentId.split(";");

        String[] eventRelatedData = splitComponentId[1].split(",");

        if (!eventRelatedData[0].equals(event.getUser().getId())) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        JSONObject entries = postgres.getDataFromPeriodicCleanTable(guild.getIdLong());

        if (componentId.startsWith("cleanCommand_PeriodicCleanSubcommandActions_delete")) {
            deleteButtonAction(event, bundle, entries, eventRelatedData);
        } else if (componentId.startsWith("cleanCommand_PeriodicCleanSubcommandActions_edit")) {
            editButtonAction(event, bundle, entries, eventRelatedData);
        } else if (componentId.startsWith("cleanCommand_PeriodicCleanSubcommandActions_next")) {
            nextButtonAction(event, bundle, entries, eventRelatedData);
        } else if (componentId.startsWith("cleanCommand_PeriodicCleanSubcommandActions_before")) {
            beforeButtonAction(event, bundle, entries, eventRelatedData);
        }

    }

    @SuppressWarnings("DuplicatedCode")
    private void beforeButtonAction(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, @NotNull JSONObject entries, String @NotNull [] splitComponentId) {
        event.deferEdit().queue();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        long count = Long.parseLong(splitComponentId[1]);
        String memberId = splitComponentId[0];
        int entrySize = entries.keySet().size();
        setEmbedFields(embedBuilder, count, entries, bundle, event.getJDA(), false, null);
        ActionRow actionRow;
        if (count - 25 > 0) {
            actionRow = ActionRow.of(
                    beforeButton(memberId, bundle, count - 25),
                    editButton(memberId, bundle, count),
                    deleteButton(memberId, bundle, count),
                    nextButton(memberId, bundle, Math.min(entrySize - count, 25)));
        } else {
            actionRow = ActionRow.of(
                    editButton(memberId, bundle, count),
                    deleteButton(memberId, bundle, count),
                    nextButton(memberId, bundle, Math.min(entrySize - count, 25)));
        }
        event.getHook().editOriginalEmbeds(embedBuilder.build())
                .setComponents(actionRow)
                .queue();

    }

    @SuppressWarnings("DuplicatedCode")
    private void nextButtonAction(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, @NotNull JSONObject entries, String @NotNull [] splitComponentId) {
        event.deferEdit().queue();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        long count = Long.parseLong(splitComponentId[1]);
        String memberId = splitComponentId[0];
        int entrySize = entries.keySet().size();
        setEmbedFields(embedBuilder, count, entries, bundle, event.getJDA(), false, null);
        ActionRow actionRow;
        if (entrySize - count > 1) {
            actionRow = ActionRow.of(
                    beforeButton(memberId, bundle, count - 25),
                    editButton(memberId, bundle, count),
                    deleteButton(memberId, bundle, count),
                    nextButton(memberId, bundle, Math.min(entrySize - count, 25)));
        } else {
            actionRow = ActionRow.of(
                    beforeButton(memberId, bundle, count - 25),
                    editButton(memberId, bundle, count),
                    deleteButton(memberId, bundle, count));
        }
        event.getHook().editOriginalEmbeds(embedBuilder.build())
                .setComponents(actionRow)
                .queue();
    }

    private void deleteButtonAction(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, JSONObject entries, String @NotNull [] splitComponentId) {
        event.deferEdit().queue();
        EmbedBuilder deleteEmbedBuilder = new EmbedBuilder();
        StringSelectMenu.Builder deleteStringMenu = StringSelectMenu
                .create(String.format("cleanCommand_ButtonActionStringMenu_delete;%s", splitComponentId[0]));

        setEmbedFields(deleteEmbedBuilder, Long.parseLong(splitComponentId[1]), entries, bundle, event.getJDA(), true, deleteStringMenu);

        ActionRow deleteActionRow = ActionRow.of(deleteStringMenu.build());

        event.getHook()
                .editOriginalEmbeds(deleteEmbedBuilder.build())
                .setComponents(deleteActionRow)
                .queue();
    }

    private void editButtonAction(@NotNull ButtonInteractionEvent event, ResourceBundle bundle, JSONObject entries, String @NotNull [] splitComponentId) {
        event.deferEdit().queue();
        EmbedBuilder editEmbedBuilder = new EmbedBuilder();
        StringSelectMenu.Builder editSelectMenu = StringSelectMenu
                .create(String.format("cleanCommand_ButtonActionStringMenu_edit;%s", splitComponentId[0]));

        setEmbedFields(editEmbedBuilder, Long.parseLong(splitComponentId[1]), entries, bundle, event.getJDA(), true, editSelectMenu);

        ActionRow editActionRow = ActionRow.of(editSelectMenu.build());

        event.getHook()
                .editOriginalEmbeds(editEmbedBuilder.build())
                .setComponents(editActionRow)
                .queue();
    }

    @JDAEvent
    @JDAStringSelectInteractionEvent(startWith = "cleanCommand_ButtonActionStringMenu_")
    public void stringMenuButtonActions(@NotNull StringSelectInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        String[] splitComponentId = event.getComponentId().split(";");
        if (!splitComponentId[1].equals(event.getUser().getId())) return;

        ResourceBundle bundle = bundle(event.getUserLocale());

        if (splitComponentId[0].equals("cleanCommand_ButtonActionStringMenu_delete")) {
            deletePeriodicCleanStringMenuAction(event, bundle);
        } else if (splitComponentId[0].equals("cleanCommand_ButtonActionStringMenu_edit")) {
            editPeriodCleanStringMenuAction(event, bundle, splitComponentId, guild);
        }
    }

    private void editPeriodCleanStringMenuAction(@NotNull StringSelectInteractionEvent event, @NotNull ResourceBundle bundle, String @NotNull [] splitComponentId, @NotNull Guild guild) {
        JSONObject value = postgres.getDataFromPeriodicCleanTable(guild.getIdLong()).getJSONObject(event.getValues().getFirst());
        Modal.Builder editActionModal = Modal.create(
                String.format("cleanCommand_editPeriodicActionModal;%s;%s", splitComponentId[1], event.getValues().getFirst()),
                bundle.getString("cleanCommandEvents.editPeriodicCleanStringMenuAction.editActionModal.title"));

        TextInput.Builder days = TextInput.create(
                        String.format("days;%d", value.getLong("days")),
                        bundle.getString("cleanCommandEvents.editPeriodicCleanStringMenuAction.editActionModal.days.label"),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(1, 3)
                .setPlaceholder(bundle.getString("cleanCommandEvents.editPeriodicCleanStringMenuAction.editActionModal.days.placeholder"));

        TextInput.Builder active = TextInput.create(
                        String.format("active;%b", value.getBoolean("active")),
                        bundle.getString("cleanCommandEvents.editPeriodicCleanStringMenuAction.editActionModal.active.label"),
                        TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(4, 5)
                .setPlaceholder(bundle.getString("cleanCommandEvents.editPeriodicCleanStringMenuAction.editActionModal.active.placeholder"));


        days.setValue(String.valueOf(value.getLong("days")));
        active.setValue(String.valueOf(value.getBoolean("active")));

        editActionModal.addActionRow(days.build());
        editActionModal.addActionRow(active.build());

        event.replyModal(editActionModal.build()).queue();
    }

    @JDAEvent
    @JDAModalInteractionEvent(startWith = "cleanCommand_editPeriodicActionModal")
    public void editPeriodicActionModalEvent(@NotNull ModalInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        String[] splitComponentId = event.getModalId().split(";");
        if (!event.getUser().getId().equals(splitComponentId[1])) return;

        event.deferEdit().queue();
        long databaseId = Long.parseLong(splitComponentId[2]);

        ModalMapping daysMapping = event.getValues().stream().filter(x -> x.getId().startsWith("days")).toList().getFirst();
        long days = Long.parseLong(daysMapping.getAsString());

        ModalMapping activeMapping = event.getValues().stream().filter(x -> x.getId().startsWith("active")).toList().getFirst();
        boolean active = Boolean.getBoolean(activeMapping.getAsString());

        ResourceBundle standardBundle = standardBundle(event.getUserLocale());
        long edited = postgres.editDataInPeriodicCleanTable(databaseId, active, days);
        if (edited > 0) {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(standardBundle.getString("replies.dataEdit"))
                    .queue();
        } else if (edited == 0) {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(standardBundle.getString("replies.noDataEdit"))
                    .queue();
        } else {
            event.getHook()
                    .editOriginalEmbeds()
                    .setComponents()
                    .setContent(String.format(standardBundle.getString("replies.dbErrorReply"), edited))
                    .queue();
        }
    }


    private void deletePeriodicCleanStringMenuAction(@NotNull StringSelectInteractionEvent event, ResourceBundle bundle) {
        event.deferEdit().queue();
        ResourceBundle standardBundle = standardBundle(event.getUserLocale());
        long removed = postgres.deleteDataFromPeriodicCleanTable(Long.parseLong(event.getValues().getFirst()));
        if (removed > 0) {
            event.getHook()
                    .editOriginal(bundle.getString("cleanCommandEvents.deletePeriodicCleanStringMenuAction.deleted"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        } else if (removed == 0) {
            event.getHook()
                    .editOriginal(bundle.getString("cleanCommandEvents.deletePeriodicCleanStringMenuAction.notDeleted"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        } else {
            event.getHook()
                    .editOriginal(String.format(standardBundle.getString("replies.dbErrorReply"), removed))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        }
    }

    private void setEmbedFields(EmbedBuilder embedBuilder, long countFrom, @NotNull JSONObject entries, ResourceBundle bundle, JDA jda, boolean withStringMenu, StringSelectMenu.Builder stringSelectMenu) {
        int i = 0;
        for (String keyString : entries.keySet().stream().sorted().toList()) {
            countFrom--;
            if (countFrom < 0) {
                i++;
                JSONObject entry = entries.getJSONObject(keyString);
                GuildChannel guildChannel = jda.getGuildChannelById(entry.getLong("channelId"));
                embedBuilder.addField(String.format("""
                                %s: %s
                                """,
                        bundle.getString("cleanCommandEvents.setEmbedFields.name.channel"),
                        guildChannel != null ? guildChannel.getAsMention() : String.format("<%s>", entry.getLong("channelId"))
                ), String.format(
                        """
                                %s: <@%s>
                                %s: %s
                                %s: %s
                                %s: %s
                                """,
                        bundle.getString("cleanCommandEvents.setEmbedFields.value.creator"),
                        entry.getLong("creator"),
                        bundle.getString("cleanCommandEvents.setEmbedFields.value.active"),
                        entry.getBoolean("active"),
                        bundle.getString("cleanCommandEvents.setEmbedFields.value.lastExecution"),
                        entry.get("lastExecution"),
                        bundle.getString("cleanCommandEvents.setEmbedFields.value.nextExecution"),
                        entry.get("nextExecution")), false);

                if (withStringMenu) {
                    stringSelectMenu.addOption(guildChannel != null ? guildChannel.getName() : String.format("<%s>", entry.getLong("channelId")), keyString);
                }
                if (i == 25) break;
            }
        }
    }

    @Override
    public Class<?> getEventClass() {
        return this.getClass();
    }
}
