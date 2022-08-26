package de.SparkArmy.eventListener;

import de.SparkArmy.eventListener.globalEvents.ModmailListener;
import de.SparkArmy.eventListener.guildEvents.channel.MediaOnlyFunction;
import de.SparkArmy.eventListener.guildEvents.commands.*;
import de.SparkArmy.eventListener.guildEvents.eventLogging.GuildMemberLeaveLogging;
import de.SparkArmy.eventListener.guildEvents.eventLogging.SlashCommandListener;
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

        // Listeners for CommandActions (Buttons,Modals,etc.)
        events.add(new ModmailListener());
        events.add(new PunishmentListener());
        events.add(new ReactionRolesListener()); // Function for give/remove role from member implement in this class
        events.add(new MediaOnlyListener());
        events.add(new NotificationListener());

        // EventLogging
        events.add(new SlashCommandListener());
        events.add(new GuildMemberLeaveLogging());

        // ChannelRelatedEvents
        events.add(new MediaOnlyFunction());

        this.events.forEach(this.jda::addEventListener);
    }
}
