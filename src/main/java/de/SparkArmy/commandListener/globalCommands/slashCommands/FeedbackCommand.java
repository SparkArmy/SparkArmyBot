package de.SparkArmy.commandListener.globalCommands.slashCommands;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.FileHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class FeedbackCommand extends CustomCommandListener {

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

    private @Nullable File getFile() {
        if (directory == null) return null;
        File file = FileHandler.getFileInDirectory(directory, "feedback.json");
        if (!file.exists()) {
            FileHandler.createFile(directory, "feedback.json");
            FileHandler.writeValuesInFile(file, new JSONObject() {{
                put("options", new JSONArray());
            }});
        }
        return file;
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

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("feedback")) return;

        OptionMapping category = event.getOption("feedback-category");


        TextInput.Builder topic = TextInput.create("topic", "Topic", TextInputStyle.SHORT)
                .setPlaceholder("Your topic, mostly your feedback-category")
                .setMaxLength(241)
                .setRequired(true);

        if (category != null) {
            String categoryString = category.getAsString();

            if (categoryString.length() > 241){
                categoryString = "Please type in a shorter Topic";
            }


            String finalCategoryString = categoryString.toLowerCase(Locale.ROOT);
            boolean blacklistPhrase = blacklist.stream().anyMatch(x-> x.contentEquals(finalCategoryString) || finalCategoryString.contains(x));

            if (blacklistPhrase){
                event.reply("You can't send this feedback").setEphemeral(true).queue();
                return;
            }

            if (!getFeedbackCategoryCollection().contains(categoryString) && !categoryString.equals("Please type in a shorter Topic")) {
                File file = getFile();
                if (file != null) {
                    String contentString = FileHandler.getFileContent(file);
                    if (contentString != null) {

                        JSONObject content = new JSONObject(contentString);
                        JSONArray objects = content.optJSONArray("options");
                        objects.put(categoryString);
                        content.put("options", objects);
                        FileHandler.writeValuesInFile(file, content);
                    }
                }
            }
            topic.setValue(categoryString);
        }

        String placeholder = """
                In this field you can write all what you wish.
                "The content will be send to the moderation-team.
                """;

        TextInput text = TextInput.create("text", "Text", TextInputStyle.PARAGRAPH)
                .setPlaceholder(placeholder)
                .build();

        String modalId = String.format("feedback;%s", event.getUser().getId());

        Modal feedbackModal = Modal.create(modalId, "Feedback").addActionRows(
                ActionRow.of(topic.build()),
                ActionRow.of(text)
        ).build();

        event.replyModal(feedbackModal).queue();
    }

    private final List<String> blacklist = new ArrayList<>(){{
        add("hure");
        add("penis");
        add("nazi");
        add("sex");
        add("porno");
        add("hitler");
        add("sub schenken");
        add("bumsen");
        add("droge");
        add("drugs");
        add("fotze");
        add("führer");
        add("geh sterben");
        add("inzucht");
        add("mörder");
        add("nudes");
        add("pädophil");
        add("penner");
        add("pornhub");
        add("psycho");
        add("rassist");
        add("sieg heil");
        add("suizid");
        add("vergewaltigt");
        add("vollpfosten");
        add("vollspacko");
        add("volltrottel");
        add("wixxer");
        add("Zuhälter");
        add("virgin");
        add("incels");
        add("kanacke");
        add("loser");
        add("Kinderficker");
    }};
}
