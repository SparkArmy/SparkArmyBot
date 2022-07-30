package de.SparkArmy.eventListener;

import de.SparkArmy.eventListener.globalEvents.ModmailListener;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;

public class EventListenerRegisterer {
    private final ArrayList<CustomEventListener> events = new ArrayList<>();
    private final JDA jda = MainUtil.jda;

    public EventListenerRegisterer() {
        registerEventListeners();
    }

    private void registerEventListeners() {
        this.events.add(
                new ModmailListener()
        );

        events.forEach(jda::addEventListener);
    }
}
