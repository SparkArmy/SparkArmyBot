package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.PostgresConnection;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.jetbrains.annotations.NotNull;

public class MemberJoinEvent extends CustomEventListener {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        putMemberInDatabase(event);
//        stickyNicknames(event);
//        stickyRoles(event);
    }

    private void putMemberInDatabase(@NotNull GuildMemberJoinEvent event){
        PostgresConnection.putDataInMemberTable(event.getMember());
    }

//    TODO Sticky Roles function
//    private void stickyNicknames(@NotNull GuildMemberJoinEvent event){
//        String nickname = PostgresConnection.getLatestNicknameByDiscordUserId(event.getMember());
//        if (nickname == null) return;
//        event.getMember().modifyNickname(nickname).queue();
//        event.getUser().openPrivateChannel().complete().sendMessage("Your nickname was set to your old nickname: " + nickname).queue(null,
//                new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
//    }
//
//    private void stickyRoles(@NotNull GuildMemberJoinEvent event){
//        List<String> roleIds = SqlUtil.getRolesFromUser(event.getUser(), event.getGuild());
//        roleIds.forEach(x->{
//           Role role = event.getGuild().getRoleById(x);
//           if (role == null) return;
//           event.getGuild().addRoleToMember(event.getUser(),role).queue();
//        });
//    }
}
