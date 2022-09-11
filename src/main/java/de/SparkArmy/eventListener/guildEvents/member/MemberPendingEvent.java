package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import org.jetbrains.annotations.NotNull;

public class MemberPendingEvent extends CustomEventListener {
    @Override
    public void onGuildMemberUpdatePending(@NotNull GuildMemberUpdatePendingEvent event) {
        pendingLogging(event);
    }

    private void pendingLogging(@NotNull GuildMemberUpdatePendingEvent event){
        if (!event.getNewPending()) {
            ChannelUtil.logInLogChannel(LoggingEmbeds.pendingUpdate(event), event.getGuild(), LogChannelType.MEMBER);
        }
    }
}
