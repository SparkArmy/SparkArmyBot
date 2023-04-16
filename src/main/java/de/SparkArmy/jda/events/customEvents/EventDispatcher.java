package de.SparkArmy.jda.events.customEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.events.annotations.JDAButton;
import de.SparkArmy.jda.events.customEvents.commandEvents.NoteSlashCommandEvents;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventDispatcher {

    private final ConfigController controller;
    private final Logger logger;
    private final JdaApi api;

    private final Set<Object> events = ConcurrentHashMap.newKeySet();

    public EventDispatcher(@NotNull JdaApi api) {
        this.api = api;
        this.controller = api.getController();
        this.logger = api.getLogger();
        registerEvents();
    }

    @SubscribeEvent
    public void buttonEvents(@NotNull ButtonInteractionEvent event) {
        for (Object o : events) {
            for (Method m : o.getClass().getDeclaredMethods()) {
                JDAButton button = m.getAnnotation(JDAButton.class);
                if (button != null && m.getParameterCount() == 1) {
                    try {
                        Constructor<?> constructor = o.getClass().getConstructor(this.getClass());
                        if (event.getComponentId().startsWith(button.startWith()))
                            m.invoke(constructor.newInstance(this), event);
                        else if (event.getComponentId().equals(button.name()))
                            m.invoke(constructor.newInstance(this), event);
                    } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                             NoSuchMethodException e) {
                        logger.error("Error to dispatch button event with component-id: " + event.getComponentId() + " in Method: " + m.getName(), e);
                    }
                }
            }
        }
    }

    private void registerEvents() {
        registerEvent(new NoteSlashCommandEvents(this));
    }

    private void registerEvent(Object o) {
        if (events.contains(o)) {
            logger.error("Event: \" " + o.getClass().getName() + " \" already registered");
            return;
        }
        events.add(o);
    }

    public ConfigController getController() {
        return controller;
    }

    public Logger getLogger() {
        return logger;
    }

    public JdaApi getApi() {
        return api;
    }
}
