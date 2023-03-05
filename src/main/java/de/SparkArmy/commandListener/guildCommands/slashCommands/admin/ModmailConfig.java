package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.SlashCommand;
import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.utils.jda.ChannelUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.json.JSONObject;

public class ModmailConfig extends SlashCommand {

    @Override
    public void dispatch(SlashCommandInteractionEvent event, JDA jda, ConfigController controller) {
        if (event.getGuild() == null) {
            event.reply("Please use this command in a guild-channel").setEphemeral(true).queue();
            return;
        }

        OptionMapping type = event.getOption("target-type");
        OptionMapping channel = event.getOption("target-channel");

        if (type == null || channel == null) {
            event.reply("Please write in all options a value").setEphemeral(true).queue();
            return;
        }

        String typeString = type.getAsString();
        if (!typeString.equals("log") && !typeString.equals("archive") && !typeString.equals("category")) {
            event.reply("Please use a valid option for \"target-type\"").setEphemeral(true).queue();
            return;
        }

        JSONObject config = controller.getGuildMainConfig(event.getGuild());
        JSONObject modmailConfig = config.optJSONObject("modmail", new JSONObject());

        if (!typeString.equals("category")) {
            MessageChannel targetChannel = ChannelUtil.rightChannel(channel.getAsChannel());
            if (targetChannel == null || !targetChannel.getType().equals(ChannelType.TEXT)) {
                event.reply("Please select a valid text-channel").setEphemeral(true).queue();
                return;
            }


            if (typeString.equals("archive")) {
                modmailConfig.put("archive-channel",targetChannel.getId());
            }else {
                modmailConfig.put("log-channel", targetChannel.getId());
            }
        } else {
            Category targetCategory = channel.getAsChannel().asCategory();
            if (!targetCategory.getType().equals(ChannelType.CATEGORY)) {
                event.reply("Please select a valid text-channel").setEphemeral(true).queue();
                return;
            }
            modmailConfig.put("category", targetCategory.getId());
        }
        config.put("modmail", modmailConfig);
        controller.writeInSpecificGuildConfig(event.getGuild(), GuildConfigType.MAIN, config);

        event.reply("The " + typeString + " was changed to " + channel.getAsChannel().getAsMention()).setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "modmail-config";
    }
}
