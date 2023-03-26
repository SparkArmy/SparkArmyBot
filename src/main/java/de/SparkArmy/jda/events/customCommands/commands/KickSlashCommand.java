package de.SparkArmy.jda.events.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
import de.SparkArmy.jda.utils.punishments.Punishment;
import de.SparkArmy.jda.utils.punishments.PunishmentType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class KickSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public void dispatchSlashEvent(SlashCommandInteractionEvent event, ConfigController controller) {
        // Create new Punishment
        new Punishment(event, PunishmentType.KICK, controller);
    }
}
