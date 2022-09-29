package de.SparkArmy.eventListener.globalEvents;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.SqlUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildJoinedEvent;
import org.jetbrains.annotations.NotNull;

public class BotJoinGuild extends CustomEventListener {

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        guildJoin(guild);
    }

    @Override
    public void onUnavailableGuildJoined(@NotNull UnavailableGuildJoinedEvent event) {
       Guild guild = jda.getGuildById(event.getGuildId());
       guildJoin(guild);
    }

    private void guildJoin(Guild guild){
        getGuildMainConfig(guild);
        SqlUtil.createDatabaseAndTablesForGuild(guild);
        guild.getMembers().forEach(x->{
            SqlUtil.putUserDataInUserTable(guild,x);
            SqlUtil.putDataInRoleUpdateTable(guild,x,x.getRoles());
        });
    }
}
