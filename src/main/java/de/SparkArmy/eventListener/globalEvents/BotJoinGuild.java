package de.SparkArmy.eventListener.globalEvents;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.PostgresConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import org.jetbrains.annotations.NotNull;

public class BotJoinGuild extends CustomEventListener {

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        guildJoin(guild);
    }

    private void guildJoin(Guild guild){
        if (guild == null) return;
        // create a guild config
        getGuildMainConfig(guild);
        // add the guild to database
        PostgresConnection.putDataInGuildTable(guild);
    }
}
