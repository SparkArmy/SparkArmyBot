package de.SparkArmy.commandListener.globalCommands.slashCommands;

import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.LoggingMarker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ModmailCommand extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        if (event.isFromGuild()) {
            event.reply("Please use this command in a private channel with the bot").setEphemeral(true).queue();
            return;
        }


        @NonNls String idExtension = event.getUser().getId() + "," + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));

        TextInput topic = TextInput.create("topic;" + idExtension, "Topic", TextInputStyle.SHORT)
                .setPlaceholder("Your Question")
                .setMinLength(10)
                .build();

        TextInput body = TextInput.create("body;" + idExtension, "Body", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Continue your topic here")
                .setMinLength(10)
                .build();

        Modal modal = Modal.create(event.getName() + ";" + idExtension, "Modmail")
                .addComponents(ActionRow.of(topic), ActionRow.of(body))
                .build();


        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/modmail");
        if (null == directory) {
            controller.getMain().logger.warn(LoggingMarker.MODMAIL, "directory from modmail is null");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        if (!FileHandler.createFile(directory, idExtension + ".json")) {
            controller.getMain().logger.warn(LoggingMarker.MODMAIL, "can't create a file in the modmail-directory");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        event.replyModal(modal).queue();
    }

    @Override
    public String getName() {
        return "modmail";
    }
}
