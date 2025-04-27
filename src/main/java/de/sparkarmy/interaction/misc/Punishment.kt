package de.sparkarmy.interaction.misc

import at.xirado.jdui.component.message.button
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.separator
import at.xirado.jdui.component.message.text
import at.xirado.jdui.component.row
import at.xirado.jdui.view.definition.function.view
import de.sparkarmy.embed.EmbedService
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.model.Embed
import de.sparkarmy.model.PunishmentType
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.requests.RestAction

fun checkPreconditions(offender: User, moderator: Member?, guild: Guild): RestAction<Boolean> {
    return guild.retrieveMember(offender)
        .map { member ->
            moderator?.canInteract(member)!! && !member.isOwner
        }
}

fun createGuildCaseEmbed(
    embedService: EmbedService,
    locale: DiscordLocale,
    offender: Member,
    reason: String,
    punishmentType: PunishmentType,
    event: SlashCommandInteractionEvent
): Embed {

    val bot = offender.jda.selfUser
    val moderator = event.member

    val title = punishmentType.identifier
    val timestamp = event.timeCreated
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

    val offenderAsString = "${offender.asMention} || ${offender.id}"
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
    punishmentType: PunishmentType,
    guild: Guild,
    locale: DiscordLocale,
    localizationService: LocalizationService
) = view {
    compose {
        +container {
            accentColor = 0xff0000
            +text(punishmentType.name)
            +text(localizationService.getString(locale, "punishment.createUserCaseEmbed.guildText", guild.name))
            +separator(true, Separator.Spacing.SMALL)
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

