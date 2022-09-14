package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.ChannelUtil;
import de.SparkArmy.utils.jda.FileHandler;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;

public class Lockdown extends CustomCommandListener {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String eventName = event.getName();
        if (!eventName.equals("lockdown")) return;

        if (event.getGuild() == null) {
            event.reply("Please us this command in a guild-channel").setEphemeral(true).queue();
            return;
        }

        OptionMapping channel = event.getOption("target_channel");

        MessageChannel target_channel;
        if (channel == null) {
            target_channel = event.getChannel();
        } else {
            target_channel = ChannelUtil.rightChannel(channel.getAsChannel());
            if (target_channel == null) {
                event.reply("Please give a valid message-channel").setEphemeral(true).queue();
                return;
            }
        }

        if (target_channel.getType().isThread()) {
            ThreadChannel treadChannel = event.getGuild().getThreadChannelById(target_channel.getId());
            if (treadChannel == null) {
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            boolean lockedState = treadChannel.isLocked();
            treadChannel.getManager().setLocked(!lockedState);
            event.reply("The tread has the locked-state: " + !lockedState + " now!").setEphemeral(true).queue();
            return;
        }

        File directory = FileHandler.getDirectoryInUserDirectory("botstuff/lockdownChannel/" + event.getGuild().getId());
        if (directory == null) {
            event.reply("Ups something went wrong").setEphemeral(true).queue();
            return;
        }

        String fileName = target_channel.getId() + ".json";
        File file = FileHandler.getFileInDirectory(directory, fileName);

        if (file.exists()) {
            String contentString = FileHandler.getFileContent(file);
            if (contentString == null) {
                event.reply("Ups something went wrong").setEphemeral(true).queue();
                return;
            }
            ChannelUtil.clearChannelPermissionsForPublicRole(target_channel);
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            event.reply("The lockdown for the channel was removed").setEphemeral(true).queue();
        } else {
            JSONObject permissions = new JSONObject(ChannelUtil.getChannelPermission(target_channel));
            if (permissions.isEmpty()) {
                event.reply("Please give a valid Channel").setEphemeral(true).queue();
                return;
            }

            FileHandler.createFile(directory,fileName);
            FileHandler.writeValuesInFile(file,permissions);

            ChannelUtil.disableWritingForPublicRole(target_channel);
            event.reply("The Channel is now in lockdown").setEphemeral(true).queue();
        }
    }
}
