package de.sparkarmy.commands.slash.mod

import de.sparkarmy.database.table.Channels
import de.sparkarmy.model.ModerationActionType
import dev.freya02.botcommands.jda.ktx.components.AttachmentUpload
import dev.freya02.botcommands.jda.ktx.components.Container
import dev.freya02.botcommands.jda.ktx.components.TextInput
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.EmbedBuilder
import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.to
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalData
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.create
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.awt.Color
import java.util.*

@Command
class Ban(private val button: Buttons, private val modals: Modals) {
    @TopLevelSlashCommandData(CommandScope.GUILD)
    @JDASlashCommand(name = "ban")
    @UserPermissions(Permission.BAN_MEMBERS, Permission.MODERATE_MEMBERS, Permission.KICK_MEMBERS)
    suspend fun onSlashInteraction(
        event: GuildSlashEvent,
        @SlashOption("user") member: Member,
        @SlashOption("reason") reason: String,
        @SlashOption("time") time: Long,
        @LocalizationBundle("EventMessages") lc: AppLocalizationContext
    ) {
        val guild = event.guild
        val user = event.user.retrieveProfile().await()

        member.user.openPrivateChannel()
            .awaitCatching()
            .onSuccess {
                it.sendMessageComponents(
                    createUserCaseEmbed(
                        reason = reason,
                        moderationActionType = ModerationActionType.BAN,
                        lc = lc,
                        guild = guild,
                        locale = Locale.GERMAN,
                        modTicketEnabled = true,
                        buttons = button,
                    )
                )
                    .useComponentsV2(true)
                    .queue()
            }
            .onFailure {

            }

        val moderationEmbed = EmbedBuilder {
            title = "Ban"
            color = Color.RED.rgb

        }

        event.replyUser(
            "punishment.success",
            "punishment_member" to member.effectiveName,
            "punishment_action" to "gebannt"
        )
            .setEphemeral(true)
            .queue()

    }
}

private suspend fun createUserCaseEmbed(
    reason: String,
    moderationActionType: ModerationActionType,
    lc: AppLocalizationContext,
    guild: Guild,
    locale: Locale,
    modTicketEnabled: Boolean,
    buttons: Buttons
) = Container {
    accentColor = Color.RED
    text(lc.localize(locale, "punishment.createUserCaseEmbed.guildText", "guild_name" to guild.name))
    separator(isDivider = true, spacing = Separator.Spacing.SMALL)
    text(
        lc.localize(
            locale,
            "punishment.createUserCaseEmbed.effect",
            "effect" to moderationActionType.identifier.lowercase()
        )
    )
    text(lc.localize(locale, "punishment.createUserCaseEmbed.reasonHeader", "reason" to reason))

    if (modTicketEnabled) {
        separator(isDivider = true, spacing = Separator.Spacing.SMALL)
        text(lc.localize(locale, "punishment.createUserCaseEmbed.complainsText"))
        actionRow {
            components += buttons.secondary(
                lc.localize(locale, "punishment.createUserCaseEmbed.buttonLabel"),
            ).persistent {

                noTimeout()

                bindTo("userCaseEmbedButtonInteraction")
            }
        }
    }
}


@Handler
class InfractionInteractionHandler(private val modals: Modals) {

    // Handler for the Interaction created by press the button created by function "createUserCaseEmbed"
    @JDAButtonListener("userCaseEmbedButtonInteraction")
    fun userCaseEmbedButtonInteraction(
        event: ButtonEvent,
    ) {
        // Get localized messages, because AppLocalizationContext trys to get bundles for guilds
        val modalTitle = event.getLocalizedMessage(event.userLocale.toLocale(),"punishment.modal.name")
        val modalComplaint = event.getLocalizedMessage(event.userLocale.toLocale(), "punishment.modal.complaint")
        val modalPictures = event.getLocalizedMessage(event.userLocale.toLocale(), "punishment.modal.pictures")

        val modal = modals.create(modalTitle) {
            label(modalComplaint) {
                child = TextInput("complaint", TextInputStyle.PARAGRAPH) {
                    minLength = 20
                }
            }
            label(modalPictures) {
                child = AttachmentUpload("pictures")
            }

            bindTo("infractionComplaintModalHandler",event.user)
        }

        event.replyModal(modal).queue()

    }

    @ModalHandler("infractionComplaintModalHandler")
    suspend fun onInfractionComplaintModal(
        event: ModalEvent,
        @ModalData member: User,
        @ModalInput("complaint") complaint: String,
        @ModalInput("pictures") pictures: List<Message.Attachment>
    ) {

        val channel = suspendTransaction {
            Channels.type
        }

    }
}
