package de.SparkArmy.commandListener.guildCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.punishmentUtils.PunishmentUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Ban extends CustomCommandListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("ban")) return;
        PunishmentUtils.executePunishment(event);
        PunishmentUtils.bannedOrKickedUsers.add(event.getUser().getId() + "," + event.getId());
    }
}
