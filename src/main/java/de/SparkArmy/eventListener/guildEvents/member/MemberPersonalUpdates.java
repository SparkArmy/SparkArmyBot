package de.SparkArmy.eventListener.guildEvents.member;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.ChannelUtil;
import de.SparkArmy.utils.LogChannelType;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateFlagsEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import org.jetbrains.annotations.NotNull;

public class MemberPersonalUpdates extends CustomEventListener {

    private final LogChannelType member = LogChannelType.MEMBER;

    @Override
    public void onGuildMemberUpdateAvatar(@NotNull GuildMemberUpdateAvatarEvent event) {
        ChannelUtil.logInLogChannel(LoggingEmbeds.memberAvatarUpdate(event.getUser()),event.getGuild(), member);
    }

    @Override
    public void onUserUpdateName(@NotNull UserUpdateNameEvent event) {
        jda.getGuilds().stream().filter(x->!x.equals(storageServer)).forEach(x->
                ChannelUtil.logInLogChannel(LoggingEmbeds.usernameUpdate(event),x, member));
    }

    @Override
    public void onUserUpdateAvatar(@NotNull UserUpdateAvatarEvent event) {
        jda.getGuilds().stream().filter(x->!x.equals(storageServer)).forEach(x->
                ChannelUtil.logInLogChannel(LoggingEmbeds.memberAvatarUpdate(event.getUser()),x,member));
    }

    @Override
    public void onUserUpdateDiscriminator(@NotNull UserUpdateDiscriminatorEvent event) {
        jda.getGuilds().stream().filter(x->!x.equals(storageServer)).forEach(x->
                ChannelUtil.logInLogChannel(LoggingEmbeds.discriminatorUpdate(event),x,member));
    }

    @Override
    public void onUserUpdateFlags(@NotNull UserUpdateFlagsEvent event) {
        jda.getGuilds().stream().filter(x->!x.equals(storageServer)).forEach(x->
                ChannelUtil.logInLogChannel(LoggingEmbeds.userFlagUpdate(event),x,member));
    }

}
