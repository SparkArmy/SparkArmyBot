package de.sparkarmy.interaction.command.slash.admin

import at.xirado.jdui.component.message.Container
import at.xirado.jdui.component.message.container
import at.xirado.jdui.component.message.separator
import at.xirado.jdui.component.message.text
import at.xirado.jdui.context
import at.xirado.jdui.state.state
import at.xirado.jdui.view.View
import at.xirado.jdui.view.compose
import at.xirado.jdui.view.replyView
import de.sparkarmy.database.entity.ReactionRoleMenu
import de.sparkarmy.database.table.ReactionRoleMenus
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.Subcommand
import de.sparkarmy.interaction.command.model.slash.dsl.subcommand.option
import de.sparkarmy.model.JsonReactionRoleMenu
import de.sparkarmy.util.getLocalizedString
import de.sparkarmy.util.headerFirst
import de.sparkarmy.util.roleMention
import dev.minn.jda.ktx.coroutines.await
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.core.annotation.Single

@Single
class ReactionRoleConfiguration(
    private val localizationService: LocalizationService
) : SlashCommand("reaction-roles", "Manage reaction roles") {

    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
        contexts(InteractionContextType.GUILD)
        subcommand(Create(), Edit(), List())
    }

    inner class Create : Subcommand("create", "Create a new reaction-role menu") {

        init {
            option<String>("json","JSON-String to create a Reaction-Role Menu")
        }

        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent,json: String) {
            if (json.isEmpty()) {
                executeEvent(event)
                return
            }

            val menu = runCatching {
                Json.decodeFromString<JsonReactionRoleMenu>(json)
            }.getOrElse {
                event.reply(event.getLocalizedString("error"))
                    .setEphemeral(true)
                    .await()
                return
            }

            val context = context {
                +menu
            }

            event.replyView(::reactionRole,false,context).await()

        }
    }

    inner class Edit : Subcommand("edit", "Edit a reaction-role menu") {

        init {
            option<String>("message_id", "The message-id from the menu")
        }

        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent, messageId: String) {
            executeEvent(event)
        }
    }

    inner class List : Subcommand("list", "List of registered reaction-role menus") {
        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent) {
            val entries = suspendTransaction {
                val guildId = event.guild?.idLong
                val query = ReactionRoleMenus.select(ReactionRoleMenus.guildId eq guildId)
                ReactionRoleMenu.wrapRows(query)
            }
            suspendTransaction {
                when {
                    entries.empty() -> {
                        val reply = event.getLocalizedString("action.noEntries")
                        event.reply(reply).setEphemeral(true).await()
                        return@suspendTransaction true
                    }

                    else -> {
                        val context = context {
                            +ContextData(
                                entries,
                                event.userLocale,
                                localizationService,
                            )
                        }
                        event.replyView<ListView>(true, context).await()
                    }
                }
            }
        }
    }

    private suspend fun executeEvent(event: SlashCommandInteractionEvent) {
        event.reply(
            event.getLocalizedString("action.featureNotImplemented", false)
        )
            .setEphemeral(true)
            .await()
    }

// Max. 40 Components
// Einträge sollen nicht auseinandergerissen werden

    class ListView() : View() {

        private val contextData: ContextData by context
        private lateinit var entries: SizedIterable<ReactionRoleMenu>
        private lateinit var locale: DiscordLocale
        private lateinit var localizationService: LocalizationService

        private var site: Long by state { 0 }
        private var startIndex: Long by state { 0 }

        override suspend fun initialize() {
            this.entries = contextData.entries
            this.locale = contextData.locale
            this.localizationService = contextData.localizationService

        }

        override suspend fun createView() = compose {
            +container {
                page()
                return@container
            }
        }

        private fun Container.page() {
            var componentCount = 2
            +text(headerFirst(localizationService.getString(locale, "commands.reactionRoles.header")))

            val entryList = entries.offset(startIndex)
            var entryCount = 0

            for (entry in entryList) {
                if ((entry.entries.size * 3 + componentCount + 2) > 40) break
                +separator(true, Separator.Spacing.SMALL)
                +text(entry.description)
                componentCount += 2
                for (rkr in entry.entries) {
                    +text(rkr.header)
                    +text(rkr.description)
                    +text(roleMention(rkr.role))
                    componentCount += 3
                }
                entryCount += 1
            }

            startIndex += entryCount

        }

    }

    private data class ContextData(
        val entries: SizedIterable<ReactionRoleMenu>,
        val locale: DiscordLocale,
        val localizationService: LocalizationService,
    )
}