package de.SparkArmy.eventListener;

import de.SparkArmy.eventListener.globalEvents.BotJoinGuild;
import de.SparkArmy.eventListener.globalEvents.commands.FeedbackListener;
import de.SparkArmy.eventListener.globalEvents.commands.ModmailListener;
import de.SparkArmy.eventListener.guildEvents.channel.MediaOnlyFunction;
import de.SparkArmy.eventListener.guildEvents.commands.*;
import de.SparkArmy.eventListener.guildEvents.member.*;
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

        // Global Events
        events.add(new BotJoinGuild());

        // Listeners for GlobalCommandActions
        events.add(new ModmailListener());
        events.add(new FeedbackListener());

        // Listeners for GuildCommandActions (Buttons,Modals,etc.)
        events.add(new PunishmentListener());
        events.add(new ReactionRolesListener()); // Function for give/remove role from member implement in this class
        events.add(new MediaOnlyListener());
        events.add(new NotificationListener());
        events.add(new ModerationConfigListener());

        // EventLogging
        events.add(new SlashCommandListener());

        // ChannelRelatedEvents
        events.add(new MediaOnlyFunction());

        // MemberRelatedEvents
        events.add(new MemberLeaveEvent());
        events.add(new MemberJoinEvent());
        events.add(new MemberRoleLogging());
        events.add(new MemberPersonalUpdates());
        events.add(new MemberPendingEvent());
        events.add(new MemberTimeoutEvent());

        this.events.forEach(this.jda::addEventListener);
    }
}
