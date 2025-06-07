package de.sparkarmy.jda.listeners

import at.xirado.jdui.component.message.button
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.separator
import at.xirado.jdui.component.message.text
import at.xirado.jdui.component.row
import at.xirado.jdui.view.definition.function.view
import at.xirado.jdui.view.sendView
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.UserCacheView
import de.sparkarmy.data.cache.WebhookCacheView
import de.sparkarmy.database.entity.GuildLogChannel
import de.sparkarmy.database.entity.ModerationAction
import de.sparkarmy.embed.EmbedService
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.jda.JDAEventListener
import de.sparkarmy.model.*
import de.sparkarmy.util.userMention
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.core.annotation.Single
import java.time.OffsetDateTime
import java.util.*
import net.dv8tion.jda.api.entities.Guild as JDAGuild
import net.dv8tion.jda.api.entities.User as JDAUser

@Single
class UserModerationActionListener(
    private val webhookCacheView: WebhookCacheView,
    private val embedService: EmbedService,
    private val guildCacheView: GuildCacheView,
    private val userCacheView: UserCacheView,
    private val localizationService: LocalizationService
) : JDAEventListener {
    override val intents: EnumSet<GatewayIntent> =
        EnumSet.of(GatewayIntent.GUILD_MODERATION, GatewayIntent.GUILD_MEMBERS)

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is GuildAuditLogEntryCreateEvent -> auditLogEvent(event)
            is GuildMemberUpdateTimeOutEvent -> memberUpdateTimeout(event)
        }
    }

    private suspend fun memberUpdateTimeout(event: GuildMemberUpdateTimeOutEvent) {
        val guild = event.guild
        val offender = event.entity
        val moderator = event.member
        val locale = guild.locale
        val reason = "No Reason provided"

        val data = PunishmentContextData(
            event.jda,
            embedService,
            localizationService,
            webhookCacheView,
            userCacheView,
            guildCacheView,
            locale,
            offender.idLong,
            moderator.idLong,
            reason,
            guild,
            ModerationActionType.TIMEOUT
        )

        modActionHandler(data)
    }

    private suspend fun auditLogEvent(event: GuildAuditLogEntryCreateEvent) {
        val actionType = event.entry.type

        when (actionType) {
            ActionType.BAN, ActionType.KICK, ActionType.UNBAN -> memberLeaveWithModerationAction(event)
            else -> {}
        }

    }

    private suspend fun memberLeaveWithModerationAction(event: GuildAuditLogEntryCreateEvent) {
        val guild = event.guild
        val offenderId = event.entry.targetIdLong
        val guildLocale = guild.locale
        val reason = event.entry.reason ?: "No reason provided"
        val moderationActionType =
            ModerationActionType.entries.find { it.offset == event.entry.type.ordinal } ?: ModerationActionType.UNKNOWN
        val moderator = event.entry.user ?: event.jda.selfUser

        val data = PunishmentContextData(
            event.jda,
            embedService,
            localizationService,
            webhookCacheView,
            userCacheView,
            guildCacheView,
            guildLocale,
            offenderId,
            moderator.idLong,
            reason,
            guild,
            moderationActionType
        )

        modActionHandler(data)

    }
}

data class PunishmentContextData(
    val jda: JDA,
    val embedService: EmbedService,
    val localizationService: LocalizationService,
    val webhookCacheView: WebhookCacheView,
    val userCacheView: UserCacheView,
    val guildCacheView: GuildCacheView,
    val locale: DiscordLocale,
    val offenderId: Long,
    val moderatorId: Long,
    val reason: String,
    val guild: JDAGuild,
    val moderationActionType: ModerationActionType
)

suspend fun modActionHandler(data: PunishmentContextData) {
    val offender = data.jda.retrieveUserById(data.offenderId).await()
    val moderator = data.jda.retrieveUserById(data.moderatorId).await()

    val cachedGuild = data.guildCacheView.save(data.guild)

    createModerationActionEntry(
        data.guild,
        moderator,
        offender,
        data.reason,
        data.moderationActionType,
        data.guildCacheView,
        data.userCacheView
    )

    val guildCaseEmbed = createGuildCaseEmbed(
        data.embedService,
        data.locale,
        data.offenderId,
        moderator,
        data.reason,
        data.moderationActionType,
        data.jda,
        data.localizationService
    ).toMessageEmbed()

    suspendTransaction {
        val type = EnumSet.of(LogChannelType.MOD_LOG)
        GuildLogChannel.getLogChannels(type, data.guild.idLong).forEach {
            data.webhookCacheView.sendMessageEmbeds(it.id.value, guildCaseEmbed)
        }
    }

    val userEmbed = createUserCaseEmbed(
        data.reason,
        data.moderationActionType,
        data.guild,
        data.locale,
        data.localizationService,
        cachedGuild.guildFeatures?.contains(GuildFeature.MOD_TICKET) ?: false
    )

    offender.openPrivateChannel().await()
        .sendView(userEmbed)
        .onErrorFlatMap(ErrorResponse.test(ErrorResponse.UNKNOWN_CHANNEL, ErrorResponse.CANNOT_SEND_TO_USER)) {
            return@onErrorFlatMap null
        }
        .await()

}

