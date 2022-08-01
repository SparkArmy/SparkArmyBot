package de.SparkArmy.eventListener;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.utils.MainUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public abstract class CustomEventListener extends ListenerAdapter {
    public ConfigController controller = MainUtil.controller;
    public Logger logger = MainUtil.logger;


    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        super.onModalInteraction(event);
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        super.onSelectMenuInteraction(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
    }
}
