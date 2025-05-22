package de.sparkarmy.jda.listeners

import at.xirado.jdui.component.message.button
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.separator
import at.xirado.jdui.component.message.text
import at.xirado.jdui.component.row
import at.xirado.jdui.view.definition.function.view
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.UserCacheView
import de.sparkarmy.data.cache.WebhookCacheView
import de.sparkarmy.database.entity.GuildLogChannel
import de.sparkarmy.database.entity.ModerationAction
import de.sparkarmy.embed.EmbedService
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.jda.JDAEventListener
import de.sparkarmy.model.Embed
import de.sparkarmy.model.LogChannelType
import de.sparkarmy.model.ModerationActionType
import de.sparkarmy.model.toMessageEmbed
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
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.RestAction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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
    private val userCacheView: UserCacheView
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
        val logChannelType = EnumSet.of(LogChannelType.MOD_LOG)

        newSuspendedTransaction {
            createModerationActionEntry(guild, moderator.user, offender.user, reason)
            val guildEmbed = createGuildCaseEmbed(
                embedService,
                locale,
                offender.idLong,
                moderator.user,
                reason,
                ModerationActionType.TIMEOUT,
                event.jda
            ).toMessageEmbed()
            GuildLogChannel.getLogChannels(logChannelType, event.guild.idLong).forEach {
                webhookCacheView.sendMessageEmbeds(it.id.value, guildEmbed)
            }
        }
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
        val logChannelType = EnumSet.of(LogChannelType.MOD_LOG)

        newSuspendedTransaction {
            createModerationActionEntry(guild, moderator.idLong, offenderId, reason, event.jda)
            val guildEmbed = createGuildCaseEmbed(
                embedService,
                guildLocale,
                offenderId,
                moderator,
                reason,
                moderationActionType,
                event.jda
            ).toMessageEmbed()
            GuildLogChannel.getLogChannels(logChannelType, event.guild.idLong).forEach {
                webhookCacheView.sendMessageEmbeds(it.id.value, guildEmbed)
            }
        }

    }

    private suspend fun createModerationActionEntry(
        guild: JDAGuild,
        moderator: JDAUser,
        offender: JDAUser,
        reason: String
    ) {
        val cachedGuild = guildCacheView.getById(guild.idLong) ?: guildCacheView.save(guild)
        val cachedModerator = userCacheView.getById(moderator.idLong) ?: userCacheView.save(moderator)
        val cachedOffender = userCacheView.getById(offender.idLong) ?: userCacheView.save(offender)
        ModerationAction.new {
            this.guild = cachedGuild
            this.reason = reason
            this.moderator = cachedModerator
            this.offender = cachedOffender
        }
    }

    private suspend fun createModerationActionEntry(
        guild: JDAGuild,
        moderatorId: Long,
        offenderId: Long,
        reason: String,
        jda: JDA
    ) {
        val cachedGuild = guildCacheView.getById(guild.idLong) ?: guildCacheView.save(guild)
        val jdaModerator = jda.retrieveUserById(moderatorId).await()
        val cachedModerator = userCacheView.getById(moderatorId) ?: userCacheView.save(jdaModerator)
        val jdaOffender = jda.retrieveUserById(offenderId).await()
        val cachedOffender = userCacheView.getById(offenderId) ?: userCacheView.save(jdaOffender)
        ModerationAction.new {
            this.guild = cachedGuild
            this.reason = reason
            this.moderator = cachedModerator
            this.offender = cachedOffender
        }
    }
}

fun checkPreconditions(offender: User, moderator: Member?, guild: Guild): RestAction<Boolean> {
    return guild.retrieveMember(offender)
        .map { member ->
            moderator?.canInteract(member)!! && !member.isOwner
        }
}

fun createGuildCaseEmbed(
    embedService: EmbedService,
    locale: DiscordLocale,
    offenderId: Long,
    moderator: User?,
    reason: String,
    moderationActionType: ModerationActionType,
    jda: JDA
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


    val embedArgs = mapOf(
        "title" to title,
        "timestamp" to timestamp,
        "mod" to moderatorAsString,
        "modIcon" to modIcon,
        "selfUser" to selfUser,
        "selfUserIcon" to selfUserIcon,
        "offender" to offenderAsString,
        "reason" to reason,
        "offenderName" to "Offender", // TODO Add Localization
        "reasonName" to "Reason" // TODO Add Localization

    )


    return embedService.getLocalizedMessageEmbed("command.mod.modCase", locale, embedArgs)
}

fun createUserCaseEmbed(
    reason: String,
    moderationActionType: ModerationActionType,
    guild: Guild,
    locale: DiscordLocale,
    localizationService: LocalizationService
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
            +separator(true, Separator.Spacing.SMALL)
            +text(localizationService.getString(locale, "punishment.createUserCaseEmbed.complainsText"))
            +row {
                +button(
                    ButtonStyle.SECONDARY,
                    localizationService.getString(locale, "punishment.createUserCaseEmbed.buttonLabel")
                )
                {
                    // TODO Implement Modmail-Function
                }
            }
        }
    }
}