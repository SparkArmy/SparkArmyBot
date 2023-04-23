package de.SparkArmy.jda.events.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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
            case "add" -> addNoteCommand(event, targetUser, eventGuild);
            case "show" -> showNoteCommand(event, commandMember, targetUser, eventGuild);
            default -> {
                logger.warn(getName() + " has a default value in switch(subcommandName) with value: " + subcommandName);
                event.reply(bundle.getString("command.dispatchSlashEvent.defaultReply")).setEphemeral(true).queue();
            }
        }

    }

    private void showNoteCommand(@NotNull SlashCommandInteractionEvent event, Member commandExecutor, @NotNull User targetUser, @NotNull Guild guild) {
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());


        JSONObject notes = db.getDataFromNoteTable(targetUser.getIdLong(), guild.getIdLong());

        if (notes.isEmpty()) {
            event.reply(bundle.getString("command.showNoteCommand.notesIsEmpty")).setEphemeral(true).queue();
            return;
        }

        EmbedBuilder initialShowNoteEmbed = new EmbedBuilder();
        initialShowNoteEmbed.setTitle(String.format(bundle.getString("command.showNoteCommand.initialShowNoteEmbed.title"), targetUser.getName()));
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
        String targetId = targetUser.getId();
        Button editButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "edit", commandUserId, targetId),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.edit.name"));
        Button removeButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "remove", commandUserId, targetId),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.remove.name"));
        Button listButton = Button.of(ButtonStyle.SECONDARY,
                String.format(buttonIdPreset, "show", commandUserId, targetId),
                bundle.getString("command.showNoteCommand.initialShowNoteEmbed.field.show.name"));


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

    private void addNoteCommand(@NotNull SlashCommandInteractionEvent event, User targetUser, Guild guild) {
        ResourceBundle bundle = Util.getResourceBundle(getName(), event.getUserLocale());


        String note = event.getOption("note", OptionMapping::getAsString); // Option is required

        if (note == null || note.isEmpty()) {
            event.reply(bundle.getString("command.addNoteCommand.noteIsNullOrEmpty")).setEphemeral(true).queue();
            return;
        }

        if (note.length() > 1024) {
            event.reply(bundle.getString("command.addNoteCommand.noteIsToLong")).setEphemeral(true).queue();
            return;
        }


        if (db.putDataInNoteTable(note, targetUser.getIdLong(), event.getUser().getIdLong(), guild.getIdLong())) {
            event.reply(bundle.getString("command.addNoteCommand.success")).setEphemeral(true).queue();
        } else {
            event.reply(bundle.getString("command.addNoteCommand.putInDBFailed")).setEphemeral(true).queue();
        }
    }
}
