package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class LogChannelConfig extends CustomCommandListener {

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("log-channel-config")) return;
        Collection<String> types = new ArrayList<>();
        LogChannelType.getLogChannelTypes().forEach(x ->{
            if (x.equals(LogChannelType.UNKNOW)) return;
            types.add(x.getName());
        });
        event.replyChoiceStrings(types).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("log-channel-config")) return;
        if (event.getGuild() == null){
            event.reply("Please use this message in a guild-channel").setEphemeral(true).queue();
            return;
        }

        OptionMapping channelTypeOption = event.getOption("target-typ");
        OptionMapping channel = event.getOption("target-channnel");

        if (channelTypeOption == null){
            event.reply("Please write a value in the option target-typ").setEphemeral(true).queue();
            return;
        }

        if (channel == null){
            event.reply("Please write a value in the option target-channel").setEphemeral(true).queue();
            return;
        }

        LogChannelType type = LogChannelType.getLogChannelTypeByName(channelTypeOption.getAsString());
        if (type.equals(LogChannelType.UNKNOW)){
            event.reply("Please write a valid value in the option target-type").setEphemeral(true).queue();
            return;
        }

        MessageChannel targetChannel = ChannelUtil.rightChannel(channel.getAsChannel());
        if (targetChannel == null){
            event.reply("Please write a valid value in the option target-channel").setEphemeral(true).queue();
            return;
        }


        JSONObject config = getGuildMainConfig(event.getGuild());
        JSONObject logChannel;
        if (config.isNull("log-channel")){
            logChannel = new JSONObject();
        }else {
            logChannel = config.getJSONObject("log-channel");
        }

        logChannel.put(type.getName(),targetChannel.getId());
        config.put("log-channel",logChannel);
        writeInGuildMainConfig(event.getGuild(),config);

        event.reply("The channel " + targetChannel.getAsMention() + " is the log-channel for " + type.name()).setEphemeral(true).queue();
    }
}
