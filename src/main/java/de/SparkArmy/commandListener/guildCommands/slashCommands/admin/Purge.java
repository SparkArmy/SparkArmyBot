package de.SparkArmy.commandListener.guildCommands.slashCommands.admin;

import de.SparkArmy.commandListener.CustomCommandListener;
import de.SparkArmy.utils.jda.ChannelUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Purge extends CustomCommandListener {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("purge")) return;

        Guild guild = event.getGuild();
        if (guild == null) return;
        String subcommandName = event.getSubcommandName();
        if (subcommandName == null) return;
        if (subcommandName.equals("periodically")) periodically(event);
        else if (subcommandName.equals("live")) {
            live(event);
        }

    }

    private void periodically(SlashCommandInteractionEvent event) {

    }

    private void live(@NotNull SlashCommandInteractionEvent event) {
        if (event.getOptions().isEmpty()) {
            event.reply("Please select on option").setEphemeral(true).queue();
            return;
        }

        // Get Option Mappings
        OptionMapping channelMapping = event.getOption("target-channel");
        OptionMapping userMapping = event.getOption("target-user");
        OptionMapping stringMapping = event.getOption("target-string");
        OptionMapping messageCountMapping = event.getOption("messages");

        // Set target channel and check if the channel valid
        MessageChannel targetChannel;
        if (channelMapping == null) targetChannel = event.getMessageChannel();
        else targetChannel = ChannelUtil.rightChannel(channelMapping.getAsChannel());

        if (targetChannel == null) {
            event.reply("Please check the channel-value").queue();
            return;
        }

        // Get target user, if one exist
        var filter = new Object() {
            User targetUser = null;
            String targetString = null;
        };
        if (userMapping != null) filter.targetUser = userMapping.getAsUser();

        // Get target string, if one exist

        if (stringMapping != null) filter.targetString = stringMapping.getAsString();

        // Set message count -> standard 100
        int messageCount = 100;
        if (messageCountMapping != null && messageCountMapping.getAsInt() <= 100) messageCount = messageCountMapping.getAsInt();


        final List<Message> messages = targetChannel.getHistory().retrievePast(messageCount).complete().stream().filter(x->{
            if (filter.targetUser != null && filter.targetString == null && x.getAuthor().equals(filter.targetUser)) return true;
            else if (filter.targetUser == null && filter.targetString != null && x.getContentRaw().contains(filter.targetString)) return true;
            else if (filter.targetUser == null && filter.targetString == null) return true;
            else return filter.targetUser != null && filter.targetString != null && x.getAuthor().equals(filter.targetUser) && x.getContentRaw().contains(filter.targetString);
        }).filter(x-> !x.isEphemeral() || !x.isWebhookMessage()).toList();

        if (messages.isEmpty()){
            event.reply("No message was deleted!").setEphemeral(true).queue();
            return;
        }

        event.reply("Message purging start").setEphemeral(true).queue();
        new Thread(()->{
        for (Message m : messages){
            m.delete().reason("Purge, started from " + event.getUser().getAsTag()).queue(null,new ErrorHandler().ignore(ErrorResponse.INVALID_MESSAGE_TARGET).ignore(ErrorResponse.UNKNOWN_MESSAGE));
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                event.getHook().editOriginal("Something went wrong").queue();
            }
        }


        event.getHook().editOriginal("Messages are successfully purged").queue();
        }).start();
    }
}
