package de.SparkArmy.commandListener.guildCommands.userCommands.moderation;

import de.SparkArmy.commandListener.UserCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

public class UserContextMute extends UserCommand {

    @Override
    public void dispatch(UserContextInteractionEvent event, JDA jda, ConfigController controller) {
        PunishmentUtil.executePunishment(event);
    }

    @Override
    public String getName() {
        return "mute";
    }
}
