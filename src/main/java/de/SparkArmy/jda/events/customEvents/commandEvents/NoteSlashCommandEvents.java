package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.events.annotations.JDAButton;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

public class NoteSlashCommandEvents {

    private final ConfigController controller;
    private final Logger logger;
    private final JDA jda;

    private final Postgres db;

    final @NotNull Button nextButton(@NotNull ResourceBundle bundle, String @NotNull [] ids, int count) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_showNoteEmbed_next;%s,%s;%d", ids[0], ids[1], count),
                bundle.getString("events.showEmbedButtons.next.label"));
    }

    final @NotNull Button beforeButton(@NotNull ResourceBundle bundle, String @NotNull [] ids, int count) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_showNoteEmbed_before;%s,%s;%d", ids[0], ids[1], count),
                bundle.getString("events.showEmbedButtons.before.label"));
    }

    final @NotNull Button closeButton(@NotNull ResourceBundle bundle, String @NotNull [] ids) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_showNoteEmbed_close;%s", ids[0]),
                bundle.getString("events.showEmbedButtons.close.label"));
    }

    public NoteSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
        this.logger = dispatcher.getLogger();
        this.jda = dispatcher.getApi().getJda();
        this.db = controller.getMain().getPostgres();
    }

    @JDAButton(startWith = "noteCommand_initialShowEmbed_show")
    public void showButtonClickEvent(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        String commandUserId = ids[0];
        String targetUserId = ids[1];
        if (!event.getUser().getId().equals(commandUserId)) return;

        @SuppressWarnings("DataFlowIssue") // Event triggered by guild-only interaction
        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(targetUserId), event.getGuild().getIdLong());

        ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());

        EmbedBuilder showNoteEmbed = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
        showNoteEmbed.setDescription(bundle.getString("events.showButtonClickEvent.showNoteEmbed.description"));
        showNoteEmbed.clearFields();

        RestAction.allOf(setEmbedFieldsAndGetModerators(showNoteEmbed, 0, notes)).mapToResult()
                .queue(x -> {
                    ActionRow actionRow;

                    if (notes.length() > 25) {
                        actionRow = ActionRow.of(nextButton(bundle, ids, 25), closeButton(bundle, ids));
                    } else {
                        actionRow = ActionRow.of(closeButton(bundle, ids));
                    }

                    event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow).queue();
                });
    }

    @JDAButton(startWith = "noteCommand_showNoteEmbed_close")
    public void closeShowEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        String[] splitId = event.getComponentId().split(";");
        String commandUserId = splitId[1];
        if (!event.getUser().getId().equals(commandUserId)) return;

        ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());
        event.deferEdit().setEmbeds().setComponents().setContent(bundle.getString("events.closeShowEmbedClickEvent.closeMessage")).queue();

    }

    @SuppressWarnings("DuplicatedCode") // first 12 lines are the as same as beforeShowEmbedClickEvent
    @JDAButton(startWith = "noteCommand_showNoteEmbed_next")
    public void nextShowEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        sendShowEmbed(event, ClickType.NEXT, NoteEmbedActionType.SHOW);
    }

    @SuppressWarnings("DuplicatedCode") // first 12 lines are the as same as nextShowEmbedClickEvent
    @JDAButton(startWith = "noteCommand_showNoteEmbed_before")
    public void beforeShowEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        sendShowEmbed(event, ClickType.BEFORE, NoteEmbedActionType.SHOW);
    }

    private void sendShowEmbed(@NotNull ButtonInteractionEvent event, ClickType clicktype, NoteEmbedActionType actionType) {
        event.deferEdit().queue();
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        String targetUserId = ids[1];
        int count = Integer.parseInt(splitId[2]);
        if (!ids[0].equals(event.getUser().getId())) return;

        EmbedBuilder showNoteEmbed = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
        showNoteEmbed.clearFields();

        @SuppressWarnings("DataFlowIssue") // Parent event is guild-only
        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(targetUserId), event.getGuild().getIdLong());

        RestAction.allOf(setEmbedFieldsAndGetModerators(showNoteEmbed, count, notes)).mapToResult()
                .queue(x -> {
                    ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());
                    ActionRow actionRow;
                    int notesSize = notes.keySet().size();
                    if (clicktype.equals(ClickType.BEFORE)) {
                        if (count - 25 > 0) {
                            actionRow = ActionRow.of(
                                    beforeButton(bundle, ids, count - 25),
                                    nextButton(bundle, ids, Math.min(notesSize - count, 25)),
                                    closeButton(bundle, ids));
                        } else {
                            actionRow = ActionRow.of(
                                    nextButton(bundle, ids, Math.min(notesSize - count, 25)),
                                    closeButton(bundle, ids));
                        }
                    } else {
                        if (notesSize - count > 1) {
                            actionRow = ActionRow.of(
                                    beforeButton(bundle, ids, count - 25),
                                    nextButton(bundle, ids, Math.min(notesSize - count, 25)),
                                    closeButton(bundle, ids));
                        } else {
                            actionRow = ActionRow.of(
                                    beforeButton(bundle, ids, count - 25),
                                    closeButton(bundle, ids));
                        }
                    }

                    switch (actionType) {

                        case SHOW ->
                                event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow).queue();
                        case EDIT -> {
                        }
                        case REMOVE -> {
                        }
                    }


                });
    }


    private @NotNull Collection<RestAction<User>> setEmbedFieldsAndGetModerators(EmbedBuilder showNoteEmbed, int countFrom, @NotNull JSONObject notes) {
        Collection<RestAction<User>> restActions = new ArrayList<>();
        int i = 0;

        for (String keyString : notes.keySet().stream().sorted().toList()) {
            countFrom--;
            if (countFrom < 0) {
                i++;
                JSONObject entry = notes.getJSONObject(keyString);
                String timeString = keyString.replace("T", " ").replaceAll(".\\d{5,}", " ");
                restActions.add(jda.retrieveUserById(entry.getLong("moderatorId"))
                        .onSuccess(user -> showNoteEmbed.addField(timeString + " from " + user.getAsTag(), entry.getString("noteContent"), false)));
                if (i == 25) break;
            }
        }
        return restActions;
    }

    private enum ClickType {
        NEXT,
        BEFORE
    }

    private enum NoteEmbedActionType {
        SHOW,
        EDIT,
        REMOVE
    }

}
