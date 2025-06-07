package de.sparkarmy.interaction.command.slash.admin

import at.xirado.jdui.component.ActionRow
import at.xirado.jdui.component.message.*
import at.xirado.jdui.component.row
import at.xirado.jdui.context
import at.xirado.jdui.state.state
import at.xirado.jdui.view.View
import at.xirado.jdui.view.compose
import at.xirado.jdui.view.replyView
import de.sparkarmy.data.cache.ChannelCacheView
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.data.cache.WebhookCacheView
import de.sparkarmy.database.entity.Guild
import de.sparkarmy.database.entity.GuildChannel
import de.sparkarmy.database.entity.GuildLogChannel
import de.sparkarmy.database.entity.GuildPunishmentConfig
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.interaction.command.AppCommandHandler
import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.localizationService
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.model.LogChannelType
import de.sparkarmy.util.headerFirst
import de.sparkarmy.util.headerSecond
import de.sparkarmy.util.roleMention
import dev.minn.jda.ktx.coroutines.await
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.components.selects.SelectOption
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.core.annotation.Single
import org.koin.core.component.inject
import java.util.*
import net.dv8tion.jda.api.components.selects.EntitySelectMenu.SelectTarget as JDASelectTarget
import net.dv8tion.jda.api.entities.Guild as JDAGuild


val log = KotlinLogging.logger("Configuration-Command")

