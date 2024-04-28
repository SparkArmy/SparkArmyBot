package de.SparkArmy.jda.events;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.DatabaseSource;
import de.SparkArmy.jda.JdaApi;
import de.SparkArmy.jda.events.customEvents.commandEvents.*;
import de.SparkArmy.jda.events.customEvents.otherEvents.MessageEvents;
import de.SparkArmy.jda.events.customEvents.otherEvents.ModMailEvents;
import de.SparkArmy.jda.events.iEvent.IJDAEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        loadAnnotations();
        awaitJdaLoading();
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

    private final Set<IJDAEvent> eventClasses = ConcurrentHashMap.newKeySet();

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

    private void loadAnnotations() {
        try {
            Connection connection = DatabaseSource.connection();
            connection.prepareStatement("""
                    DELETE FROM botdata.tbljdaevents WHERE "jevupdated" = false;
                    """).execute();

            String packageName = "de.SparkArmy.jda.annotations.events";
            InputStream stream = ClassLoader.getSystemClassLoader()
                    .getResourceAsStream(packageName.replaceAll("[.]", "/"));
            if (stream == null) return;
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            reader.lines()
                    .filter(line -> line.endsWith(".class"))
                    .forEach(line -> {
                        try {
                            PreparedStatement prepStmt = connection.prepareStatement("""
                                    INSERT INTO botdata."tblJdaEventAnnotations" ("jeaJDAClass", "jeaAnnotation")
                                    VALUES (?,?) on conflict ("jeaJDAClass","jeaAnnotation") do nothing
                                    """);
                            prepStmt.setString(1, line.replace("JDA", ""));
                            prepStmt.setString(2, line);
                            prepStmt.executeUpdate();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            Class<?> clazz = Class.forName(packageName + "." + line.substring(0, line.lastIndexOf('.')), true, ClassLoader.getSystemClassLoader());
                            @SuppressWarnings("unchecked")
                            Class<? extends Annotation> aClass = (Class<? extends Annotation>) clazz;
                            loadEvents(aClass, connection);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadEvents(@NotNull Class<? extends Annotation> a, @NotNull Connection connection) {
        String aName = a.getSimpleName();
        try {
            connection.setAutoCommit(false);
            PreparedStatement registerEvent = connection.prepareStatement("""
                    INSERT INTO botdata."tbljdaevents" ("fk_jevjdaclass", "jevmethodname", "jevuserclassname", "jevupdated")
                     VALUES (?,?,?,?) on conflict ("jevmethodname", "jevuserclassname") do
                     UPDATE SET
                     "fk_jevjdaclass" = EXCLUDED."fk_jevjdaclass",
                     "jevmethodname" = EXCLUDED."jevmethodname",
                     "jevuserclassname" = EXCLUDED."jevuserclassname",
                     "jevupdated" = EXCLUDED."jevupdated";
                    """);
            for (IJDAEvent c : eventClasses) {
                for (Method m : c.getMethods(a)) {
                    registerEvent.setString(1, aName.replace("JDA", ""));
                    registerEvent.setString(2, m.getName());
                    registerEvent.setString(3, c.getEventClass().getSimpleName());
                    registerEvent.setBoolean(4, true);
                    registerEvent.executeUpdate();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    private void invokeMethod(@NotNull Method method, @NotNull IJDAEvent eventClass, Object... params) {
        try {
            Constructor<?> constructor = eventClass.getEventClass().getConstructor(this.getClass());
            method.invoke(constructor.newInstance(this), params);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {

    }

    public ConfigController getController() {
        return controller;
    }


    public JdaApi getApi() {
        return api;
    }
}
