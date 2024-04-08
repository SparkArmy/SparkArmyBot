package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.db.DatabaseAction;
import de.SparkArmy.jda.annotations.events.JDAButtonInteractionEvent;
import de.SparkArmy.jda.annotations.events.JDAModalInteractionEvent;
import de.SparkArmy.jda.annotations.events.JDASlashCommandInteractionEvent;
import de.SparkArmy.jda.annotations.events.JDAStringSelectInteractionEvent;
import de.SparkArmy.jda.events.EventManager;
import de.SparkArmy.jda.events.iEvent.IJDAEvent;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static de.SparkArmy.utils.Util.logger;

public class NoteSlashCommandEvents implements IJDAEvent {
    private final DatabaseAction db;

    public NoteSlashCommandEvents(EventManager ignoredManager) {
        this.db = new DatabaseAction();
    }

    private ResourceBundle bundle(DiscordLocale locale) {
        return Util.getResourceBundle("note", locale);
    }

    private ResourceBundle standardPhrases(DiscordLocale locale) {
        return Util.getResourceBundle("standardPhrases", locale);
    }

    final @NotNull Button nextButton(@NotNull ResourceBundle bundle, String @NotNull [] ids, int count, @NotNull NoteEmbedActionType actionType) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_%sNoteEmbed_next;%s,%s;%d", actionType.getName(), ids[0], ids[1], count),
                bundle.getString("buttons.next"));
    }

    final @NotNull Button beforeButton(@NotNull ResourceBundle bundle, String @NotNull [] ids, int count, @NotNull NoteEmbedActionType actionType) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_%sNoteEmbed_before;%s,%s;%d", actionType.getName(), ids[0], ids[1], count),
                bundle.getString("buttons.before"));
    }

    final @NotNull Button closeButton(@NotNull ResourceBundle bundle, String @NotNull [] ids, @NotNull NoteEmbedActionType actionType) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_%sNoteEmbed_close;%s", actionType.getName(), ids[0]),
                bundle.getString("buttons.close"));
    }

    @JDAButtonInteractionEvent(startWith = "noteCommand")
    public void noteButtonsEvents(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        String componentId = event.getComponentId();
        String[] splitComponentId = componentId.split(";");

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        switch (splitComponentId[0]) {
            case "noteCommand_initialShowEmbed_show" ->
                    sendInitialEmbed(event, NoteEmbedActionType.SHOW, bundle, standardPhrases);
            case "noteCommand_initialShowEmbed_edit" ->
                    sendInitialEmbed(event, NoteEmbedActionType.EDIT, bundle, standardPhrases);
            case "noteCommand_initialShowEmbed_remove" ->
                    sendInitialEmbed(event, NoteEmbedActionType.REMOVE, bundle, standardPhrases);
            case "noteCommand_showNoteEmbed_close",
                    "noteCommand_editNoteEmbed_close",
                    "noteCommand_removeNoteEmbed_close" -> closeEmbed(event, bundle);
            case "noteCommand_showNoteEmbed_next" ->
                    sendShowEmbed(event, ClickType.NEXT, NoteEmbedActionType.SHOW, standardPhrases);
            case "noteCommand_editNoteEmbed_next" ->
                    sendShowEmbed(event, ClickType.NEXT, NoteEmbedActionType.EDIT, standardPhrases);
            case "noteCommand_removeNoteEmbed_next" ->
                    sendShowEmbed(event, ClickType.NEXT, NoteEmbedActionType.REMOVE, standardPhrases);
            case "noteCommand_showNoteEmbed_before" ->
                    sendShowEmbed(event, ClickType.BEFORE, NoteEmbedActionType.SHOW, standardPhrases);
            case "noteCommand_editNoteEmbed_before" ->
                    sendShowEmbed(event, ClickType.BEFORE, NoteEmbedActionType.EDIT, standardPhrases);
            case "noteCommand_removeNoteEmbed_before" ->
                    sendShowEmbed(event, ClickType.BEFORE, NoteEmbedActionType.REMOVE, standardPhrases);
        }
    }

    private void closeEmbed(@NotNull ButtonInteractionEvent event, ResourceBundle bundle) {
        String[] splitId = event.getComponentId().split(";");
        String commandUserId = splitId[1];
        if (!event.getUser().getId().equals(commandUserId)) return;

        event.deferEdit().setEmbeds().setComponents().setContent(bundle.getString("events.closeShowEmbedClickEvent.closeMessage")).queue();
    }

    @SuppressWarnings("DuplicatedCode")
    private void sendInitialEmbed(@NotNull ButtonInteractionEvent event, NoteEmbedActionType actionType, ResourceBundle bundle, ResourceBundle standardPhrases) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        event.deferEdit().queue();
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        String commandUserId = ids[0];
        String targetUserId = ids[1];
        if (!event.getUser().getId().equals(commandUserId)) return;

        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(targetUserId), guild.getIdLong());

        EmbedBuilder showNoteEmbed = new EmbedBuilder(event.getMessage().getEmbeds().getFirst());
        showNoteEmbed.setDescription(bundle.getString("events.showButtonClickEvent.showNoteEmbed.description"));
        showNoteEmbed.clearFields();

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(
                String.format("noteCommand_showNoteEmbed_%sMenu;%s,%s", actionType.getName(), commandUserId, targetUserId));

       setEmbedFieldsAndGetModerators(showNoteEmbed,menuBuilder,0,notes);
        ActionRow actionRow;
        if (notes.length() > 25) {
            actionRow = ActionRow.of(
                    nextButton(standardPhrases, ids, 25, actionType),
                    closeButton(standardPhrases, ids, actionType));
        } else {
            actionRow = ActionRow.of(closeButton(standardPhrases, ids, actionType));
        }
        switch (actionType) {
            case SHOW -> event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow).queue();
            case EDIT, REMOVE ->
                    event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow, ActionRow.of(menuBuilder.build())).queue();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void sendShowEmbed(@NotNull ButtonInteractionEvent event, ClickType clicktype, NoteEmbedActionType actionType, ResourceBundle standardPhrases) {
        Guild guild = event.getGuild();
        if (guild == null) return;
        event.deferEdit().queue();
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        String commandUserId = ids[0];
        String targetUserId = ids[1];
        int count = Integer.parseInt(splitId[2]);
        if (!ids[0].equals(event.getUser().getId())) return;

        EmbedBuilder showNoteEmbed = new EmbedBuilder(event.getMessage().getEmbeds().getFirst());
        showNoteEmbed.clearFields();

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(
                String.format("noteCommand_showNoteEmbed_%sMenu;%s,%s", actionType.getName(), commandUserId, targetUserId));

        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(targetUserId), event.getGuild().getIdLong());

        setEmbedFieldsAndGetModerators(showNoteEmbed,menuBuilder,count,notes);

        ActionRow actionRow;
        int notesSize = notes.keySet().size();
        if (clicktype.equals(ClickType.BEFORE)) {
            if (count - 25 > 0) {
                actionRow = ActionRow.of(
                        beforeButton(standardPhrases, ids, count - 25, actionType),
                        nextButton(standardPhrases, ids, Math.min(notesSize - count, 25), actionType),
                        closeButton(standardPhrases, ids, actionType));
            } else {
                actionRow = ActionRow.of(
                        nextButton(standardPhrases, ids, Math.min(notesSize - count, 25), actionType),
                        closeButton(standardPhrases, ids, actionType));
            }
        } else {
            if (notesSize - count > 1) {
                actionRow = ActionRow.of(
                        beforeButton(standardPhrases, ids, count - 25, actionType),
                        nextButton(standardPhrases, ids, Math.min(notesSize - count, 25), actionType),
                        closeButton(standardPhrases, ids, actionType));
            } else {
                actionRow = ActionRow.of(
                        beforeButton(standardPhrases, ids, count - 25, actionType),
                        closeButton(standardPhrases, ids, actionType));
            }
        }

        switch (actionType) {
            case SHOW -> event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow).queue();
            case EDIT, REMOVE ->
                    event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow, ActionRow.of(menuBuilder.build())).queue();
        }

    }

    @JDAStringSelectInteractionEvent(startWith = "noteCommand_showNoteEmbed_editMenu")
    public void noteEditSelectEvent(@NotNull StringSelectInteractionEvent event) {
        if (event.getGuild() == null) return;
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        if (!event.getUser().getId().equals(ids[0])) return;
        TextInput.Builder noteContent = TextInput.create(event.getValues().getFirst(), "Note Content", TextInputStyle.PARAGRAPH);
        noteContent.setMaxLength(1024);

        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(ids[1]), event.getGuild().getIdLong());
        noteContent.setValue(notes.getJSONObject(event.getValues().getFirst()).getString("noteContent"));

        ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());

        Modal.Builder noteEditModal = Modal.create("noteCommand_editNoteModal;%s,%s".formatted(ids[0], ids[1]),
                bundle.getString("events.noteEditSelectEvent.editNoteModal.title"));
        noteEditModal.addComponents(ActionRow.of(noteContent.build()));
        event.getHook().deleteOriginal().queue();
        event.replyModal(noteEditModal.build()).queue();
    }

    @JDAStringSelectInteractionEvent(startWith = "noteCommand_showNoteEmbed_removeMenu")
    public void noteRemoveSelectEvent(@NotNull StringSelectInteractionEvent event) {
        if (event.getGuild() == null) return;
        event.deferEdit().queue();
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        if (!event.getUser().getId().equals(ids[0])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());

        long value = db.deleteDataFromNoteTable(Long.parseLong(ids[1]), event.getGuild().getIdLong(), LocalDateTime.parse(event.getValues().getFirst().replace(" ", "T")));
        if (value > 0) {
            event.getHook().editOriginal(bundle.getString("events.noteRemoveSelectEvent.success"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        } else if (value < 0) {
            event.getHook().editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), value))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        } else {
            event.getHook().editOriginal(standardPhrases.getString("replies.noDataEdit"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        }
    }

    @JDAModalInteractionEvent(startWith = "noteCommand_editNoteModal")
    public void noteModalEvent(@NotNull ModalInteractionEvent event) {
        if (event.getGuild() == null) return;
        event.deferReply(true).queue();
        String[] splitId = event.getModalId().split(";");
        String[] ids = splitId[1].split(",");
        if (!event.getUser().getId().equals(ids[0])) return;

        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());
        ModalMapping noteContentField = event.getValues().getFirst();

        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(ids[1]), event.getGuild().getIdLong());

        if (notes.getJSONObject(noteContentField.getId()).getString("noteContent").equals(noteContentField.getAsString())) {
            event.getHook().editOriginal(bundle.getString("events.noteModalEvent.editIsEqualEntry")).queue();
            return;
        }

        long updateValue = db.updateDataFromNoteTable(event.getGuild().getIdLong(), Long.parseLong(ids[1]), LocalDateTime.parse(noteContentField.getId().replace(" ", "T")), noteContentField.getAsString());

        if (updateValue > 0) {
            event.getHook().editOriginal(bundle.getString("events.noteModalEvent.successMessage")).queue();
        } else if (updateValue < 0) {
            event.getHook().editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), updateValue)).queue();
        } else {
            event.getHook().editOriginal(standardPhrases.getString("replies.noDataEdit"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        }
    }

    @JDASlashCommandInteractionEvent(name = "note")
    public void initialSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = bundle(event.getUserLocale());
        ResourceBundle standardPhrases = standardPhrases(event.getUserLocale());
        String subcommandName = event.getSubcommandName();

        if (subcommandName == null) {
            event.reply(bundle.getString("command.dispatchSlashEvent.subcommandNameIsNull")).setEphemeral(true).queue();
            return;
        }

        Member commandMember = event.getMember();
        Guild eventGuild = event.getGuild();
        if (commandMember == null || eventGuild == null) {
            event.reply(bundle.getString("command.dispatchSlashEvent.eventNotFromGuild")).setEphemeral(true).queue();
            return;
        }

        User targetUser = event.getOption("user", OptionMapping::getAsUser);
        if (targetUser == null) {
            event.reply(bundle.getString("command.dispatchSlashEvent.targetIsNull")).setEphemeral(true).queue();
            return;
        }

        switch (subcommandName) {
            case "add" -> addNoteCommand(event, targetUser, eventGuild, bundle, standardPhrases);
            case "show" -> showNoteCommand(event, commandMember, targetUser, eventGuild, bundle, standardPhrases);
            default -> {
                logger.warn("noteSlashCommand has a default value in switch(subcommandName) with value: " + subcommandName);
                event.reply(bundle.getString("command.dispatchSlashEvent.defaultReply")).setEphemeral(true).queue();
            }
        }

    }

    private void showNoteCommand(@NotNull SlashCommandInteractionEvent event, Member commandExecutor, @NotNull User targetUser, @NotNull Guild guild, ResourceBundle bundle, ResourceBundle standardPhrases) {

        JSONObject notes = db.getDataFromNoteTable(targetUser.getIdLong(), guild.getIdLong());

        if (notes.isEmpty()) {
            event.reply(bundle.getString("command.showNoteCommand.notesIsEmpty")).setEphemeral(true).queue();
            return;
        }

        EmbedBuilder initialShowNoteEmbed = new EmbedBuilder();
        initialShowNoteEmbed.setTitle(String.format(bundle.getString("command.showNoteCommand.initialShowNoteEmbed.title"), targetUser.getName()));
        initialShowNoteEmbed.setDescription(bundle.getString("command.showNoteCommand.initialShowNoteEmbed.description"));
        initialShowNoteEmbed.addField(
                standardPhrases.getString("embeds.fields.name.show"),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.show.value"), true);
        initialShowNoteEmbed.addField(
                standardPhrases.getString("embeds.fields.name.edit"),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.edit.value"), true);
        initialShowNoteEmbed.addField(
                standardPhrases.getString("embeds.fields.name.remove"),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.remove.value"), true);

        String buttonIdPreset = "noteCommand_initialShowEmbed_%s;%s,%s";
        String commandUserId = event.getUser().getId();
        String targetId = targetUser.getId();
        Button editButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "edit", commandUserId, targetId),
                standardPhrases.getString("buttons.edit"));
        Button removeButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "remove", commandUserId, targetId),
                standardPhrases.getString("buttons.remove"));
        Button listButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "show", commandUserId, targetId),
                standardPhrases.getString("buttons.show"));


        if (commandExecutor.getPermissions().contains(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(initialShowNoteEmbed.build())
                    .setComponents(ActionRow.of(listButton, editButton, removeButton))
                    .setEphemeral(true)
                    .queue();
        } else {
            event.replyEmbeds(initialShowNoteEmbed.build())
                    .setComponents(ActionRow.of(listButton, editButton, removeButton.asDisabled()))
                    .setEphemeral(true)
                    .queue();
        }


    }

    private void addNoteCommand(@NotNull SlashCommandInteractionEvent event, User targetUser, Guild guild, ResourceBundle bundle, ResourceBundle standardPhrases) {
        event.deferReply(true).queue();
        String note = event.getOption("note", OptionMapping::getAsString); // Option is required

        if (note == null || note.isEmpty()) {
            event.getHook().editOriginal(bundle.getString("command.addNoteCommand.noteIsNullOrEmpty")).queue();
            return;
        }

        if (note.length() > 1024) {
            event.getHook().editOriginal(bundle.getString("command.addNoteCommand.noteIsToLong")).queue();
            return;
        }

        long addValue = db.putDataInNoteTable(guild.getIdLong(), targetUser.getIdLong(), event.getUser().getIdLong(), note, LocalDateTime.now());

        if (addValue > 0) {
            event.getHook().editOriginal(bundle.getString("command.addNoteCommand.success")).queue();
        } else if (addValue < 0) {
            event.getHook().editOriginal(String.format(standardPhrases.getString("replies.dbErrorReply"), addValue)).queue();
        } else {
            event.getHook().editOriginal(standardPhrases.getString("replies.noDataEdit"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        }
    }

    private void setEmbedFieldsAndGetModerators(
            EmbedBuilder showNoteEmbed, StringSelectMenu.Builder menuBuilder, int countFrom, @NotNull JSONObject notes) {
        int i = 0;
        for (String keyString : notes.keySet().stream().sorted().toList()) {
            countFrom--;
            if (countFrom < 0) {
                i++;
                JSONObject entry = notes.getJSONObject(keyString);
                String timeString = keyString.replace("T", " ").replaceAll(".\\d{5,}", " ");
                showNoteEmbed.addField(timeString + " from <!@" + entry.getLong("moderatorId") + ">", entry.getString("noteContent"), false);
                if (menuBuilder != null) menuBuilder.addOption(timeString,keyString);
                if (i == 25) break;
            }
        }
    }

    @Override
    public Class<?> getEventClass() {
        return this.getClass();
    }

    private enum ClickType {
        NEXT,
        BEFORE
    }

    private enum NoteEmbedActionType {
        SHOW("show"),
        EDIT("edit"),
        REMOVE("remove");
        private final String name;

        NoteEmbedActionType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
