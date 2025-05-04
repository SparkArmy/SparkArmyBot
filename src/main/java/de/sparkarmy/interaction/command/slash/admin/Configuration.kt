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
import de.sparkarmy.database.entity.GuildPunishmentConfig
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.localizationService
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.model.LogChannelType
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
        +text(headerFirst(getLocalizeString("commands.configuration.embeds.conf_0.title")))
        +text(getLocalizeString("commands.configuration.embeds.conf_0.description"))
        +separator(true, Separator.Spacing.SMALL)
        +section(
            button(
                ButtonStyle.SECONDARY,
                getLocalizeString("commands.configuration.embeds.conf_0.punishmentFieldName")
            ) {

                newSuspendedTransaction {

                    val punishmentConfig = cachedGuild.guildPunishmentConfig
                    punishmentConfig?.let { GuildPunishmentConfig.new(cachedGuild.id.value) {} }

                    muteRole = punishmentConfig?.muteRole
                    warnRole = punishmentConfig?.warnRole
                }

                nextView = 100
            }) {
            +text(headerSecond(getLocalizeString("commands.configuration.embeds.conf_0.punishmentFieldName")))
            +text(getLocalizeString("commands.configuration.embeds.conf_0.punishmentFieldDescription"))
        }
        +separator(true, Separator.Spacing.SMALL)
        +section(
            button(
                ButtonStyle.SECONDARY,
                getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldName")
            ) {
                nextView = 200
            }
        ) {
            +text(getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldName"))
            +text(getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldDescription"))
        }
    }

    private fun Container.punishmentView() {
        +text(
            headerFirst(getLocalizeString("commands.configuration.embeds.conf_0.punishmentFieldName"))
        )
        +text(
            getLocalizeString("commands.configuration.embeds.conf_0.punishmentFieldDescription")
        )
        +separator(true, Separator.Spacing.SMALL)

        +section(
            button(
                style = when {
                    muteRole == null -> ButtonStyle.SECONDARY
                    else -> ButtonStyle.SUCCESS
                },
                getLocalizeString("commands.configuration.embeds.conf_100.muteRoleFieldName")
            ) {
                nextView = 101
            }) {
            val rolePart = when (muteRole) {
                null -> getLocalizeString("commands.configuration.embeds.conf_100.noRoleSet")
                else -> roleMention(muteRole!!)
            }
            +text(
                "${getLocalizeString("commands.configuration.embeds.conf_100.muteRoleFieldName")}: $rolePart"
            )
        }
        +section(
            button(
                style = when {
                    warnRole == null -> ButtonStyle.SECONDARY
                    else -> ButtonStyle.SUCCESS
                },
                getLocalizeString("commands.configuration.embeds.conf_100.warnRoleFieldName")
            ) {
                nextView = 102
            }
        ) {
            val rolePart = when (warnRole) {
                null -> getLocalizeString("commands.configuration.embeds.conf_100.noRoleSet")

                else -> roleMention(warnRole!!)
            }
            +text(
                "${getLocalizeString("commands.configuration.embeds.conf_100.warnRoleFieldName")}: $rolePart"
            )
        }
        +row {
            +button(
                ButtonStyle.SECONDARY,
                getLocalizeString("action.backButton")
            ) {
                nextView = 0
            }
        }
    }

    private fun Container.punishmentSelectView() {
        +text(
            headerFirst(
                getLocalizeString(
                    when (nextView) {
                        101 -> "commands.configuration.embeds.conf_101.title"
                        102 -> "commands.configuration.embeds.conf_102.title"
                        else -> "error"
                    }
                )
            )
        )
        +text(
            getLocalizeString(
                "commands.configuration.embeds.conf_100.roleSelectDescription"
            )
        )
        +separator(true, Separator.Spacing.SMALL)
        +row {
            +entitySelect(
                targets = listOf(EntitySelectMenu.SelectTarget.ROLE),
                placeholder = getLocalizeString(
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
                getLocalizeString("action.disableButton")
            ) {
                when (nextView) {
                    101 -> {
                        muteRole = null

                        newSuspendedTransaction {
                            cachedGuild.guildPunishmentConfig?.muteRole = muteRole
                        }
                    }

                    102 -> {
                        warnRole = null

                        newSuspendedTransaction {
                            cachedGuild.guildPunishmentConfig?.warnRole = warnRole
                        }
                    }
                }
                nextView = 100
            }
        }
    }

    private fun Container.logChannelConfigurationView() {
        LogChannelType.entries.map { SelectOption.of(it.name, it.id.toString()) }
        +text(headerFirst(getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldName")))
        +text(getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldDescription"))
    }

    private fun getLocalizeString(key: String, vararg arguments: Any) =
        localizationService.getString(locale, key, arguments)
}

data class ContextData(
    val cachedGuild: Guild,
    val jdaGuild: JDAGuild,
    val localizationService: LocalizationService,
    val locale: DiscordLocale
)





