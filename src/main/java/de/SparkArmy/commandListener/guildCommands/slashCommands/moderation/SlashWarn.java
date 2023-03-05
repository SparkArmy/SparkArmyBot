package de.SparkArmy.commandListener.guildCommands.slashCommands.moderation;

import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashWarn extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        PunishmentUtil.executePunishment(event);
    }

    @Override
    public String getName() {
        return "warn";
    }

}
