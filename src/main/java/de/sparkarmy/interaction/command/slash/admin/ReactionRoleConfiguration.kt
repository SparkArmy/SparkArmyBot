package de.sparkarmy.interaction.command.slash.admin

import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.Subcommand
import de.sparkarmy.interaction.command.model.slash.dsl.subcommand.option
import de.sparkarmy.util.getLocalizedString
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import org.koin.core.annotation.Single

@Single
class ReactionRoleConfiguration(
    private val localizationService: LocalizationService
) : SlashCommand("reaction-roles", "Manage reaction roles") {

    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
        contexts(InteractionContextType.GUILD)
        subcommand(Create())
    }

    inner class Create : Subcommand("create", "Create a new reaction-role menu") {
        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent) {
            event.reply(
                event.getLocalizedString("action.featureNotImplemented", false)
            )
                .setEphemeral(true)
                .await()
        }
    }

    inner class Edit : Subcommand("edit", "Edit a reaction-role menu") {

        init {
            option<String>("message_id", "The message-id from the menu")
        }

        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent, messageId: String) {

        }
    }
}