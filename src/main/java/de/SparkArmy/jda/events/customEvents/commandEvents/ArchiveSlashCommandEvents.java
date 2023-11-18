package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.annotations.interactions.JDASlashCommand;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class ArchiveSlashCommandEvents {


    private final ConfigController controller;

    public ArchiveSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
    }

    @JDASlashCommand(name = "archive")
    public void initialSlashEvent(@NotNull SlashCommandInteractionEvent event) {
        GuildChannel targetChannel = event.getOption("channel", event.getGuildChannel(), OptionMapping::getAsChannel);

        Guild guild = event.getGuild();
        if (guild == null || targetChannel == null) return;

        long categoryId = controller.getGuildArchiveCategory(guild);

        ResourceBundle bundle = Util.getResourceBundle(event.getName(), event.getUserLocale());

        if (categoryId == 0) {
            event.reply(bundle.getString("archiveEvents.initialSlashCommand.categoryIdReturn0")).queue();
        } else if (categoryId < 0) {
            event.reply(
                    String.format(bundle.getString("archiveEvents.initialSlashCommand.categoryIdReturnLower0"),
                            categoryId)).queue();
        } else {
            Category category = guild.getCategoryById(categoryId);
            if (category == null) {
                event.reply(bundle.getString("archiveEvents.initialSlashCommand.categoryIsNull")).queue();
                return;
            }
            moveChannel(event, guild, category, targetChannel);
        }

    }


    private void moveChannel(@NotNull SlashCommandInteractionEvent event, Guild guild, @NotNull Category archiveCategory, @NotNull GuildChannel targetChannel) {
        ResourceBundle bundle = Util.getResourceBundle(event.getName(), event.getUserLocale());
        if (archiveCategory.getChannels().contains(targetChannel)) {
            event.reply(bundle.getString("archiveEvents.error.channelIsInCategory")).setEphemeral(true).queue();
            return;
        }
        switch (targetChannel.getType()) {
            case TEXT -> guild.modifyTextChannelPositions()
                    .selectPosition(targetChannel)
                    .setCategory(archiveCategory, true)
                    .queue(x -> event.reply(bundle.getString("archiveEvents.successful.move")).setEphemeral(true).queue(), new ErrorHandler()
                            .handle(ErrorResponse.MISSING_ACCESS, e -> event.reply(bundle.getString("archiveEvents.error.noAccess")).setEphemeral(true).queue())
                            .handle(ErrorResponse.UNKNOWN_CHANNEL, e -> event.reply(bundle.getString("archiveEvents.error.unknownChannel")).setEphemeral(true).queue()));
            case VOICE -> guild.modifyVoiceChannelPositions()
                    .selectPosition(targetChannel)
                    .setCategory(archiveCategory, true)
                    .queue(x -> event.reply(bundle.getString("archiveEvents.successful.move")).setEphemeral(true).queue(), new ErrorHandler()
                            .handle(ErrorResponse.MISSING_ACCESS, e -> event.reply(bundle.getString("archiveEvents.error.noAccess")).setEphemeral(true).queue())
                            .handle(ErrorResponse.UNKNOWN_CHANNEL, e -> event.reply(bundle.getString("archiveEvents.error.unknownChannel")).setEphemeral(true).queue()));
        }
    }
}
