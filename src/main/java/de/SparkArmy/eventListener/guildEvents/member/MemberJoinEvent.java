package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.SqlUtil;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.jetbrains.annotations.NotNull;

public class MemberJoinEvent extends CustomEventListener {
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        putMemberInDatabase(event);
    }

    private void putMemberInDatabase(@NotNull GuildMemberJoinEvent event){
        if (SqlUtil.isUserNotInUserTable(event.getGuild(), event.getMember())){
            SqlUtil.putUserDataInUserTable(event.getGuild(), event.getMember());
        }
    }
}
