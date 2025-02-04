package de.sparkarmy.interaction.command.slash.mod

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.embed.EmbedService
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.dsl.option
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.util.getLocalizedString
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

@Single
class MuteCommand(
    private val guildCache: GuildCacheView,
    private val embedService: EmbedService
) : SlashCommand("mute", "Mutes a user on the server") {

    val log = KotlinLogging.logger { "MuteCommand" }

    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        feature = GuildFeature.PUNISHMENT
        option<User>("user", "The user to mute")
        option<String>("reason", "The Reason for the mute", builder = {
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
        val cachedGuild = guildCache.getById(guild.idLong)
        newSuspendedTransaction {
            val muteRoleId = cachedGuild?.guildPunishmentConfig?.muteRole

            when {
                muteRoleId == null -> event.reply(
                    event.getLocalizedString(
                        "commands.punishment.mute.noMuteRoleSet",
                        true
                    )
                ).setEphemeral(true).await()

                else -> {
                    val muteRole = guild.getRoleById(muteRoleId)

                    if (muteRole == null) {
                        event.reply(event.getLocalizedString("commands.punishment.mute.noMuteRoleSet", true))
                            .setEphemeral(true).await()
                        return@newSuspendedTransaction
                    }


                    guild.retrieveMember(user)
                        .flatMap { member ->
                            if (event.member?.canInteract(member)!! && !member.isOwner) {
                                guild.addRoleToMember(user, muteRole)
                                    .reason(reason)
                                    .flatMap { t ->
                                        event.reply(
                                            event.getLocalizedString(
                                                "commands.punishment.mute.userMuteSuccessfully",
                                                true,
                                                user.asMention
                                            )
                                        ).setEphemeral(true)
                                    }
                            } else {
                                event.reply(
                                    event.getLocalizedString(
                                        "commands.punishment.mute.userMuteFailed",
                                        true,
                                        user.asMention
                                    )
                                ).setEphemeral(true)
                            }
                        }
                        .await()
                }
            }
        }
    }
}