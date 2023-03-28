package de.SparkArmy.jda.events.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jda.events.customCommands.CustomCommand;
import de.SparkArmy.utils.Util;
import de.SparkArmy.controller.GuildConfigType;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

public class ArchiveSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "archive";
    }

    private ResourceBundle bundle;

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, @NotNull ConfigController controller) {

        bundle = Util.getResourceBundle(event.getName(), event.getUserLocale());

        @SuppressWarnings("ConstantConditions") // Channel is a required option
        GuildChannel targetChannel = event.getOption("channel").getAsChannel();
        Guild guild = event.getGuild();
        if (guild == null) return;

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


    private void moveChannel(SlashCommandInteractionEvent event, Guild guild, @NotNull Category archiveCategory, @NotNull GuildChannel targetChannel) {
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
