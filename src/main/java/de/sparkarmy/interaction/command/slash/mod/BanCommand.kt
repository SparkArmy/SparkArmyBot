package de.sparkarmy.interaction.command.slash.mod

import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.dsl.option
import de.sparkarmy.jda.listeners.checkPreconditions
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.util.getLocalizedString
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.requests.ErrorResponse
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit

@Single
class BanCommand : SlashCommand("ban", "Ban a user from the server") {
    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        feature = GuildFeature.PUNISHMENT
        option<User>("user", "The user to ban")
        option<String>("reason", "The Reason for the kick", builder = {
            setMinLength(10)
        })
        option<Boolean?>("messages", "Remove messages from the last day")
    }

    // TODO add Embeds for log and user

    @Handler(ephemeral = true)
    suspend fun run(event: SlashCommandInteractionEvent, user: User, reason: String, messages: Boolean = false) {
        val guild = event.guild
        if (guild == null) {
            event.reply(MessageCreate("You can only use this command on server")).setEphemeral(true).queue()
            return
        }

        val duration = when {
            messages -> 1
            else -> 0
        }


        checkPreconditions(user, event.member, guild)
            .flatMap { b ->
                if (b)
                    guild.ban(user, duration, TimeUnit.DAYS)
                        .reason(reason)
                        .flatMap { void ->
                            event.reply(
                                event.getLocalizedString(
                                    "commands.punishment.ban.userBanSuccessfully",
                                    true,
                                    user
                                )
                            ).setEphemeral(true)
                        }
                else
                    event.reply(event.getLocalizedString("commands.punishment.ban.userBanFailed", true, user))
                        .setEphemeral(true)
            }
            .queue(
                null,
                ErrorHandler().handle(listOf(ErrorResponse.MISSING_PERMISSIONS, ErrorResponse.UNKNOWN_USER)) { t ->
                    event.reply(event.getLocalizedString("commands.punishment.ban.userBanFailed", true, user))
                        .setEphemeral(true)
                })
    }
}