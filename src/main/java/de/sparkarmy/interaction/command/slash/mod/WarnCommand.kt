package de.sparkarmy.interaction.command.slash.mod

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.UserCacheView
import de.sparkarmy.data.cache.WebhookCacheView
import de.sparkarmy.embed.EmbedService
import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.localizationService
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.dsl.option
import de.sparkarmy.jda.listeners.PunishmentContextData
import de.sparkarmy.jda.listeners.checkPreconditions
import de.sparkarmy.jda.listeners.modActionHandler
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.model.ModerationActionType
import de.sparkarmy.util.getLocalizedString
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.requests.ErrorResponse
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

@Single
class WarnCommand(
    private val guildCacheView: GuildCacheView,
    private val embedService: EmbedService,
    private val webhookCacheView: WebhookCacheView,
    private val userCacheView: UserCacheView
) : SlashCommand("warn", "Warns a user on the server") {

    init {
        commandData.defaultPermissions =
            DefaultMemberPermissions.enabledFor(net.dv8tion.jda.api.Permission.MODERATE_MEMBERS)
        feature = GuildFeature.PUNISHMENT
        contexts(InteractionContextType.GUILD)

        option<Member>("user", "The member to warn")
        option<String>("reason", "The reason for the warn") {
            setMinLength(10)
        }
    }


    @Handler(ephemeral = true)
    suspend fun run(event: SlashCommandInteractionEvent, user: Member, reason: String) {
        val guild = event.guild!!
        event.deferReply().setEphemeral(true).await()
        val hook = event.hook


        val cachedGuild = guildCacheView.save(guild)

        val warnRole = newSuspendedTransaction {
            val warnRoleLong = cachedGuild.guildPunishmentConfig?.warnRole
            when {
                warnRoleLong != null -> guild.getRoleById(warnRoleLong)
                else -> null
            }
        }

        if (warnRole == null) {
            hook.editOriginal(
                event.getLocalizedString(
                    "commands.punishment.warn.noWarnRoleSet"
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
                        listOf(warnRole),
                        listOf()
                    )
                        .reason(reason)
                        .flatMap {
                            hook.editOriginal(
                                event.getLocalizedString(
                                    "commands.punishment.warn.userWarnSuccessfully",
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
                                    "commands.punishment.warn.userWarnFailed",
                                    false,
                                    user.effectiveName
                                )
                            )
                        }

                    else -> hook.editOriginal(
                        event.getLocalizedString(
                            "commands.punishment.warn.userWarnFailed",
                            false,
                            user.effectiveName
                        )
                    )
                }
            }
            .await()

        val data = PunishmentContextData(
            event.jda,
            embedService,
            localizationService,
            webhookCacheView,
            userCacheView,
            guildCacheView,
            event.guildLocale,
            user.idLong,
            event.user.idLong,
            reason,
            guild,
            ModerationActionType.MUTE
        )

        modActionHandler(data)
    }
}