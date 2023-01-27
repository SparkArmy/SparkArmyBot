package de.SparkArmy.commandListener.guildCommands.userCommands.moderation;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public class UserContextWarn extends CustomCommandListener {

    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("warn")) return;
        PunishmentUtil.executePunishment(event);
    }
}
