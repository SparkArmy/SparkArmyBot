package de.SparkArmy.utils.jda;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class JdaEventUtil {

    public static @Nullable SlashCommandInteractionEvent getSlashEvent(@NotNull Event event){
        if (!event.getClass().equals(SlashCommandInteractionEvent.class)) return null;
        return new SlashCommandInteractionEvent(event.getJDA(), event.getResponseNumber(), ((SlashCommandInteractionEvent) event).getInteraction());
    }

    public static @Nullable ButtonInteractionEvent getButtonEvent(@NotNull Event event){
        if (!event.getClass().equals(ButtonInteractionEvent.class)) return null;
        return new ButtonInteractionEvent(event.getJDA(), event.getResponseNumber(), ((ButtonInteractionEvent) event).getInteraction());
    }

    public static @Nullable ModalInteractionEvent getModalEvent(@NotNull Event event){
        if (!event.getClass().equals(ModalInteractionEvent.class)) return null;
        return new ModalInteractionEvent(event.getJDA(), event.getResponseNumber(), ((ModalInteractionEvent) event).getInteraction());
    }

    public static @Nullable SelectMenuInteractionEvent getSelectMenuEvent(@NotNull Event event){
        if (!event.getClass().equals(SelectMenuInteractionEvent.class)) return null;
        return new SelectMenuInteractionEvent(event.getJDA(), event.getResponseNumber(), ((SelectMenuInteractionEvent) event).getInteraction());
    }
}
