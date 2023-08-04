package de.SparkArmy.jda.events.annotations.interactions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JDAModal {
    String name() default "";

    String startWith() default "";
}
