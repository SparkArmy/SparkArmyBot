package de.SparkArmy.eventListener.guildEvents.commands;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class SlashCommandListener extends CustomEventListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        String user = String.format("%s (%s)",event.getUser().getAsTag(),event.getUser().getId());
        ChannelUtil.logInLogChannel(user + " use " + event.getCommandString(),event.getGuild(), LogChannelType.SERVER);
    }
}
