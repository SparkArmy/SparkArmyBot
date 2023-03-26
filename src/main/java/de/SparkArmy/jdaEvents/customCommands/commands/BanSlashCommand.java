package de.SparkArmy.jdaEvents.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jdaEvents.customCommands.CustomCommand;
import de.SparkArmy.utils.punishments.Punishment;
import de.SparkArmy.utils.punishments.PunishmentType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class BanSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "ban";
    }

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, @NotNull ConfigController controller) {
        // Create new Punishment
        new Punishment(event, PunishmentType.BAN, controller);
    }
}
