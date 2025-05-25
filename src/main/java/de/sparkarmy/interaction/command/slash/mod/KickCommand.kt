package de.sparkarmy.interaction.command.slash.mod

import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.dsl.option
import de.sparkarmy.jda.listeners.checkPreconditions
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.util.getLocalizedString
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.requests.ErrorResponse
import org.koin.core.annotation.Single

@Single
class KickCommand : SlashCommand("kick", "Kicks a user from the server") {
    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        feature = GuildFeature.PUNISHMENT
        contexts(InteractionContextType.GUILD)

        option<User>("user", "The user to kick")
        option<String>("reason", "The Reason for the kick", builder = {
            setMinLength(10)
        })
    }

    @Handler
    suspend fun run(event: SlashCommandInteractionEvent, user: User, reason: String) {
        val guild = event.guild!!

        event.deferReply().setEphemeral(true).await()
        val hook = event.hook

        checkPreconditions(user, event.member, guild)
            .flatMap {
                when {
                    it -> guild.kick(user)
                        .reason(reason)
                        .flatMap {
                            hook.editOriginal(
                                event.getLocalizedString(
                                    "commands.punishment.kick.userKickSuccessfully",
                                    true,
                                    user
                                )
                            )
                        }
                        .onErrorFlatMap(
                            ErrorResponse.test(
                                ErrorResponse.UNKNOWN_USER, ErrorResponse.MISSING_PERMISSIONS
                            )
                        ) {
                            hook.editOriginal(
                                event.getLocalizedString(
                                    "commands.punishment.kick.userKickFailed",
                                    false,
                                    user.effectiveName
                                )
                            )
                        }

                    else -> event.reply(event.getLocalizedString("commands.punishment.kick.userKickFailed", true, user))
                }
            }
            .await()
    }
}