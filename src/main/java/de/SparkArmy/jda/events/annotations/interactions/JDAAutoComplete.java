package de.SparkArmy.jda.events.annotations.interactions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JDAAutoComplete {
    String commandName();
}
