package de.SparkArmy.eventListener.guildEvents.autoComplete;

import de.SparkArmy.eventListener.CustomEventListener;
import de.SparkArmy.utils.FileHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class FeedbackAutoComplete extends CustomEventListener {

    private final File directory = FileHandler.getDirectoryInUserDirectory("botstuff/autocomplete");

    @Contract(value = " -> new", pure = true)
    private @NotNull Collection<String> getFeedbackCategoryCollection() {
        if (directory == null) return new ArrayList<>();
        File file = FileHandler.getFileInDirectory(directory, "feedback.json");
        if (!file.exists()) return new ArrayList<>() {
        };
        String fileContentString = FileHandler.getFileContent(file);
        if (fileContentString == null) return new ArrayList<>();
        JSONObject content = new JSONObject(fileContentString);
        if (content.isNull("options")) return new ArrayList<>();
        Collection<String> values = new ArrayList<>();
        content.getJSONArray("options").toList().forEach(x -> values.add(x.toString()));
        return values;
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("feedback")) return;

        OptionMapping category = event.getOption("feedback-category");
        if (category == null) return;

        String string = category.getAsString().toLowerCase(Locale.ROOT);

        Collection<String> collection = getFeedbackCategoryCollection();

        if (collection.isEmpty()) {
            collection = new ArrayList<>() {{
                add("YouTube");
                add("Twitch");
                add("Community");
                add("Discord");
                add("Merch-Shop");
            }};
        }
        List<String> finalCollection = new ArrayList<>(collection.stream().filter(x -> x.toLowerCase(Locale.ROOT).startsWith(string)).toList());
        if (finalCollection.size() >= 24) {
            finalCollection.subList(24, finalCollection.size() - 1).clear();
        }
        event.replyChoiceStrings(finalCollection).queue();
    }
}
