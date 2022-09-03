package de.SparkArmy.eventListener.guildEvents.member;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateFlagsEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;

class LoggingEmbeds {

    static final Color color = new Color(0x1CA28E);

    public static @NotNull MessageEmbed memberRoleLogging(@NotNull Member member, @NotNull List<Role> roles, @NotNull GenericEvent event, User moderator){
        // Set a title by EventClass
        String title = event.getClass().getSimpleName().replace("GuildMember","").replace("Event","");
        // Get the roles as mention
        StringBuilder rolesString = new StringBuilder();
        roles.stream().filter(x->!x.isPublicRole()).forEach(x->rolesString.append(x.getAsMention()).append(","));
        rolesString.deleteCharAt(rolesString.length()-1);

        return new EmbedBuilder(){{
            setTitle(title);
            setAuthor(member.getEffectiveName(),null, member.getEffectiveAvatarUrl());
            setColor(color);
            setDescription(rolesString);
            setTimestamp(OffsetDateTime.now());
            // Add a moderator if moderator not null and moderator not equals member
            if (moderator != null && !moderator.equals(member.getUser())) addField("Moderator",String.format("%s (%s)",moderator.getAsTag(),moderator.getId()),false);
        }}.build();
    }

    public static @NotNull MessageEmbed memberAvatarUpdate(@NotNull User member){
        return new EmbedBuilder(){{
            setTitle("UserAvatarUpdate");
            setDescription("User updated his avatar");
            setImage(member.getEffectiveAvatar().getUrl());
            setAuthor(member.getAsTag());
            setColor(color);
            setTimestamp(OffsetDateTime.now());
        }}.build();
    }

    public static @NotNull MessageEmbed usernameUpdate(@NotNull UserUpdateNameEvent event){
        return new EmbedBuilder(){{
            setTitle("UsernameUpdate");
            setDescription("User updated his username");
            setAuthor(event.getUser().getAsTag(),null,event.getUser().getEffectiveAvatarUrl());
            setColor(color);
            addField("Old name", event.getOldName(), false);
            addField("New name", event.getNewName(), false);
        }}.build();
    }

    public static @NotNull MessageEmbed discriminatorUpdate(@NotNull UserUpdateDiscriminatorEvent event){
        return new EmbedBuilder(){{
            setTitle("UserDiscriminatorUpdate");
            setDescription("User updated his discriminator");
            setAuthor(event.getUser().getAsTag(),null,event.getUser().getEffectiveAvatarUrl());
            setColor(color);
            addField("Old discriminator", event.getOldDiscriminator(), false);
            addField("New discriminator", event.getNewDiscriminator(), false);
        }}.build();
    }

    public static @NotNull MessageEmbed userFlagUpdate(@NotNull UserUpdateFlagsEvent event){
        return new EmbedBuilder(){{
            setTitle("UserFlagsUpdate");
            setDescription("User updated his flags");
            setAuthor(event.getUser().getAsTag(),null,event.getUser().getEffectiveAvatarUrl());
            setColor(color);
            addField("Old flags",event.getOldFlags().stream().toList().toString(),false);
            addField("New flags",event.getNewFlags().stream().toList().toString(),false);
        }}.build();
    }

    public static @NotNull MessageEmbed nicknameUpdate(@NotNull GuildMemberUpdateNicknameEvent event){
        return new EmbedBuilder(){{
            setTitle("NicknameUpdate");
            setDescription("User updated his nickname");
            setAuthor(event.getUser().getAsTag(),null,event.getUser().getEffectiveAvatarUrl());
            setColor(color);
            addField("Old name", event.getOldNickname() != null ? event.getOldNickname() : event.getUser().getName(), false);
            addField("New name", event.getNewNickname() != null ? event.getNewNickname() : event.getMember().getEffectiveName(), false);
        }}.build();
    }

    public static @NotNull MessageEmbed pendingUpdate(GuildMemberUpdatePendingEvent event){
        return new EmbedBuilder(){{
            setTitle("PendingUpdate");
            setDescription("User accept the rules");
            setAuthor(event.getUser().getAsTag(),null,event.getUser().getEffectiveAvatarUrl());
            setColor(color);
        }}.build();
    }
}
