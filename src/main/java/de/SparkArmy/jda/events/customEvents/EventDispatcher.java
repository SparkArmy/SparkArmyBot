package de.SparkArmy.jda.events.customEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.events.annotations.interactions.*;
import de.SparkArmy.jda.events.customEvents.commandEvents.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventDispatcher {

    private final ShardManager shardManager;
    private final ConfigController controller;
    private final Logger logger;
    private final JdaApi api;

    private final Set<Object> events = ConcurrentHashMap.newKeySet();


    public EventDispatcher(@NotNull JdaApi api) {
        this.api = api;
        this.controller = api.getController();
        this.logger = api.getLogger();
        this.shardManager = api.getShardManager();
        awaitJdaLoading();
        registerEvents();
    }

    private void awaitJdaLoading() {
        for (int i = 0; i < shardManager.getShardsTotal(); i++) {
            try {
                JDA jda = shardManager.getShardById(i);
                if (jda == null) {
                    logger.error("Shard %d is null".formatted(i));
                    controller.getMain().systemExit(1);
                    return;
                }
                jda.awaitReady();
                logger.info("Shard %d is ready".formatted(i));
            } catch (InterruptedException e) {
                logger.error("Error in shard loading", e);
                controller.getMain().systemExit(1);
            }
        }
    }

    @SubscribeEvent
    public void dispatchEvent(GenericEvent event) {
        if (event instanceof GenericInteractionCreateEvent) {
            interactionEvents((GenericInteractionCreateEvent) event);
        }
    }

    private void interactionEvents(GenericInteractionCreateEvent event) {
        for (Object o : events) {
            for (Method m : o.getClass().getMethods()) {
                try {
                    Constructor<?> constructor = o.getClass().getConstructor(this.getClass());
                    if (event instanceof ButtonInteractionEvent) {
                        JDAButton annotation = m.getAnnotation(JDAButton.class);
                        if (annotation != null && m.getParameterCount() == 1) {
                            String componentId = ((ButtonInteractionEvent) event).getComponentId();
                            if (componentId.startsWith(annotation.startWith())) {
                                m.invoke(constructor.newInstance(this), event);
                            } else if (componentId.equals(annotation.name())) {
                                m.invoke(constructor.newInstance(this), event);
                            }
                        }
                    } else if (event instanceof CommandAutoCompleteInteractionEvent) {
                        JDAAutoComplete annotation = m.getAnnotation(JDAAutoComplete.class);
                        if (annotation != null && m.getParameterCount() == 1) {
                            if (((CommandAutoCompleteInteractionEvent) event).getName().equals(annotation.commandName())) {
                                m.invoke(constructor.newInstance(this), event);
                            }
                        }
                    } else if (event instanceof EntitySelectInteractionEvent) {
                        JDAEntityMenu annotation = m.getAnnotation(JDAEntityMenu.class);
                        String componentId = ((EntitySelectInteractionEvent) event).getComponentId();
                        if (annotation != null && m.getParameterCount() == 1) {
                            if (componentId.startsWith(annotation.startWith())) {
                                m.invoke(constructor.newInstance(this), event);
                            } else if (componentId.equals(annotation.name())) {
                                m.invoke(constructor.newInstance(this), event);
                            }
                        }
                    } else if (event instanceof MessageContextInteractionEvent) {
                        JDAMessageCommand annotation = m.getAnnotation(JDAMessageCommand.class);
                        if (annotation != null && m.getParameterCount() == 1) {
                            if (((MessageContextInteractionEvent) event).getName().equals(annotation.name())) {
                                m.invoke(constructor.newInstance(this), event);
                            }
                        }
                    } else if (event instanceof ModalInteractionEvent) {
                        JDAModal annotation = m.getAnnotation(JDAModal.class);
                        String componentId = ((ModalInteractionEvent) event).getModalId();
                        if (annotation != null && m.getParameterCount() == 1) {
                            if (componentId.startsWith(annotation.startWith())) {
                                m.invoke(constructor.newInstance(this), event);
                            } else if (componentId.equals(annotation.name())) {
                                m.invoke(constructor.newInstance(this), event);
                            }
                        }
                    } else if (event instanceof SlashCommandInteractionEvent) {
                        JDASlashCommand annotation = m.getAnnotation(JDASlashCommand.class);
                        if (annotation != null && m.getParameterCount() == 1) {
                            if (((SlashCommandInteractionEvent) event).getName().equals(annotation.name())) {
                                m.invoke(constructor.newInstance(this), event);
                            }
                        }
                    } else if (event instanceof StringSelectInteractionEvent) {
                        JDAStringMenu annotation = m.getAnnotation(JDAStringMenu.class);
                        String componentId = ((StringSelectInteractionEvent) event).getComponentId();
                        if (annotation != null && m.getParameterCount() == 1) {
                            if (componentId.startsWith(annotation.startWith())) {
                                m.invoke(constructor.newInstance(this), event);
                            } else if (componentId.equals(annotation.name())) {
                                m.invoke(constructor.newInstance(this), event);
                            }
                        }
                    } else if (event instanceof UserContextInteractionEvent) {
                        JDAUserCommand annotation = m.getAnnotation(JDAUserCommand.class);
                        if (annotation != null && m.getParameterCount() == 1) {
                            if (((UserContextInteractionEvent) event).getName().equals(annotation.name())) {
                                m.invoke(constructor.newInstance(this), event);
                            }
                        }
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                         InstantiationException e) {
                    logger.error("Error to dispatch interactionEvent in %s | %s".formatted(o.getClass().getName(), m.getName()), e);
                }
            }
        }
    }


    private void registerEvents() {
        registerEvent(new ArchiveSlashCommandEvents(this));
        registerEvent(new GeneralCommandEvents(this));
        registerEvent(new NicknameSlashCommandEvents(this));
        registerEvent(new NoteSlashCommandEvents(this));
        registerEvent(new PunishmentCommandEvents(this));
        registerEvent(new NotificationSlashCommandEvents(this));
        registerEvent(new CleanSlashCommandEvents(this));
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
