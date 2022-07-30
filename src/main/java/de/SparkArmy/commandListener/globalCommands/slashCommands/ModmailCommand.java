package de.SparkArmy.commandListener.globalCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public class ModmailCommand extends ListenerAdapter implements CustomCommandListener {


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        String rightEventName = "modmail";
        if (!Objects.equals(eventName, rightEventName)) return;



        @NonNls String idExtension = event.getUser().getId() + LocalDateTime.now();

        TextInput topic = TextInput.create("topic;" + idExtension,"Topic", TextInputStyle.SHORT)
                .setPlaceholder("Your Question")
                .setMinLength(10)
                .build();

        TextInput body = TextInput.create("body;" + idExtension,"Body",TextInputStyle.PARAGRAPH)
                .setPlaceholder("Continue your topic here")
                .setMinLength(10)
                .build();

        Modal modal = Modal.create(rightEventName + idExtension,"Modmail")
                .addActionRows(ActionRow.of(topic),ActionRow.of(body))
                .build();

        event.replyModal(modal).queue();
    }
}
