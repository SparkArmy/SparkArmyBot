package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.SqlUtil;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

public class MemberJoinEvent extends CustomEventListener {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        putMemberInDatabase(event);
        stickyNicknames(event);
    }

    private void putMemberInDatabase(@NotNull GuildMemberJoinEvent event){
        if (SqlUtil.isUserNotInUserTable(event.getGuild(), event.getMember())){
            SqlUtil.putUserDataInUserTable(event.getGuild(), event.getMember());
        }
    }

    private void stickyNicknames(GuildMemberJoinEvent event){
        String nickname = SqlUtil.getLatestNickname(event);
        event.getMember().modifyNickname(nickname).queue();
        event.getUser().openPrivateChannel().complete().sendMessage("Your nickname was set to your old nickname: " + nickname).queue(null,
                new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
    }
}
