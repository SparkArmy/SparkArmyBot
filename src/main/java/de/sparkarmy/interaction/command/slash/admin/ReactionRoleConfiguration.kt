package de.sparkarmy.interaction.command.slash.admin

import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.Subcommand
import de.sparkarmy.interaction.command.model.slash.dsl.option
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import org.koin.core.annotation.Single

@Single
class ReactionRoleConfiguration : SlashCommand("reaction-roles", "Manage reaction roles") {

    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
        contexts(InteractionContextType.GUILD)
    }

    inner class Create : Subcommand("create", "Create a new reaction-role menu") {
        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent) {

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