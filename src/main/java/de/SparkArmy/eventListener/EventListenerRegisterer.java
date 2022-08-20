package de.SparkArmy.eventListener;

import de.SparkArmy.eventListener.globalEvents.ModmailListener;
import de.SparkArmy.eventListener.guildEvents.commands.MediaOnlyListener;
import de.SparkArmy.eventListener.guildEvents.commands.PunishmentListener;
import de.SparkArmy.eventListener.guildEvents.commands.ReactionRolesListener;
import de.SparkArmy.eventListener.guildEvents.commands.SlashCommandListener;
import de.SparkArmy.eventListener.guildEvents.member.GuildMemberLeaveLogging;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;

public class EventListenerRegisterer {
    private final ArrayList<CustomEventListener> events = new ArrayList<>();
    private final JDA jda = MainUtil.jda;

    public EventListenerRegisterer() {
        this.registerEventListeners();
    }

    private void registerEventListeners() {
        events.add(new ModmailListener());
        events.add(new PunishmentListener());
        events.add(new GuildMemberLeaveLogging());
        events.add(new ReactionRolesListener());
        events.add(new MediaOnlyListener());

        // EventLogging
        events.add(new SlashCommandListener());

        this.events.forEach(this.jda::addEventListener);
    }
}
