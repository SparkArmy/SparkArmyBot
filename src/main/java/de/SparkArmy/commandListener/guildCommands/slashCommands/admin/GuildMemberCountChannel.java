package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.jda.ChannelUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.json.JSONObject;

public class GuildMemberCountChannel extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        if (event.getGuild() == null) {
            event.reply("Please use this command in a guild-channel").setEphemeral(true).queue();
            return;
        }
        if (event.getSubcommandName() == null) {
            event.reply("Please select a subcommand").setEphemeral(true).queue();
            return;
        }

        if (event.getSubcommandName().equals("channel")) {
            OptionMapping channelOption = event.getOption("target-channel");
            if (channelOption == null) {
                event.reply("Please give a valid option-value").setEphemeral(true).queue();
                return;
            }
            MessageChannel channel = ChannelUtil.rightChannel(channelOption.getAsChannel());
            if (channel == null) {
                event.reply("Please give a valid message-channel").setEphemeral(true).queue();
                return;
            }

            JSONObject config = controller.getSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN);
            JSONObject channelConfig = new JSONObject() {{
                put("string", "");
                put("count-channel", channel.getId());
            }};
            if (!config.isNull("user-count")) {
                channelConfig.put("string", config.getJSONObject("user-count").getString("string"));
            }
            config.put("user-count", channelConfig);
            controller.writeInSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN, config);

            event.reply("Count channel is " + channel.getAsMention() + " now.").setEphemeral(true).queue();
        }else if (event.getSubcommandName().equals("name")) {
            OptionMapping nameOption = event.getOption("channel-name");
            if (nameOption == null) {
                event.reply("Please give a valid option-value").setEphemeral(true).queue();
                return;
            }
            String s = nameOption.getAsString();
            if (s.length() > 90) {
                event.reply("The char limit is 90!").setEphemeral(true).queue();
                return;
            }

            JSONObject config = controller.getSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN);
            JSONObject channelConfig = new JSONObject() {{
                put("string", s);
                put("count-channel", "");
            }};
            if (!config.isNull("user-count")) {
                channelConfig.put("count-channel", config.getJSONObject("user-count").getString("count-channel"));
            }
            config.put("user-count", channelConfig);
            controller.writeInSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN, config);

            event.reply("String was set").setEphemeral(true).queue();
        }
        else {
            JSONObject config = controller.getSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN);
            if (config.isNull("user-count")) {
                event.reply("You can't clear an empty object").setEphemeral(true).queue();
                return;
            }
            config.remove("user-count");
            controller.writeInSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN, config);

            event.reply("User count cleared successfully").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "count-channel-settings";
    }
}
