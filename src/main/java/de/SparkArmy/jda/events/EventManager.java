package de.SparkArmy.jda.events;

import de.SparkArmy.config.ConfigController;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.annotations.events.*;
import de.SparkArmy.jda.events.customEvents.commandEvents.*;
import de.SparkArmy.jda.events.customEvents.otherEvents.MessageEvents;
import de.SparkArmy.jda.events.customEvents.otherEvents.ModMailEvents;
import de.SparkArmy.jda.events.iEvent.IJDAEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventManager extends ListenerAdapter {

    private final JdaApi api;
    private final ShardManager shardManager;
    private final ConfigController controller;
    private final Logger logger;

    public EventManager(@NotNull JdaApi api) {
        this.api = api;
        this.shardManager = api.getShardManager();
        this.controller = api.getController();
        this.logger = api.getLogger();
        loadEventClasses();
        loadEventsAndAnnotations();
        awaitJdaLoading();
    }

    private void awaitJdaLoading() {
        for (int i = 0; i < shardManager.getShardsTotal(); i++) {
            try {
                JDA jda = shardManager.getShardById(i);
                if (jda == null) {
                    logger.error("Shard %d is null".formatted(i));
                    controller.main().systemExit(1);
                    return;
                }
                jda.awaitReady();
                logger.info("Shard %d is ready".formatted(i));
            } catch (InterruptedException e) {
                logger.error("Error in shard loading", e);
                controller.main().systemExit(1);
            }
        }
    }

    private final Set<IJDAEvent> eventClasses = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<String, ArrayList<Map<Method, IJDAEvent>>> methods = new ConcurrentHashMap<>();
    private final Set<Class<? extends Annotation>> annotationClasses = ConcurrentHashMap.newKeySet();

    private void loadEventClasses() {
        registerEventClass(new ArchiveSlashCommandEvents(this));
        registerEventClass(new CleanSlashCommandEvents(this));
        registerEventClass(new ConfigureSlashCommandEvents(this));
        registerEventClass(new FeedbackSlashCommandEvents(this));
        registerEventClass(new GeneralCommandEvents(this));
        registerEventClass(new NoteSlashCommandEvents(this));
        registerEventClass(new NotificationSlashCommandEvents(this));
        registerEventClass(new PunishmentCommandEvents(this));
        registerEventClass(new MessageEvents(this));
        registerEventClass(new ModMailEvents(this));
    }

    private void registerEventClass(IJDAEvent clazz) {
        eventClasses.add(clazz);
    }

    private void loadEventsAndAnnotations() {
        for (IJDAEvent c : eventClasses) {
            for (Method m : c.getMethods()) {
                List<Parameter> parameterList = Arrays.stream(MethodUtils.getAccessibleMethod(m).getParameters()).toList();
                String jdaClassName = parameterList.getFirst().getType().getSimpleName();
                ArrayList<Map<Method, IJDAEvent>> partTwo = new ArrayList<>();
                if (methods.containsKey(jdaClassName)) partTwo = methods.get(jdaClassName);
                partTwo.add(Map.of(m, c));
                methods.put(jdaClassName, partTwo);
            }
        }
        annotationClasses.add(JDAButtonInteractionEvent.class);
        annotationClasses.add(JDACommandAutoCompleteInteractionEvent.class);
        annotationClasses.add(JDAEntitySelectInteractionEvent.class);
        annotationClasses.add(JDAMessageContextInteractionEvent.class);
        annotationClasses.add(JDAModalInteractionEvent.class);
        annotationClasses.add(JDASlashCommandInteractionEvent.class);
        annotationClasses.add(JDAStringSelectInteractionEvent.class);
        annotationClasses.add(JDAUserContextInteractionEvent.class);

    }

    private void invokeMethod(@NotNull Method method, @NotNull IJDAEvent eventClass, GenericEvent event) {
        try {
            Constructor<?> constructor = eventClass.getEventClass().getConstructor(this.getClass());
            method.invoke(constructor.newInstance(this), event);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        // Get related Methods
        String jdaClassName = event.getClass().getSimpleName();
        ArrayList<Map<Method, IJDAEvent>> methodList = methods.get(jdaClassName);
        if (methodList == null) return; // Return is no method registered

        if (event instanceof GenericInteractionCreateEvent iEvent) {
            Class<? extends Annotation> aClass = null;

            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                if (annotationClass.getSimpleName().endsWith(jdaClassName)) aClass = annotationClass;
            }

            if (aClass == null) {
                throw new RuntimeException("Annotation is null");
            }
            for (Map<Method, IJDAEvent> map : methodList) {
                Method m = map.keySet().stream().toList().getFirst();
                Annotation a = m.getAnnotation(aClass);

                Object aNameObject = AnnotationUtils.getValue(a, "name");
                Object aStartWithObject = AnnotationUtils.getValue(a, "startWith");

                String objectValueAsString = String.valueOf(Objects.requireNonNullElse(aStartWithObject, aNameObject));

                switch (iEvent) {
                    case GenericComponentInteractionCreateEvent cEvent -> {
                        if (!cEvent.getComponentId().startsWith(objectValueAsString)) continue;
                        invokeMethod(m, map.get(m), cEvent);
                    }
                    case GenericCommandInteractionEvent cEvent -> {
                        if (!cEvent.getName().startsWith(objectValueAsString)) continue;
                        invokeMethod(m, map.get(m), cEvent);
                    }
                    case CommandAutoCompleteInteractionEvent aEvent -> {
                        if (!aEvent.getName().startsWith(objectValueAsString)) continue;
                        invokeMethod(m, map.get(m), aEvent);
                    }
                    case ModalInteractionEvent mEvent -> {
                        if (!mEvent.getModalId().startsWith(objectValueAsString)) continue;
                        invokeMethod(m, map.get(m), mEvent);
                    }
                    default -> {
                    }
                }
            }
        } else {
            for (Map<Method, IJDAEvent> map : methodList) {
                Method m = map.keySet().stream().toList().getFirst();
                invokeMethod(m, map.get(m), event);
            }
        }

    }

    public ConfigController getController() {
        return controller;
    }


    public JdaApi getApi() {
        return api;
    }
}
