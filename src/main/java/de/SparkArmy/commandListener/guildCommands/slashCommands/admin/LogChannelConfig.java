package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.LogChannelType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.json.JSONObject;

public class LogChannelConfig extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        if (event.getGuild() == null) {
            event.reply("Please use this message in a guild-channel").setEphemeral(true).queue();
            return;
        }

        OptionMapping channelTypeOption = event.getOption("target-typ");
        OptionMapping channel = event.getOption("target-channnel");

        if (channelTypeOption == null) {
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


        JSONObject config = controller.getGuildMainConfig(event.getGuild());
        JSONObject logChannel;
        if (config.isNull("log-channel")) {
            logChannel = new JSONObject();
        } else {
            logChannel = config.getJSONObject("log-channel");
        }

        logChannel.put(type.getName(), targetChannel.getId());
        config.put("log-channel", logChannel);
        controller.writeInSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN, config);

        event.reply("The channel " + targetChannel.getAsMention() + " is the log-channel for " + type.name()).setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "log-channel-config";
    }
}
