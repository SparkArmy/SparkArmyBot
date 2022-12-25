package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.PostgresConnection;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import org.jetbrains.annotations.NotNull;

public class MemberNicknameUpdate extends CustomEventListener {

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        PostgresConnection.putDataInNicknameTable(event.getMember());
        ChannelUtil.logInLogChannel(LoggingEmbeds.nicknameUpdate(event),event.getGuild(), LogChannelType.MEMBER);
    }
}
