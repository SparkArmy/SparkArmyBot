package de.SparkArmy.jda.events.customEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.events.annotations.JDAButton;
import de.SparkArmy.jda.events.annotations.JDAEntityMenu;
import de.SparkArmy.jda.events.annotations.JDAModal;
import de.SparkArmy.jda.events.annotations.JDAStringMenu;
import de.SparkArmy.jda.events.customEvents.commandEvents.NoteSlashCommandEvents;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
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
                        logger.error("Error to dispatch buttonEvent with component-id: " + event.getComponentId() + " in Method: " + m.getName(), e);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void selectEvents(GenericSelectMenuInteractionEvent<?, ?> event) {
        for (Object o : events) {
            if (event instanceof StringSelectInteractionEvent) {
                for (Method m : o.getClass().getDeclaredMethods()) {
                    JDAStringMenu stringMenu = m.getAnnotation(JDAStringMenu.class);
                    if (stringMenu != null && m.getParameterCount() == 1) {
                        try {
                            Constructor<?> constructor = o.getClass().getConstructor(this.getClass());
                            if (event.getComponentId().startsWith(stringMenu.startWith()))
                                m.invoke(constructor.newInstance(this), event);
                            else if (event.getComponentId().equals(stringMenu.name()))
                                m.invoke(constructor.newInstance(this), event);
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                                 NoSuchMethodException e) {
                            logger.error("Error to dispatch stringSelectEvent with component-id: " + event.getComponentId() + " in Method: " + m.getName(), e);
                        }
                    }
                }
            } else if (event instanceof EntitySelectInteractionEvent) {
                for (Method m : o.getClass().getDeclaredMethods()) {
                    JDAEntityMenu stringMenu = m.getAnnotation(JDAEntityMenu.class);
                    if (stringMenu != null && m.getParameterCount() == 1) {
                        try {
                            Constructor<?> constructor = o.getClass().getConstructor(this.getClass());
                            if (event.getComponentId().startsWith(stringMenu.startWith()))
                                m.invoke(constructor.newInstance(this), event);
                            else if (event.getComponentId().equals(stringMenu.name()))
                                m.invoke(constructor.newInstance(this), event);
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                                 NoSuchMethodException e) {
                            logger.error("Error to dispatch entitySelectEvent with component-id: " + event.getComponentId() + " in Method: " + m.getName(), e);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void modalEvents(ModalInteractionEvent event) {
        for (Object o : events) {
            for (Method m : o.getClass().getDeclaredMethods()) {
                JDAModal stringMenu = m.getAnnotation(JDAModal.class);
                if (stringMenu != null && m.getParameterCount() == 1) {
                    try {
                        Constructor<?> constructor = o.getClass().getConstructor(this.getClass());
                        if (event.getModalId().startsWith(stringMenu.startWith()))
                            m.invoke(constructor.newInstance(this), event);
                        else if (event.getModalId().equals(stringMenu.name()))
                            m.invoke(constructor.newInstance(this), event);
                    } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                             NoSuchMethodException e) {
                        logger.error("Error to dispatch entitySelectEvent with component-id: " + event.getModalId() + " in Method: " + m.getName(), e);
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
