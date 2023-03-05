package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.jda.mediaOnlyUtils.MediaOnlyUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class MediaOnly extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        if (event.getGuild() == null) {
            event.reply("Please execute the command on a guild").setEphemeral(true).queue();
            return;
        }


        OptionMapping action = event.getOption("action");
        OptionMapping channel = event.getOption("channel");


        if (action == null && channel == null) {
            MediaOnlyUtil.sendOverviewEmbed(event);
        } else if (action != null && channel == null) {
            MediaOnlyUtil.sendActionEmbed(event);
        } else if (action == null) {
            MediaOnlyUtil.sendOverviewEmbed(event);
        } else {
            MediaOnlyUtil.sendChannelEmbed(event);
        }
    }

    @Override
    public String getName() {
        return "media-only";
    }

}
