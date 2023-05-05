package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.controller.GuildConfigType;
import de.SparkArmy.jda.events.annotations.JDASlashCommand;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

public class ArchiveSlashCommandEvents {


    private final ConfigController controller;

    public ArchiveSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        this.controller = dispatcher.getController();
    }

    @JDASlashCommand(name = "archive")
    public void initialSlashEvent(@NotNull SlashCommandInteractionEvent event) {
        GuildChannel targetChannel = event.getOption("channel", OptionMapping::getAsChannel);

        Guild guild = event.getGuild();
        if (guild == null || targetChannel == null) return;

        JSONObject guildConfig = controller.getGuildMainConfig(guild);

        Collection<Permission> deniedPermissions = new ArrayList<>();
        deniedPermissions.add(Permission.VIEW_CHANNEL);

        if (guildConfig.isNull("archive-category")) {
            guild.createCategory("archive").addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions).queue(category -> {
                guildConfig.put("archive-category", category.getId());
                controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, guildConfig);
                moveChannel(event, guild, category, targetChannel);
            });
        } else if (guildConfig.getString("archive-category").isEmpty() || guildConfig.getString("archive-category").isBlank()) {
            guild.createCategory("archive").addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions).queue(category -> {
                guildConfig.put("archive-category", category.getId());
                controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, guildConfig);
                moveChannel(event, guild, category, targetChannel);
            });
        } else {
            Category archiveCategory = guild.getCategoryById(guildConfig.getString("archive-category"));
            if (archiveCategory == null) {
                guild.createCategory("archive").addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions).queue(category -> {
                    guildConfig.put("archive-category", category.getId());
                    controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, guildConfig);
                    moveChannel(event, guild, category, targetChannel);
                });
            } else {
                moveChannel(event, guild, archiveCategory, targetChannel);
            }
        }
    }


    private void moveChannel(@NotNull SlashCommandInteractionEvent event, Guild guild, @NotNull Category archiveCategory, @NotNull GuildChannel targetChannel) {
        ResourceBundle bundle = Util.getResourceBundle(event.getName(), event.getUserLocale());
        if (archiveCategory.getChannels().contains(targetChannel)) {
            event.reply(bundle.getString("command.error.channelIsInCategory")).setEphemeral(true).queue();
            return;
        }
        switch (targetChannel.getType()) {
            case TEXT -> guild.modifyTextChannelPositions()
                    .selectPosition(targetChannel)
                    .setCategory(archiveCategory, true)
                    .queue(x -> event.reply(bundle.getString("command.successful.move")).setEphemeral(true).queue(), new ErrorHandler()
                            .handle(ErrorResponse.MISSING_ACCESS, e -> event.reply(bundle.getString("command.error.noAccess")).setEphemeral(true).queue())
                            .handle(ErrorResponse.UNKNOWN_CHANNEL, e -> event.reply(bundle.getString("command.error.unknownChannel")).setEphemeral(true).queue()));
            case VOICE -> guild.modifyVoiceChannelPositions()
                    .selectPosition(targetChannel)
                    .setCategory(archiveCategory, true)
                    .queue(x -> event.reply(bundle.getString("command.successful.move")).setEphemeral(true).queue(), new ErrorHandler()
                            .handle(ErrorResponse.MISSING_ACCESS, e -> event.reply(bundle.getString("command.error.noAccess")).setEphemeral(true).queue())
                            .handle(ErrorResponse.UNKNOWN_CHANNEL, e -> event.reply(bundle.getString("command.error.unknownChannel")).setEphemeral(true).queue()));
        }
    }
}
