package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.ChannelUtil;
import de.SparkArmy.utils.LogChannelType;
import de.SparkArmy.utils.SqlUtil;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import org.jetbrains.annotations.NotNull;

public class MemberNicknameUpdate extends CustomEventListener {

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        SqlUtil.putNicknameInNicknameTable(event);
        ChannelUtil.logInLogChannel(LoggingEmbeds.nicknameUpdate(event),event.getGuild(), LogChannelType.MEMBER);
    }

}
