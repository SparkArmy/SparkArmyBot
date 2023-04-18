package de.SparkArmy.jda.events.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JDAStringMenu {
    String name() default "";

    String startWith() default "";
}
