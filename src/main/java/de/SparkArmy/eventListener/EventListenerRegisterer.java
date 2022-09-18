package de.SparkArmy.eventListener;

import de.SparkArmy.eventListener.globalEvents.BotJoinGuild;
import de.SparkArmy.eventListener.globalEvents.commands.BotLeaveGuild;
import de.SparkArmy.eventListener.globalEvents.commands.FeedbackListener;
import de.SparkArmy.eventListener.globalEvents.commands.ModmailListener;
import de.SparkArmy.eventListener.guildEvents.channel.MediaOnlyFunction;
import de.SparkArmy.eventListener.guildEvents.commands.*;
import de.SparkArmy.eventListener.guildEvents.member.*;
import de.SparkArmy.eventListener.guildEvents.message.MessageDelete;
import de.SparkArmy.eventListener.guildEvents.message.MessageReceive;
import de.SparkArmy.eventListener.guildEvents.message.MessageUpdate;
import de.SparkArmy.eventListener.guildEvents.message.Reactions;
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
        events.add(new BotLeaveGuild());

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
        events.add(new MemberRoleUpdates());
        events.add(new MemberPersonalUpdates());
        events.add(new MemberPendingEvent());
        events.add(new MemberTimeoutEvent());
        events.add(new MemberNicknameUpdate());

        // Message Events
        events.add(new MessageReceive());
        events.add(new MessageUpdate());
        events.add(new MessageDelete());
        events.add(new Reactions());


        this.events.forEach(this.jda::addEventListener);
    }
}
