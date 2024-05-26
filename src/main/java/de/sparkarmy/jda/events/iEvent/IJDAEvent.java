package de.sparkarmy.jda.events.iEvent;

import de.sparkarmy.jda.annotations.internal.JDAEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public interface IJDAEvent {
    Class<?> getEventClass();

    default Method @NotNull [] getMethods() {
        List<Method> methods = new ArrayList<>();
        for (Method m : getEventClass().getMethods()) {
            if (m.getAnnotation(JDAEvent.class) == null) continue;
            methods.add(m);
        }
        return methods.toArray(new Method[0]);
    }
}
