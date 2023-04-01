package de.SparkArmy.jda.events.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
import de.SparkArmy.jda.utils.punishments.Punishment;
import de.SparkArmy.jda.utils.punishments.PunishmentType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class SoftbanSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "softban";
    }

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, ConfigController controller) {
        new Punishment(event, PunishmentType.SOFTBAN, controller);
    }
}
