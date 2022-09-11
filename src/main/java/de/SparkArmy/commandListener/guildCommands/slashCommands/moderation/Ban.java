package de.SparkArmy.commandListener.guildCommands.slashCommands.moderation;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Ban extends CustomCommandListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("ban")) return;
        PunishmentUtil.executePunishment(event);
        PunishmentUtil.bannedOrKickedUsers.add(event.getUser().getId() + "," + event.getId());
    }
}
