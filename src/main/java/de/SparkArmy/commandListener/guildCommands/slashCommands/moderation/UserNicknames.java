package de.SparkArmy.commandListener.guildCommands.slashCommands.moderation;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.SqlUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.OffsetDateTime;

public class UserNicknames extends CustomCommandListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("user-nicknames")) return;
        if (event.getGuild() == null) {
            event.reply("Please use this command in a guild-channel").setEphemeral(true).queue();
            return;
        }

        OptionMapping targetUserOption = event.getOption("target-user");
        if (targetUserOption == null) {
            event.reply("PLease write a value in all required options").setEphemeral(true).queue();
            return;
        }
        Member targetMember = targetUserOption.getAsMember();
        if (targetMember == null){
            event.reply("Please give a valid member").setEphemeral(true).queue();
            return;
        }

        JSONArray nicknames = SqlUtil.getNicknamesFromMember(event.getGuild(),targetMember);

        if (nicknames.isEmpty()){
            event.reply("This member has no nicknames").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder nicknamesEmbed = new EmbedBuilder();
        nicknamesEmbed.setTitle("Member-Nicknames");
        nicknamesEmbed.setDescription("Here you have the nicknames from " + targetMember.getEffectiveName() + ".");
        nicknamesEmbed.setTimestamp(OffsetDateTime.now());

        nicknames.forEach(x->{
            JSONObject obj = (JSONObject) x;
            if (nicknamesEmbed.getFields().size() > 24) return;
            nicknamesEmbed.addField(obj.getString("time"),obj.getString("value"),false);
        });

        event.replyEmbeds(nicknamesEmbed.build()).setEphemeral(true).queue();
    }
}
