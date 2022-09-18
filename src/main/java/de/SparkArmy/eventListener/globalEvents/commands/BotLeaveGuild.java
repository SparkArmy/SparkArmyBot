package de.SparkArmy.eventListener.globalEvents.commands;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.SqlUtil;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import org.jetbrains.annotations.NotNull;

public class BotLeaveGuild extends CustomEventListener {
    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        SqlUtil.dropGuildDatabase(event.getGuild().getId());
    }
}