@Single
class Configuration(
    private val guildCacheView: GuildCacheView,
    private val channelCacheView: ChannelCacheView,
    private val webhookCacheView: WebhookCacheView
) : SlashCommand("configuration", "Configures the server configs") {

    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
        contexts(InteractionContextType.GUILD)
    }

    private val appCommandHandler by inject<AppCommandHandler>()

    @Handler
    suspend fun run(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!
        val cachedGuild = guildCacheView.getById(guild.idLong)

        if (cachedGuild == null) return

        val context = context {
            +ContextData(
                cachedGuild,
                guild,
                localizationService,
                event.userLocale,
                channelCacheView,
                webhookCacheView,
                appCommandHandler
            )
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
    private lateinit var channelCacheView: ChannelCacheView
    private lateinit var webhookCacheView: WebhookCacheView
    private lateinit var appCommandHandler: AppCommandHandler


    private var nextView: Int by state(0)
    private var muteRole: Long? by state(null)
    private var warnRole: Long? by state(null)


    override suspend fun initialize() {
        this.cachedGuild = contextData.cachedGuild
        this.jdaGuild = contextData.jdaGuild
        this.localizationService = contextData.localizationService
        this.locale = contextData.locale
        this.channelCacheView = contextData.channelCacheView
        this.webhookCacheView = contextData.webhookCacheView
        this.appCommandHandler = contextData.appCommandHandler
    }

    override suspend fun createView() = compose {


        +container(0x6a0880) {
            when (nextView) {
                0 -> overview()
                1 -> errorView()
                100 -> punishmentView()
                101, 102 -> punishmentSelectView()
                200 -> logChannelConfigurationView()
                201, 202, 203, 204, 205, 206, 207, 208, 209, 210 -> logChannelConfigurationSelectView()
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

                suspendTransaction {

                    val punishmentConfig = cachedGuild.guildPunishmentConfig
                    if (punishmentConfig == null) GuildPunishmentConfig.new(cachedGuild.id.value) {}

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
            +text(headerFirst(getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldName")))
            +text(getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldDescription"))
        }
        +separator(true, Separator.Spacing.SMALL)
        +section(
            button(
                ButtonStyle.DANGER,
                getLocalizeString("action.update")
            ) {
                if (muteRole != null || warnRole != null) {
                    cachedGuild.guildFeatures?.plusAssign(GuildFeature.PUNISHMENT)
                } else {
                    cachedGuild.guildFeatures?.remove(GuildFeature.PUNISHMENT)
                }

                appCommandHandler.updateGuildCommands(jdaGuild, true)
                nextView = 0
            }
        ) {
            +text(getLocalizeString("commands.configuration.embeds.conf_0.updateConfigDescription"))
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
                targets = listOf(JDASelectTarget.ROLE),
                placeholder = getLocalizeString(
                    "commands.configuration.embeds.conf_100.roleSelectPlaceholder"
                )
            ) {
                val selectedRole = mentions.roles[0].idLong
                when (nextView) {
                    101 -> {
                        muteRole = selectedRole

                        suspendTransaction {
                            cachedGuild.guildPunishmentConfig?.muteRole = muteRole
                        }
                    }

                    102 -> {
                        warnRole = selectedRole

                        suspendTransaction {
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

                        suspendTransaction {
                            cachedGuild.guildPunishmentConfig?.muteRole = muteRole
                        }
                    }

                    102 -> {
                        warnRole = null

                        suspendTransaction {
                            cachedGuild.guildPunishmentConfig?.warnRole = warnRole
                        }
                    }
                }
                nextView = 100
            }
        }
    }

    private fun Container.logChannelConfigurationView() {
        val options = LogChannelType.entries.map { SelectOption.of(it.identifier, it.offset.toString()) }
        +text(headerFirst(getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldName")))
        +text(getLocalizeString("commands.configuration.embeds.conf_0.logChannelFieldDescription"))
        +separator(true, Separator.Spacing.SMALL)
        +text(getLocalizeString("commands.configuration.embeds.conf_200.channelSelectDescription"))
        +row {
            +stringSelect(
                options = options,
                placeholder = getLocalizeString("action.channelSelectPlaceholder")
            ) {
                nextView = 200 + values[0].toInt()
            }
        }

    }

    private fun Container.logChannelConfigurationSelectView() {
        val type = LogChannelType.entries.find { it.offset == (nextView - 200) }

        if (type == null) {
            nextView = 1
            return
        }

        +text(
            headerFirst(
                "${type.identifier}-${
                    getLocalizeString("commands.configuration.embeds.conf_200.channelSelectViewHeader")
                }"
            )
        )
        +text(
            getLocalizeString(
                "commands.configuration.embeds.conf_200.channelSelectViewDescription"
            )
        )
        +row {
            logChannelSelectRow(type)
        }
    }

    private fun ActionRow.logChannelSelectRow(type: LogChannelType) {
        append(
            entitySelect(
                targets = listOf(JDASelectTarget.CHANNEL),
                channelTypes = listOf(ChannelType.TEXT),
                placeholder = getLocalizeString("action.channelSelectPlaceholder")
            ) {
                val guildTextChannel = values[0] as TextChannel
                val textChannelId = guildTextChannel.idLong

                val cachedWebhook = webhookCacheView.getById(textChannelId)

                val typeEnumSet = EnumSet.of(type)

                if (cachedWebhook != null) {
                    suspendTransaction {
                        GuildLogChannel[textChannelId].channelType += type
                    }
                    return@entitySelect
                }

                val channelWebhookUrl = guildTextChannel.createWebhook(jda.selfUser.effectiveName).await().url

                suspendTransaction {
                    val channel = channelCacheView.getById(textChannelId)
                    if (channel == null) channelCacheView.save(guildTextChannel)

                    val guildChannel = GuildChannel.findById(textChannelId)
                    if (guildChannel == null) GuildChannel.new(textChannelId) { guild = cachedGuild }

                    GuildLogChannel.new(textChannelId) {
                        webhookUrl = channelWebhookUrl
                        channelType = typeEnumSet
                    }
                }
                val webhookChannel = suspendTransaction { GuildLogChannel[textChannelId] }
                webhookCacheView.save(jda, listOf(webhookChannel))

                nextView = 200
            }
        )
    }

    private fun Container.errorView() {
        accentColor = 0xe30b0b
        +text("Error, please use the command again")
    }

    private fun getLocalizeString(key: String, vararg arguments: Any) =
        localizationService.getString(locale, key, arguments)
}

data class ContextData(
    val cachedGuild: Guild,
    val jdaGuild: JDAGuild,
    val localizationService: LocalizationService,
    val locale: DiscordLocale,
    val channelCacheView: ChannelCacheView,
    val webhookCacheView: WebhookCacheView,
    val appCommandHandler: AppCommandHandler
)





