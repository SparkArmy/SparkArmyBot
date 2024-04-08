package de.SparkArmy.jda.events.iEvent;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public interface IJDAEvent {
    Class<?> getEventClass();

    default Method @NotNull [] getMethods(Class<? extends Annotation> annotatedClass) {
        List<Method> methods = new ArrayList<>();
        for (Method m : getEventClass().getMethods()) {
            if (m.getAnnotation(annotatedClass) == null) continue;
            methods.add(m);
        }
        return methods.toArray(new Method[0]);
    }
}
