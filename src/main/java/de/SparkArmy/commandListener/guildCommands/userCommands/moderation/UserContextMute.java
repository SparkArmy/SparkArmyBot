package de.SparkArmy.commandListener.guildCommands.userCommands.moderation;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public class UserContextMute extends CustomCommandListener {
    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("mute")) return;
        PunishmentUtil.executePunishment(event);
    }
}
