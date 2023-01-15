package de.SparkArmy.commandListener.guildCommands.slashCommands.moderation;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.PostgresConnection;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UserPunishments extends CustomCommandListener {

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

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("user-punishments")) return;

        OptionMapping userOption = event.getOption("target-member");
        OptionMapping typeOption = event.getOption("punishment-type");
        OptionMapping moderatorOption = event.getOption("target-moderator");

        if (userOption != null) {
            getPunishmentsFromUser(event, userOption, typeOption);
        } else if (moderatorOption != null) {
            getPunishmentsExecutedByModerator(event);
        } else {
            event.reply("Please give a target member").setEphemeral(true).queue();
        }
    }

    private void getPunishmentsExecutedByModerator(@NotNull SlashCommandInteractionEvent event) {
        event.reply("This function is not implement, please use the other option").setEphemeral(true).queue();
    }

    private void getPunishmentsFromUser(@NotNull SlashCommandInteractionEvent event, OptionMapping memberMapping, OptionMapping typeMapping) {
        event.deferReply(true).queue();
        JSONArray values;
        if (getPunishmentTypeFromOptionMapping(typeMapping).equals(PunishmentType.UNKNOW)) {
            values = PostgresConnection.getPunishmentDataByOffender(memberMapping.getAsMember());
        } else {
            values = PostgresConnection.getPunishmentDataByOffender(memberMapping.getAsMember(), getPunishmentTypeFromOptionMapping(typeMapping));
        }
        List<JSONObject> valuesAsJsonObject = new ArrayList<>();
        if (values == null) {
            sendOverviewEmbed(valuesAsJsonObject, event);
        } else {
            values.forEach(x -> {
                JSONObject jObj = (JSONObject) x;
                valuesAsJsonObject.add(jObj);
            });
            sendOverviewEmbed(valuesAsJsonObject, event);
        }
    }

    private PunishmentType getPunishmentTypeFromOptionMapping(OptionMapping mapping) {
        if (mapping == null) return PunishmentType.UNKNOW;
        return PunishmentType.getByName(mapping.getAsString());
    }

    private void sendOverviewEmbed(List<JSONObject> values, SlashCommandInteractionEvent event) {
        new Thread(() -> {
            if (values == null) {
                event.reply("Database not connected!").setEphemeral(true).queue();
                return;
            } else if (values.isEmpty()) {
                event.reply("For this user no entry's exist").setEphemeral(true).queue();
                return;
            }
            List<MessageEmbed> embeds = new ArrayList<>();
            OffsetDateTime t = OffsetDateTime.now();
            int i;
            int res = Math.ceilDiv(values.size(), 25);
            for (i = 0; i < res; i++) {
                List<JSONObject> sublist;
                if (values.size() > 24) {
                    sublist = values.subList(i, i + 24);
                } else {
                    sublist = values.subList(i, values.size());
                }
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("User Punishments");
                embedBuilder.setAuthor(jda.getSelfUser().getAsTag(), null, jda.getSelfUser().getEffectiveAvatarUrl());
                embedBuilder.setTimestamp(t);
                for (JSONObject o : sublist) {
                    User moderator = jda.retrieveUserById(o.get("modId").toString()).complete();
                    embedBuilder.addField(
                            new MessageEmbed.Field(

                                    o.get("punishment").toString() + " | " + o.get("timestamp").toString(),
                                    String.format(
                                            """
                                                    Reason: %s
                                                    Moderator: %s
                                                    """,
                                            o.get("reason").toString(), moderator != null ? moderator.getAsTag() : o.get("modId").toString()
                                    ),
                                    true)
                    );
                }
                embeds.add(embedBuilder.build());
            }
            event.getHook().editOriginalEmbeds(embeds).queue();
        }).start();


        // TODO Implement the moderator option
    }
}
