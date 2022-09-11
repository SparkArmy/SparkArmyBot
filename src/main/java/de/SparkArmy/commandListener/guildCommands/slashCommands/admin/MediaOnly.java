package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.mediaOnlyUtils.MediaOnlyUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

public class MediaOnly extends CustomCommandListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("media-only")) return;
        if (event.getGuild() == null){
            event.reply("Please execute the command on a guild").setEphemeral(true).queue();
            return;
        }


        OptionMapping action = event.getOption("action");
        OptionMapping channel = event.getOption("channel");


        if (action == null && channel == null) {
            MediaOnlyUtil.sendOverviewEmbed(event);
        } else if (action != null && channel == null) {
            MediaOnlyUtil.sendActionEmbed(event);
        } else if (action == null){
            MediaOnlyUtil.sendOverviewEmbed(event);
        }else {
            MediaOnlyUtil.sendChannelEmbed(event);
        }
    }
}
