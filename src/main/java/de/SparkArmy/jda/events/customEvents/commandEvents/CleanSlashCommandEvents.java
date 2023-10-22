package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.jda.events.annotations.interactions.JDASlashCommand;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

public class CleanSlashCommandEvents {

    public CleanSlashCommandEvents(@NotNull EventDispatcher ignoredDispatcher) {
    }

    @JDASlashCommand(name = "clean")
    public void initialCleanSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) return;
        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) return;
        switch (subcommandName) {
            case "all" -> allCleanSubcommand(event);
            case "last" -> lastCleanSubcommand(event);
        }
    }

    private void allCleanSubcommand(@NotNull SlashCommandInteractionEvent event) {
        Integer amount = event.getOption("count", 100, OptionMapping::getAsInt);
        User user = event.getOption("user", OptionMapping::getAsUser);

        event.getChannel().getHistory().retrievePast(amount)
                .map(x -> {
                    if (user != null) {
                        return x.stream().filter(y -> y.getAuthor().equals(user)).toList();
                    } else return x;
                })
                .map(x -> event.getChannel().purgeMessages(x))
                .queue();
    }

    private void lastCleanSubcommand(@NotNull SlashCommandInteractionEvent event) {
        Integer days = event.getOption("days", 5, OptionMapping::getAsInt);
        event.getChannel().getHistory().retrievePast(100)
                .map(x -> x.stream().filter(y -> y.getTimeCreated().isAfter(OffsetDateTime.now().minusDays(days))).toList())
                .map(x -> event.getChannel().purgeMessages(x))
                .queue();
    }
}
