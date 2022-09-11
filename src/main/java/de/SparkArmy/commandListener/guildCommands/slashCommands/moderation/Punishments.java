package de.SparkArmy.commandListener.guildCommands.slashCommands.moderation;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.SqlUtil;
import de.SparkArmy.utils.jda.punishmentUtils.PunishmentType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Punishments extends CustomCommandListener {

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

        if (userOption == null){
            event.reply("Please write in target-user a value").setEphemeral(true).queue();
            return;
        }

        PunishmentType type = null;
        if (typeOption != null){
            type = PunishmentType.getByName(typeOption.getAsString());
            if (type == PunishmentType.UNKNOW){
                event.reply("Please give a valid type-option").setEphemeral(true).queue();
                return;
            }
        }

        JSONArray values = SqlUtil.getPunishmentDataFromUser(event.getGuild(), userOption.getAsUser(), type);
        if (values == null){
            event.reply("SQL is disabled").setEphemeral(true).queue();
            return;
        }
        if (values.isEmpty()){
            event.reply("There is no entry for this user").setEphemeral(true).queue();
            return;
        }

        List<MessageEmbed.Field> fields = new ArrayList<>();
        for (int i = 0;i<values.length();i++){
            JSONObject punishment = values.getJSONObject(i);
            String title = String.format("%s || %s",punishment.getString("id"),punishment.getString("punishment"));
            String value = String.format("""
                    Moderator: %s
                    Time: %s
                    """,
                    punishment.getString("moderatorId"),
                    punishment.getString("time"));

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

        EmbedBuilder showEmbed = new EmbedBuilder();
        showEmbed.setTitle("Punishments");
        showEmbed.setDescription("The punishments from " + userOption.getAsUser().getAsTag());
        finalList.forEach(showEmbed::addField);

        event.replyEmbeds(showEmbed.build()).setEphemeral(true).queue();
    }
}
