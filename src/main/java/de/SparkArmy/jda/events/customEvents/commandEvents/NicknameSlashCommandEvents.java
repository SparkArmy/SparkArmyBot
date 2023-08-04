package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.jda.events.annotations.interactions.JDASlashCommand;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

public class NicknameSlashCommandEvents {

    public NicknameSlashCommandEvents(EventDispatcher ignoredDispatcher) {

    }

    @JDASlashCommand(name = "nickname")
    public void dispatchSlashEvent(@NotNull SlashCommandInteractionEvent event) {
        String subcommandName = event.getSubcommandName();
        ResourceBundle bundle = Util.getResourceBundle("nickname", event.getUserLocale());

        if (subcommandName == null) {
            event.reply(bundle.getString("nicknameCommand.dispatchSlashEvent.subcommandIsNull")).setEphemeral(true).queue();
            return;
        }
        switch (subcommandName) {
            case "change" -> changeNickname(event);
            case "remove" -> removeNickname(event);
            default ->
                    event.reply(bundle.getString("nicknameCommand.dispatchSlashEvent.switchSubcommandName.default")).setEphemeral(true).queue();
        }
    }

    private void removeNickname(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = Util.getResourceBundle("nickname", event.getUserLocale());
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply(bundle.getString("nicknameCommand.guildIsNull")).setEphemeral(true).queue();
            return;
        }
        User targetUser = event.getOption("member", OptionMapping::getAsUser);
        if (targetUser == null) {
            event.reply(bundle.getString(bundle.getString("nicknameCommand.userIsNull"))).setEphemeral(true).queue();
            return;
        }
        event.deferReply(true).queue();
        InteractionHook hook = event.getHook();
        guild.retrieveMember(targetUser).queue(member ->
                        member.modifyNickname(null).queue(y -> hook.editOriginal(bundle.getString("nicknameCommand.removeNickname.successfullyExecuted")).queue(),
                                new ErrorHandler()
                                        .handle(ErrorResponse.UNKNOWN_MEMBER, e -> hook.editOriginal(bundle.getString("nicknameCommand.memberNotExist")).queue())
                                        .handle(ErrorResponse.MISSING_PERMISSIONS, e -> hook.editOriginal(bundle.getString("nicknameCommand.removeNickname.missingPermissions")).queue())
                        ),
                new ErrorHandler()
                        .handle(ErrorResponse.UNKNOWN_USER, e -> hook.editOriginal(bundle.getString("nicknameCommand.userIsNull")).queue())
                        .handle(ErrorResponse.UNKNOWN_MEMBER, e -> hook.editOriginal(bundle.getString("nicknameCommand.memberNotExist")).queue()));
    }

    private void changeNickname(@NotNull SlashCommandInteractionEvent event) {
        ResourceBundle bundle = Util.getResourceBundle("nickname", event.getUserLocale());
        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply(bundle.getString("nicknameCommand.guildIsNull")).setEphemeral(true).queue();
            return;
        }
        User targetUser = event.getOption("member", OptionMapping::getAsUser);
        if (targetUser == null) {
            event.reply(bundle.getString(bundle.getString("nicknameCommand.userIsNull"))).setEphemeral(true).queue();
            return;
        }

        String newNickname = event.getOption("nickname", OptionMapping::getAsString);
        if (newNickname == null) {
            event.reply(bundle.getString("nicknameCommand.changeNickname.newNicknameIsNull")).setEphemeral(true).queue();
            return;
        } else if (newNickname.isEmpty()) {
            event.reply(bundle.getString("nicknameCommand.changeNickname.newNicknameIsNull")).setEphemeral(true).queue();
            return;
        } else if (newNickname.length() > 32) {
            event.reply(bundle.getString("nicknameCommand.changeNickname.newNicknameIsToLong")).setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();
        InteractionHook hook = event.getHook();
        guild.retrieveMember(targetUser).queue(member ->
                        member.modifyNickname(newNickname).queue(y -> hook.editOriginal(bundle.getString("nicknameCommand.changeNickname.successfullyExecuted")).queue(),
                                new ErrorHandler()
                                        .handle(ErrorResponse.UNKNOWN_MEMBER, e -> hook.editOriginal(bundle.getString("nicknameCommand.memberNotExist")).queue())
                                        .handle(ErrorResponse.MISSING_PERMISSIONS, e -> hook.editOriginal(bundle.getString("nicknameCommand.removeNickname.missingPermissions")).queue())
                        ),
                new ErrorHandler()
                        .handle(ErrorResponse.UNKNOWN_USER, e -> hook.editOriginal(bundle.getString("nicknameCommand.userIsNull")).queue())
                        .handle(ErrorResponse.UNKNOWN_MEMBER, e -> hook.editOriginal(bundle.getString("nicknameCommand.memberNotExist")).queue()));
    }
}
