package de.SparkArmy.jda.events.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.util.ResourceBundle;

public class NoteSlashCommand extends CustomCommand {

    @Override
    public String getName() {
        return "note";
    }

    private Postgres db;

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, @NotNull ConfigController controller) {
        Logger logger = controller.getMain().getLogger();
        db = controller.getMain().getPostgres();
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());
        String subcommandName = event.getSubcommandName();

        //noinspection DataFlowIssue
        switch (subcommandName) {
            case "add" -> addNoteCommand(event);
            case "show" -> showNoteCommand(event);
            default -> {
                logger.warn(getName() + " has a default value in switch(subcommandName) with value: " + subcommandName);
                event.reply(bundle.getString("command.dispatchSlashEvent.defaultReply")).setEphemeral(true).queue();
            }
        }

    }

    private void showNoteCommand(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());

        User member = event.getOption("user", OptionMapping::getAsUser);


        @SuppressWarnings("DataFlowIssue") // Command is guild only and Option is required
        JSONObject notes = db.getDataFromNoteTable(member.getIdLong(), event.getGuild().getIdLong());

        if (notes.isEmpty()) {
            event.reply(bundle.getString("command.showNoteCommand.notesIsEmpty")).setEphemeral(true).queue();
            return;
        }

        EmbedBuilder initialShowNoteEmbed = new EmbedBuilder();
        initialShowNoteEmbed.setTitle(String.format(bundle.getString("command.showNoteCommand.initialShowNoteEmbed.title"), member.getName()));
        initialShowNoteEmbed.setDescription(bundle.getString("command.showNoteCommand.initialShowNoteEmbed.description"));
        initialShowNoteEmbed.addField(
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.show.name"),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.show.value"), true);
        initialShowNoteEmbed.addField(
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.edit.name"),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.edit.value"), true);
        initialShowNoteEmbed.addField(
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.remove.name"),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.remove.value"), true);

        String buttonIdPreset = "noteCommand_initialShowEmbed_%s;%s,%s";
        String commandUserId = event.getUser().getId();
        String targetId = member.getId();
        Button editButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "edit", commandUserId, targetId),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.edit.name"));
        Button removeButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "remove", commandUserId, targetId),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.remove.name"));
        Button listButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "show", commandUserId, targetId),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.show.name"));

        event.replyEmbeds(initialShowNoteEmbed.build())
                .setComponents(ActionRow.of(listButton, editButton, removeButton))
                .setEphemeral(true)
                .queue();
    }

    private void addNoteCommand(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());

        User targetUser = event.getOption("user", OptionMapping::getAsUser); // Option is required
        String note = event.getOption("note", OptionMapping::getAsString); // Option is required

        //noinspection DataFlowIssue
        if (note.length() > 1024) {
            event.reply(bundle.getString("command.addNoteCommand.noteIsToLong")).setEphemeral(true).queue();
            return;
        }

        //noinspection DataFlowIssue // Command is guild-only
        if (db.putDataInNoteTable(note, targetUser.getIdLong(), event.getUser().getIdLong(), event.getGuild().getIdLong())) {
            event.reply(bundle.getString("command.addNoteCommand.success")).setEphemeral(true).queue();
        } else {
            event.reply(bundle.getString("command.addNoteCommand.putInDBFailed")).setEphemeral(true).queue();
        }
    }
}
