package de.SparkArmy.commandListener.globalCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.FileHandler;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Logger;

public class ModmailCommand extends ListenerAdapter implements CustomCommandListener {

    private final Logger logger = MainUtil.logger;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        String rightEventName = "modmail";
        if (!Objects.equals(eventName, rightEventName)) return;



        @NonNls String idExtension = event.getUser().getId() + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis()));

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
        if (directory == null){
            logger.warning("MODMAIL: directory from modmail is null");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        if (!FileHandler.createFile(directory.getAbsolutePath(),idExtension + ".json")){
            logger.warning("MODMAIL: can't create a file in the modmail-directory");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        if (!FileHandler.writeValuesInFile(directory,idExtension + ".json",new JSONObject().put("user", event.getUser().getId()))){
            logger.warning("MODMAIL: can't write values in the file");
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }
        event.replyModal(modal).queue();
    }
}
