package de.SparkArmy.jda.events.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class NicknameSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "nickname";
    }

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, ConfigController controller) {
        String subcommandName = event.getSubcommandName();
        ResourceBundle bundle = Util.getResourceBundle("nickname", event.getUserLocale());

        if (subcommandName == null) {
            event.reply(bundle.getString("command.dispatchSlashEvent.subcommandIsNull")).queue();
            return;
        }
        switch (subcommandName) {
            case "change" ->
            case "remove" ->
            default -> {

            }
        }
    }
}
