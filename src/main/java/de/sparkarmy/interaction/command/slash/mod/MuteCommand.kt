package de.sparkarmy.interaction.command.slash.mod

import at.xirado.jdui.view.sendView
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.WebhookCacheView
import de.sparkarmy.database.entity.GuildLogChannel
import de.sparkarmy.embed.EmbedService
import de.sparkarmy.interaction.command.model.localizationService
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.dsl.option
import de.sparkarmy.interaction.misc.checkPreconditions
import de.sparkarmy.interaction.misc.createGuildCaseEmbed
import de.sparkarmy.interaction.misc.createUserCaseEmbed
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.model.LogChannelType
import de.sparkarmy.model.PunishmentType
import de.sparkarmy.model.toMessageEmbed
import de.sparkarmy.util.getLocalizedString
import dev.minn.jda.ktx.coroutines.await
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.requests.ErrorResponse
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

@Single
class MuteCommand(
    private val guildCache: GuildCacheView,
    private val webhookCacheView: WebhookCacheView,
    private val embedService: EmbedService
) : SlashCommand("mute", "Mutes a user on the server") {

    val log = KotlinLogging.logger { "MuteCommand" }

    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
        feature = GuildFeature.PUNISHMENT
        option<Member>("user", "The member to mute")
        option<String>("reason", "The Reason for the mute", builder = {
            setMinLength(10)
        })
    }

    // TODO add Embeds for log and user

    @Handler(ephemeral = true)
    suspend fun run(event: SlashCommandInteractionEvent, user: Member, reason: String) {
        val guild = event.guild
        if (guild == null) return
        event.deferReply().setEphemeral(true).await()
        val hook = event.hook


        val cachedGuild = guildCache.getById(guild.idLong)

        val muteRole = newSuspendedTransaction {
            val muteRoleLong = cachedGuild?.guildPunishmentConfig?.muteRole
            when {
                muteRoleLong != null -> guild.getRoleById(muteRoleLong)
                else -> null
            }
        }

        if (muteRole == null) {
            hook.editOriginal(
                event.getLocalizedString(
                    "commands.punishment.mute.noMuteRoleSet"
                )
            )
                .await()
            return
        }

        checkPreconditions(user.user, event.member, guild)
            .flatMap {
                when {
                    it -> guild.modifyMemberRoles(
                        user,
                        listOf(muteRole),
                        listOf()
                    )
                        .reason(reason)
                        .flatMap {
                            hook.editOriginal(
                                event.getLocalizedString(
                                    "commands.punishment.mute.userMuteSuccessfully",
                                    false,
                                    user.effectiveName
                                )
                            )
                        }
                        .onErrorFlatMap(
                            ErrorResponse.test(
                                ErrorResponse.UNKNOWN_MEMBER, ErrorResponse.MISSING_PERMISSIONS
                            )
                        ) {
                            hook.editOriginal(
                                event.getLocalizedString(
                                    "commands.punishment.mute.userMuteFailed",
                                    false,
                                    user.effectiveName
                                )
                            )
                        }

                    else -> hook.editOriginal(
                        event.getLocalizedString(
                            "",
                            false,
                            user.effectiveName
                        )
                    )
                }
            }
            .await()


        val guildChannel = GuildLogChannel.getLogChannels(LogChannelType.MOD_LOG, guild.idLong)
        val embed =
            createGuildCaseEmbed(
                embedService,
                event.guildLocale,
                user,
                reason,
                PunishmentType.MUTE,
                event
            ).toMessageEmbed()

        guildChannel.forEach {
            try {
                webhookCacheView.getById(it.id.value)?.sendMessageEmbeds(embed)?.await()
            } catch (e: ErrorResponseException) {
                log.warn { "Webhook from ${it.id} removed, can't send webhook message" }

                webhookCacheView.remove(it.id.value)
            }
        }

        user.user.openPrivateChannel().await()
            .sendView(createUserCaseEmbed(reason, PunishmentType.MUTE, guild, event.guildLocale, localizationService))
            .onErrorFlatMap(ErrorResponse.test(ErrorResponse.UNKNOWN_CHANNEL, ErrorResponse.CANNOT_SEND_TO_USER)) {
                hook.editOriginal("Nop")
            }
            .await()
    }
}