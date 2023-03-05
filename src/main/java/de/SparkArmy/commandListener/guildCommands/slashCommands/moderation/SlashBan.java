package de.SparkArmy.commandListener.guildCommands.slashCommands.moderation;

import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashBan extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        PunishmentUtil.executePunishment(event);
        PunishmentUtil.bannedOrKickedUsers.add(event.getUser().getId() + "," + event.getId());
    }

    @Override
    public String getName() {
        return "ban";
    }

}
