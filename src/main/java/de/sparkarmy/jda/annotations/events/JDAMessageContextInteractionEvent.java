package de.sparkarmy.jda.annotations.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JDAMessageContextInteractionEvent {
    String name();
}
