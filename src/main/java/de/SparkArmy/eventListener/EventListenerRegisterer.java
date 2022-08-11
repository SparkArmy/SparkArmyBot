package de.SparkArmy.eventListener;

import de.SparkArmy.eventListener.globalEvents.ModmailListener;
import de.SparkArmy.eventListener.guildEvents.Commands.PunishmentListener;
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

        this.events.forEach(this.jda::addEventListener);
    }
}