fun checkPreconditions(offender: User, moderator: Member?, guild: Guild): RestAction<Boolean> {
    return guild.retrieveMember(offender)
        .map { member ->
            moderator?.canInteract(member)!! && !member.isOwner
        }
}

private fun createGuildCaseEmbed(
    embedService: EmbedService,
    locale: DiscordLocale,
    offenderId: Long,
    moderator: User?,
    reason: String,
    moderationActionType: ModerationActionType,
    jda: JDA,
    localizationService: LocalizationService
): Embed {

    val bot = jda.selfUser

    val title = moderationActionType.identifier
    val timestamp = OffsetDateTime.now()
    val moderatorAsString = when {
        moderator == null -> bot.effectiveName
        else -> moderator.effectiveName
    }
    val modIcon = when {
        moderator == null -> bot.effectiveAvatarUrl
        else -> moderator.effectiveAvatarUrl
    }
    val selfUser = bot.effectiveName
    val selfUserIcon = bot.effectiveAvatarUrl

    val offenderAsString = "${userMention(offenderId)} || $offenderId"
    val reason = reason

    val offenderName = localizationService.getString(locale, "command.mod.modCase.fields.offenderName")
    val reasonName = localizationService.getString(locale, "command.mod.modCase.fields.reasonName")


    val embedArgs = mapOf(
        "title" to title,
        "timestamp" to timestamp,
        "mod" to moderatorAsString,
        "modIcon" to modIcon,
        "selfUser" to selfUser,
        "selfUserIcon" to selfUserIcon,
        "offender" to offenderAsString,
        "reason" to reason,
        "offenderName" to offenderName,
        "reasonName" to reasonName

    )


    return embedService.getLocalizedMessageEmbed("command.mod.modCase", locale, embedArgs)
}

private fun createUserCaseEmbed(
    reason: String,
    moderationActionType: ModerationActionType,
    guild: Guild,
    locale: DiscordLocale,
    localizationService: LocalizationService,
    modTicketEnabled: Boolean
) = view {
    compose {
        +container {
            accentColor = 0xff0000
            +text(localizationService.getString(locale, "punishment.createUserCaseEmbed.guildText", guild.name))
            +separator(true, Separator.Spacing.SMALL)
            +text(
                localizationService.getString(
                    locale,
                    "punishment.createUserCaseEmbed.effect",
                    moderationActionType.name.lowercase()
                )
            )
            +text(localizationService.getString(locale, "punishment.createUserCaseEmbed.reasonHeader", reason))
            if (modTicketEnabled) {
                +separator(true, Separator.Spacing.SMALL)
                +text(localizationService.getString(locale, "punishment.createUserCaseEmbed.complainsText"))
                +row {
                    +button(
                        ButtonStyle.SECONDARY,
                        localizationService.getString(locale, "punishment.createUserCaseEmbed.buttonLabel")
                    )
                    {
                        // TODO Implement Modmail-Function (Waiting for Feature-Release from JDUI)
                    }
                }
            }
        }
    }
}


private suspend fun createModerationActionEntry(
    guild: JDAGuild,
    moderator: JDAUser,
    offender: JDAUser,
    reason: String,
    type: ModerationActionType,
    guildCacheView: GuildCacheView,
    userCacheView: UserCacheView
) {
    val cachedGuild = guildCacheView.getById(guild.idLong) ?: guildCacheView.save(guild)
    val cachedModerator = userCacheView.getById(moderator.idLong) ?: userCacheView.save(moderator)
    val cachedOffender = userCacheView.getById(offender.idLong) ?: userCacheView.save(offender)
    suspendTransaction {
        ModerationAction.new {
            this.type = EnumSet.of(type)
            this.guild = cachedGuild
            this.reason = reason
            this.moderator = cachedModerator
            this.offender = cachedOffender
        }
    }

}