package de.SparkArmy.commandListener.globalCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.FileHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ModmailCommand extends CustomCommandListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        String rightEventName = "modmail";
        if (!Objects.equals(eventName, rightEventName)) return;


        @NonNls String idExtension = event.getUser().getId() + "," + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));

        TextInput topic = TextInput.create("topic;" + idExtension,"Topic", TextInputStyle.SHORT)
                .setPlaceholder("Your Question")
                .setMinLength(10)
                .build();

        TextInput body = TextInput.create("body;" + idExtension,"Body",TextInputStyle.PARAGRAPH)
                .setPlaceholder("Continue your topic here")
                .setMinLength(10)
                .build();

        Modal modal = Modal.create(rightEventName + ";" + idExtension,"Modmail")
                .addActionRows(ActionRow.of(topic),ActionRow.of(body))
                .build();


        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/modmail");
        if (null == directory){
            this.logger.warning("MODMAIL: directory from modmail is null");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        if (!FileHandler.createFile(directory,idExtension + ".json")){
            this.logger.warning("MODMAIL: can't create a file in the modmail-directory");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        event.replyModal(modal).queue();
    }
}
