package de.SparkArmy.commandListener.guildCommands.messageCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Report extends CustomCommandListener{
    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (!event.getName().equals("report")) return;
        if (event.getGuild() == null) return;

        Member eventMember = event.getMember();
        Member targetMember = event.getTarget().getMember();

        if (eventMember == null || targetMember == null) return;

        if (targetMember.getUser().isBot() || targetMember.getUser().isSystem()) {
            event.reply("You can't report this message!").setEphemeral(true).queue();
            return;
        }


        String report = String.format("Message reported from %s (%s). The message sent from %s (%s)\nLink: %s",
                eventMember.getUser().getAsTag(),eventMember.getId(),
                targetMember.getUser().getAsTag(),targetMember.getId(),
                event.getTarget().getJumpUrl());

        ChannelUtil.logInLogChannel(report, event.getGuild(), LogChannelType.MOD);

        event.reply("Message was reported").setEphemeral(true).queue();
    }
}
