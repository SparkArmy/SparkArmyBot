package de.SparkArmy.jdaEvents.customCommands.commands;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.jdaEvents.customCommands.CustomCommand;
import de.SparkArmy.util.customTypes.GuildConfigType;
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

public class ArchiveSlashCommand extends CustomCommand {
    @Override
    public String getName() {
        return "archive";
    }

    @Override
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event, ConfigController controller) {
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
                moveChannel(event, guild, category, targetChannel);
                controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, guildConfig);
            });
        } else if (guildConfig.getString("archive-category").isEmpty() || guildConfig.getString("archive-category").isBlank()) {
            guild.createCategory("archive").addRolePermissionOverride(guild.getPublicRole().getIdLong(), null, deniedPermissions).queue(category -> {
                guildConfig.put("archive-category", category.getId());
                moveChannel(event, guild, category, targetChannel);
                controller.writeInSpecificGuildConfig(guild, GuildConfigType.MAIN, guildConfig);
            });
        } else {
            Category archiveCategory = guild.getCategoryById(guildConfig.getString("archive-category"));
            moveChannel(event, guild, archiveCategory, targetChannel);
        }
    }

    private void moveChannel(SlashCommandInteractionEvent event, Guild guild, Category archiveCategory, @NotNull GuildChannel targetChannel) {
        switch (targetChannel.getType()) {
            case TEXT -> guild.modifyTextChannelPositions()
                    .selectPosition(targetChannel)
                    .setCategory(archiveCategory, true)
                    .and(event.reply("Channel was moved").setEphemeral(true))
                    .queue(null, new ErrorHandler()
                            .handle(ErrorResponse.MISSING_ACCESS, e -> event.reply("Bot has no access to this channel").setEphemeral(true).queue())
                            .handle(ErrorResponse.UNKNOWN_CHANNEL, e -> event.reply("Unknown Channel, try it again").setEphemeral(true).queue()));
            case VOICE -> guild.modifyVoiceChannelPositions()
                    .selectPosition(targetChannel)
                    .setCategory(archiveCategory, true)
                    .and(event.reply("Channel was moved").setEphemeral(true))
                    .queue(null, new ErrorHandler()
                            .handle(ErrorResponse.MISSING_ACCESS, e -> event.reply("Bot has no access to this channel").setEphemeral(true).queue())
                            .handle(ErrorResponse.UNKNOWN_CHANNEL, e -> event.reply("Unknown Channel, try it again").setEphemeral(true).queue()));
        }
    }
}
