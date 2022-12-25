package de.SparkArmy.commandListener.guildCommands.slashCommands.moderation;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.PostgresConnection;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
                .filter(x->!x.equals(PunishmentType.UNKNOW))
                .filter(x->x.getName().startsWith(event.getFocusedOption().getValue()))
                .toList().forEach(x->strings.add(x.getName()));
        event.replyChoiceStrings(strings).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("user-punishments")) return;

        OptionMapping userOption = event.getOption("target-user");
        OptionMapping typeOption = event.getOption("punishment-type");
        OptionMapping moderatorOption = event.getOption("target-moderator");

        if (userOption != null){
            getPunishmentsFromUser(event,userOption,typeOption);
        } else if (moderatorOption != null) {
            getPunishmentsExecutedByModerator(event);
        } else {
            event.reply("Please give a target member").setEphemeral(true).queue();
        }
    }

    private void getPunishmentsExecutedByModerator(@NotNull SlashCommandInteractionEvent event) {
        event.reply("This function is not implement, please use the other option").setEphemeral(true).queue();
    }

    private void getPunishmentsFromUser(SlashCommandInteractionEvent event,OptionMapping memberMapping,OptionMapping typeMapping) {
        JSONArray values;
        if (getPunishmentTypeFromOptionMapping(typeMapping).equals(PunishmentType.UNKNOW)){
            values = PostgresConnection.getPunishmentDataByOffender(memberMapping.getAsMember());
        } else {
            values = PostgresConnection.getPunishmentDataByOffender(memberMapping.getAsMember(),getPunishmentTypeFromOptionMapping(typeMapping));
        }
        sendOverviewEmbed(values,event);
    }

    private PunishmentType getPunishmentTypeFromOptionMapping(OptionMapping mapping){
        if (mapping == null) return PunishmentType.UNKNOW;
        return PunishmentType.getByName(mapping.getAsString());
    }

    private void sendOverviewEmbed(JSONArray values,SlashCommandInteractionEvent event){
        if (values == null){
            event.reply("Database not connected!").setEphemeral(true).queue();
            return;
        } else if (values.isEmpty()) {
            event.reply("For this user no entry's exist").setEphemeral(true).queue();
            return;
        }

        List<Object> valuesAsList = values.toList();

        // TODO Display more than 24 punishments
        // TODO Implement the moderator option

        List<Object> tempSublist;


        EmbedBuilder punishmentsEmbed = new EmbedBuilder();
        punishmentsEmbed.setTitle("Punishments from user");
        // Get the offender as user and set an author
//        User u = jda.getUserById(((JSONObject) tempSublist.get(0)).getString("mbrId"));
//        if (u != null) {
//            punishmentsEmbed.setAuthor(u.getAsTag(), null, u.getEffectiveAvatarUrl());
//        }

        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (int i = 0;i<values.length();i++){
            JSONObject punishment = values.getJSONObject(i);
            String title = String.format("%s || %s",punishment.getString("punishment"),punishment.get("timestamp").toString());
            String value = String.format("""
                    Moderator-Id: %s
                    Reason: %s
                    """,
                    punishment.get("modId").toString(),
                    punishment.get("reason").toString());

            fields.add(new MessageEmbed.Field(title,value,false));
        }

        List<MessageEmbed.Field> finalList;

        if (fields.size() > 24){
//          Collections.reverse(fields);
            finalList = fields.subList(0,24);
        }
        else{
            finalList = fields;
        }
        finalList.forEach(punishmentsEmbed::addField);
        punishmentsEmbed.setTimestamp(OffsetDateTime.now());
        event.replyEmbeds(punishmentsEmbed.build()).setEphemeral(true).queue();
    }
}
