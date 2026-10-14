package de.sparkarmy.jda.interaction.slash.mod

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.jda.filter.ModerationCommandFilter
import de.sparkarmy.model.PunishmentType
import dev.freya02.botcommands.jda.ktx.durations.ban
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.builder.filter
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.db.annotations.RequiresDatabase
import io.github.freya022.botcommands.api.core.entities.InputUser
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.replyUser
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.Permission
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

private val logger = KotlinLogging.logger { }

@Command
@RequiresDatabase
class PunishmentCommand(private val guildCacheView: GuildCacheView) : GuildApplicationCommandProvider {

    override fun declareGuildApplicationCommands(manager: GuildApplicationCommandManager) {

        manager.slashCommand("ban", ::slashBanCommand) {
            filters += filter<ModerationCommandFilter>()
            filters += filter<ModerationCommandFilter>()

            botPermissions += Permission.BAN_MEMBERS
            userPermissions += Permission.BAN_MEMBERS

            customOption("localizationContext")



            option("offender")
            option("reason") {
                lengthRange = LengthRange.of(10, 100)
            }
            aggregate(declaredName = "timeframe", aggregator = ::getDuration) {
                option(declaredName = "time")
                option(declaredName = "unit") {
                    usePredefinedChoices = true
                }
            }
        }
    }

    suspend fun slashBanCommand(
        event: GuildSlashEvent,
        offender: InputUser,
        reason: String,
        timeframe: Duration,
        @LocalizationBundle("Commands", prefix = "ban") localizationContext: AppLocalizationContext,
    ) {
        val moderator = event.member
        val offenderAsMember = offender.member

        val punishmentType = PunishmentType.BAN

        if (offenderAsMember == null) {
            event.replyUser(
                "punishment.event.failure",
                "punishment_member" to offender.asMention,
                "punishment_action" to punishmentType
            ).queue()
            return
        }

        if (moderator.canInteract(offenderAsMember)) {
            event.replyUser(
                "punishment.event.permissionFailure",
                "punishment_member" to offender.asMention,
                "punishment_action" to punishmentType
            ).queue()
            return
        }

        event.guild.ban(offender, timeframe)
            .reason(reason)
            .flatMap {
                event.replyUser(
                    "punishment.event.success",
                    "punishment_member" to offender.asMention,
                    "punishment_action" to punishmentType
                )
            }
            .onErrorFlatMap {
                event.replyUser(
                    "punishment.event.failure",
                    "punishment_member" to offender.asMention,
                    "punishment_action" to punishmentType
                )
            }
            .queue()


    }

    fun getDuration(time: Long, unit: TimeUnit): Duration {
        return time.toDuration(unit.toDurationUnit())
    }
}
