package de.SparkArmy.eventListener.globalEvents;

import de.SparkArmy.eventListener.CustomEventListener;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ModmailModal extends ListenerAdapter implements CustomEventListener {

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        super.onModalInteraction(event);
    }
}
