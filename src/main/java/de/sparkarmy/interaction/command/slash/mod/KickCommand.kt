package de.sparkarmy.interaction.command.slash.mod

import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.dsl.option
import de.sparkarmy.interaction.misc.checkPreconditions
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

@Single
class KickCommand : SlashCommand("kick", "Kicks a user from the server") {
    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        feature = GuildFeature.PUNISHMENT
        option<User>("user", "The user to kick")
        option<String>("reason", "The Reason for the kick", builder = {
            setMinLength(10)
        })
    }

    // TODO add Embeds for log and user

    @Handler
    suspend fun run(event: SlashCommandInteractionEvent, user: User, reason: String) {
        val guild = event.guild
        if (guild == null) {
            event.reply(MessageCreate("You can only use this command on server")).setEphemeral(true).queue()
            return
        }
        checkPreconditions(user, event.member, guild)
            .flatMap { b ->
                if (b)
                    guild.kick(user)
                        .reason(reason)
                        .flatMap { void ->
                            event.reply(
                                event.getLocalizedString(
                                    "commands.punishment.kick.userKickSuccessfully",
                                    true,
                                    user
                                )
                            ).setEphemeral(true)
                        }
                else
                    event.reply(event.getLocalizedString("commands.punishment.kick.userKickFailed", true, user))
                        .setEphemeral(true)
            }
            .queue(
                null,
                ErrorHandler().handle(listOf(ErrorResponse.MISSING_PERMISSIONS, ErrorResponse.UNKNOWN_USER)) { t ->
                    event.reply(event.getLocalizedString("commands.punishment.Kick.userKickFailed", true, user))
                        .setEphemeral(true)
                })
    }
}