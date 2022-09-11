package de.SparkArmy.eventListener.guildEvents.commands;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.jda.mediaOnlyUtils.MediaOnlyUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class MediaOnlyListener extends CustomEventListener {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        MediaOnlyUtil.buttonDispatcher(event);
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        MediaOnlyUtil.sendChannelEmbed(event);
    }

}
