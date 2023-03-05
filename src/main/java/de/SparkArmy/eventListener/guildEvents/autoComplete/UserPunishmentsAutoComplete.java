package de.SparkArmy.eventListener.guildEvents.autoComplete;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentType;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class UserPunishmentsAutoComplete extends CustomEventListener {

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("user-punishments")) return;

        Collection<String> strings = new ArrayList<>();
        Arrays.stream(PunishmentType.values())
                .filter(x -> !x.equals(PunishmentType.UNKNOW))
                .filter(x -> x.getName().startsWith(event.getFocusedOption().getValue()))
                .toList().forEach(x -> strings.add(x.getName()));
        event.replyChoiceStrings(strings).queue();
    }
}
