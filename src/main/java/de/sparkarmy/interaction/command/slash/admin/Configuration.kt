package de.sparkarmy.interaction.command.slash.admin

import at.xirado.jdui.component.message.*
import at.xirado.jdui.component.row
import at.xirado.jdui.context
import at.xirado.jdui.state.state
import at.xirado.jdui.view.View
import at.xirado.jdui.view.compose
import at.xirado.jdui.view.replyView
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.database.entity.Guild
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.localizationService
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.util.headerFirst
import de.sparkarmy.util.headerSecond
import de.sparkarmy.util.roleMention
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.components.selects.EntitySelectMenu
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import net.dv8tion.jda.api.entities.Guild as JDAGuild

@Single
class Configuration(
    val guildCacheView: GuildCacheView
) : SlashCommand("configuration", "Configures the server configs") {

    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
        contexts(InteractionContextType.GUILD)
    }

    @Handler
    suspend fun run(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!
        val cachedGuild = guildCacheView.getById(guild.idLong)

        if (cachedGuild == null) return

        val context = context {
            +ContextData(cachedGuild, guild, localizationService, event.userLocale)
        }
        event.replyView<ConfigurationView>(true, context).await()
    }
}

class ConfigurationView : View() {
    private val contextData: ContextData by context
    private lateinit var cachedGuild: Guild
    private lateinit var jdaGuild: JDAGuild
    private lateinit var localizationService: LocalizationService
    private lateinit var locale: DiscordLocale


    private var nextView: Int by state(0)
    private var muteRole: Long? by state(null)
    private var warnRole: Long? by state(null)


    override suspend fun initialize() {
        this.cachedGuild = contextData.cachedGuild
        this.jdaGuild = contextData.jdaGuild
        this.localizationService = contextData.localizationService
        this.locale = contextData.locale

        newSuspendedTransaction {
            muteRole = cachedGuild.guildPunishmentConfig?.muteRole
            warnRole = cachedGuild.guildPunishmentConfig?.warnRole
        }
    }

    override suspend fun createView() = compose {
        +container(0x6a0880) {
            when (nextView) {
                0 -> overview()
                100 -> punishmentView()
                101, 102 -> punishmentSelectView()
            }
            return@container
        }


    }

    private fun Container.overview() {
        +text(headerFirst(localizationService.getString(locale, "commands.configuration.embeds.conf_0.title")))
        +text(localizationService.getString(locale, "commands.configuration.embeds.conf_0.description"))
        +separator(true, Separator.Spacing.SMALL)
        +section(
            button(
                ButtonStyle.SECONDARY,
                localizationService.getString(locale, "commands.configuration.embeds.conf_0.punishmentFieldName")
            ) {
                nextView = 100
            }) {
            +text(
                headerSecond(
                    localizationService.getString(
                        locale,
                        "commands.configuration.embeds.conf_0.punishmentFieldName"
                    )
                )
            )
            +text(
                localizationService.getString(
                    locale,
                    "commands.configuration.embeds.conf_0.punishmentFieldDescription"
                )
            )
        }
    }

    private fun Container.punishmentView() {
        +text(
            headerFirst(
                localizationService.getString(
                    locale,
                    "commands.configuration.embeds.conf_100.title"
                )
            )
        )
        +text(
            localizationService.getString(
                locale,
                "commands.configuration.embeds.conf_100.description"
            )
        )
        +separator(true, Separator.Spacing.SMALL)

        +section(
            button(
                style = when {
                    muteRole == null -> ButtonStyle.SECONDARY
                    else -> ButtonStyle.SUCCESS
                },
                localizationService.getString(locale, "commands.configuration.embeds.conf_100.muteRoleFieldName")
            ) {
                nextView = 101
            }) {
            val rolePart = when (muteRole) {
                null -> localizationService.getString(
                    locale,
                    "commands.configuration.embeds.conf_100.noRoleSet"
                )

                else -> roleMention(muteRole!!)
            }
            +text(
                "${
                    localizationService.getString(
                        locale,
                        "commands.configuration.embeds.conf_100.muteRoleFieldName",
                    )
                }: $rolePart"
            )
        }
        +section(
            button(
                style = when {
                    warnRole == null -> ButtonStyle.SECONDARY
                    else -> ButtonStyle.SUCCESS
                },
                localizationService.getString(locale, "commands.configuration.embeds.conf_100.warnRoleFieldName")
            ) {
                nextView = 102
            }
        ) {
            val rolePart = when (muteRole) {
                null -> localizationService.getString(
                    locale,
                    "commands.configuration.embeds.conf_100.noRoleSet"
                )

                else -> roleMention(warnRole!!)
            }
            +text(
                "${
                    localizationService.getString(
                        locale,
                        "commands.configuration.embeds.conf_100.warnRoleFieldName",
                    )
                }: $rolePart"
            )
        }
        +row {
            +button(
                ButtonStyle.SECONDARY,
                localizationService.getString(locale, "action.backButton")
            ) {
                nextView = 0
            }
        }
    }

    private fun Container.punishmentSelectView() {
        +text(
            headerFirst(
                localizationService.getString(
                    locale,
                    when (nextView) {
                        101 -> "commands.configuration.embeds.conf_101.title"
                        102 -> "commands.configuration.embeds.conf_102.title"
                        else -> "error"
                    }

                )
            )
        )
        +text(
            localizationService.getString(
                locale,
                "commands.configuration.embeds.conf_100.roleSelectDescription"
            )
        )
        +separator(true, Separator.Spacing.SMALL)
        +row {
            +entitySelect(
                targets = listOf(EntitySelectMenu.SelectTarget.ROLE),
                placeholder = localizationService.getString(
                    locale,
                    "commands.configuration.embeds.conf_100.roleSelectPlaceholder"

                )
            ) {
                val selectedRole = mentions.roles[0].idLong
                when (nextView) {
                    101 -> {
                        muteRole = selectedRole

                        newSuspendedTransaction {
                            cachedGuild.guildPunishmentConfig?.muteRole = muteRole
                        }
                    }

                    102 -> {
                        warnRole = selectedRole

                        newSuspendedTransaction {
                            cachedGuild.guildPunishmentConfig?.warnRole = warnRole
                        }
                    }
                }
                nextView = 100
            }
        }
        +row {
            +button(
                ButtonStyle.DANGER,
                "action.disableButton"
            ) {
                nextView = 100
            }
        }
    }
}

data class ContextData(
    val cachedGuild: Guild,
    val jdaGuild: JDAGuild,
    val localizationService: LocalizationService,
    val locale: DiscordLocale
)





