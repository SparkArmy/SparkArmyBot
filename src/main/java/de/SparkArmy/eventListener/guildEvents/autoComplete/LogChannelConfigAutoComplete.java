package de.SparkArmy.eventListener.guildEvents.autoComplete;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class LogChannelConfigAutoComplete extends CustomEventListener {

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("log-channel-config")) return;
        Collection<String> types = new ArrayList<>();
        LogChannelType.getLogChannelTypes().forEach(x -> {
            if (x.equals(LogChannelType.UNKNOW)) return;
            types.add(x.getName());
        });
        event.replyChoiceStrings(types).queue();
    }
}
