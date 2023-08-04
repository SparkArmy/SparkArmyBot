package de.SparkArmy.jda.events.annotations.interactions;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JDAButton {
    String name() default "";

    String startWith() default "";
}
