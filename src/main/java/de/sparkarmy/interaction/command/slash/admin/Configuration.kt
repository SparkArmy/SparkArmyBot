package de.sparkarmy.interaction.command.slash.admin


import at.xirado.jdui.component.MessageComponentCallbackResult
import at.xirado.jdui.component.message.button.Button
import at.xirado.jdui.component.message.select.EntitySelectMenu
import at.xirado.jdui.message.ChildMessageView
import at.xirado.jdui.message.messageBody
import at.xirado.jdui.message.messageComponents
import at.xirado.jdui.persistence.PersistentMessageConfig
import at.xirado.jdui.persistence.PersistentMessageView
import at.xirado.jdui.replyView
import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.database.entity.GuildPunishmentConfig
import de.sparkarmy.embed.EmbedService
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.interaction.command.model.contexts
import de.sparkarmy.interaction.command.model.embedService
import de.sparkarmy.interaction.command.model.localizationService
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.model.toMessageEmbed
import dev.minn.jda.ktx.coroutines.await
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single

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
        if (event.guild == null) {
            event.reply("You have to use this command on a guild").await()
            return
        }
        event.replyView(Overview(event, localizationService, embedService, guildCacheView)).setEphemeral(true).await()
    }


}

class Overview(
    val event: GenericInteractionCreateEvent,
    val localizationService: LocalizationService,
    val embedService: EmbedService,
    val guildCacheView: GuildCacheView
) : PersistentMessageView<OverviewState>(OverviewConfig), ChildMessageView<Overview> {
    private var site: Int by state::site
    private val stringPool = "commands.configuration.embeds.conf_${site}"
    private val userLocale = event.userLocale

    private val punishmentButton = Button(
        "conf_punishmentButton",
        ButtonStyle.SECONDARY,
        label = localizationService.getString(
            userLocale,
            "${stringPool}.punishmentFieldName"
        )
    ) { _, bEvent ->
        MessageComponentCallbackResult.ChildView(
            PunishmentView(
                bEvent,
                localizationService,
                embedService,
                guildCacheView
            )
        ) {}
    }

    override suspend fun createMessage() = messageBody {

        val stringPool = "commands.configuration.embeds.conf_0"
        val userLocale = event.userLocale
        val title = localizationService.getString(userLocale, "${stringPool}.title")
        val description = localizationService.getString(userLocale, "${stringPool}.description")
        val name = localizationService.getString(userLocale, "${stringPool}.punishmentFieldName")
        val value = localizationService.getString(userLocale, "${stringPool}.punishmentFieldDescription")

        val args = mapOf(
            "title" to title,
            "description" to description,
            "punishmentFieldName" to name,
            "punishmentFieldValue" to value
        )

        embeds += getConfEmbedFromSite(embedService, site, userLocale, args)

    }

    override suspend fun defineComponents() = messageComponents {
        row {
            +punishmentButton
        }
    }

}

class PunishmentView(
    val event: GenericInteractionCreateEvent,
    val localizationService: LocalizationService,
    val embedService: EmbedService,
    val guildCacheView: GuildCacheView
) : ChildMessageView<Overview> {
    private var site: Int = 100
    private val stringPool = "commands.configuration.embeds.conf_${site}"
    private val userLocale = event.userLocale

    override suspend fun createMessage() = messageBody {

        val title = localizationService.getString(userLocale, "${stringPool}.title")
        val description = localizationService.getString(userLocale, "${stringPool}.description")
        val muteRoleFieldName = localizationService.getString(userLocale, "${stringPool}.muteRoleFieldName")
        val muteRoleFieldValue = localizationService.getString(userLocale, "${stringPool}.muteRoleFieldValue")
        val warnRoleFieldName = localizationService.getString(userLocale, "${stringPool}.warnRoleFieldName")
        val warnRoleFieldValue = localizationService.getString(userLocale, "${stringPool}.warnRoleFieldValue")


        val args = mapOf(
            "title" to title,
            "description" to description,
            "muteRoleFieldName" to muteRoleFieldName,
            "muteRoleFieldValue" to muteRoleFieldValue,
            "warnRoleFieldName" to warnRoleFieldName,
            "warnRoleFieldValue" to warnRoleFieldValue
        )

        embeds += getConfEmbedFromSite(embedService, site, userLocale, args)
    }

    private suspend fun muteRoleButton(): Button = Button(
        "conf_muteRoleButton",
        when {
            newSuspendedTransaction {
                val gId = event.guild?.idLong

                if (gId == null) return@newSuspendedTransaction false

                val cG = guildCacheView.getById(gId)

                cG?.guildPunishmentConfig?.muteRole != null

            } -> ButtonStyle.SUCCESS

            else -> ButtonStyle.SECONDARY
        },
        localizationService.getString(userLocale, "${stringPool}.muteRoleFieldName"))
    { _, bEvent ->
        MessageComponentCallbackResult.ChildView(
            MutePunishmentView(
                bEvent,
                localizationService,
                embedService,
                guildCacheView
            )
        ) {}
    }

    private suspend fun warnRoleButton(): Button = Button(
        "conf_warnRoleButton",
        when {
            newSuspendedTransaction {
                val gId = event.guild?.idLong

                if (gId == null) return@newSuspendedTransaction false

                val cG = guildCacheView.getById(gId)

                cG?.guildPunishmentConfig?.warnRole != null

            } -> ButtonStyle.SUCCESS

            else -> ButtonStyle.SECONDARY
        },
        localizationService.getString(userLocale, "${stringPool}.warnRoleFieldName"))
    { _, bEvent ->
        MessageComponentCallbackResult.ChildView(
            WarnPunishmentView(
                bEvent,
                localizationService,
                embedService,
                guildCacheView
            )
        ) {}
    }

    private val goBackButton = Button(
        "conf_PunishToOverviewBackButton",
        ButtonStyle.PRIMARY,
        localizationService.getString(userLocale, "action.backButton")
    )
    { _, bEvent ->
        MessageComponentCallbackResult.ChildView(Overview(bEvent, localizationService, embedService, guildCacheView)) {}
    }


    override suspend fun defineComponents() = messageComponents {
        val muteRoleButton = muteRoleButton()
        val warnRoleButton = warnRoleButton()
        row {
            +muteRoleButton
            +warnRoleButton
            +goBackButton
        }
    }

}

class MutePunishmentView(
    val event: ButtonInteractionEvent,
    val localizationService: LocalizationService,
    val embedService: EmbedService,
    val guildCacheView: GuildCacheView
) : ChildMessageView<PunishmentView> {
    private var site: Int = 101
    private val stringPool = "commands.configuration.embeds.conf_${site}"
    private val userLocale = event.userLocale


    override suspend fun createMessage() =
        getMessageBodyForPunishmentChildViews(localizationService, embedService, site, stringPool, userLocale)


    private val roleSelectMenu: EntitySelectMenu = EntitySelectMenu(
        "conf_muteRoleSelect",
        listOf(net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.ROLE),
        localizationService.getString(userLocale, "${stringPool}.muteRoleSelectPlaceholder")
    ) { _, eEvent ->
        newSuspendedTransaction {
            val guildPunishmentConfig = guildCacheView.getById(eEvent.guild?.idLong!!)?.guildPunishmentConfig
            when {
                guildPunishmentConfig == null -> GuildPunishmentConfig.new(eEvent.guild?.idLong) {
                    muteRole = eEvent.mentions.roles.component1().idLong
                }

                else -> guildPunishmentConfig.muteRole = eEvent.mentions.roles.component1().idLong
            }
        }
        MessageComponentCallbackResult.ChildView(
            PunishmentView(
                eEvent,
                localizationService,
                embedService,
                guildCacheView
            )
        ) {}
    }

    private val disableMute: Button = Button(
        "conf_muteDisableButton",
        ButtonStyle.DANGER,
        localizationService.getString(userLocale, "${stringPool}.muteRoleDisableButtonLabel")
    ) { _, bEvent ->
        newSuspendedTransaction {
            val guildPunishmentConfig = guildCacheView.getById(bEvent.guild?.idLong!!)?.guildPunishmentConfig
            when {
                guildPunishmentConfig == null -> GuildPunishmentConfig.new(bEvent.guild?.idLong) {}
                else -> guildPunishmentConfig.muteRole = null
            }
            MessageComponentCallbackResult.ChildView(
                PunishmentView(
                    bEvent,
                    localizationService,
                    embedService,
                    guildCacheView
                )
            ) {}
        }
    }

    private val goBackButton: Button = Button(
        "conf_MuteToPunishmentBackButton",
        ButtonStyle.SECONDARY,
        localizationService.getString(userLocale, "action.backButton")
    ) { _, bEvent ->
        MessageComponentCallbackResult.ChildView(
            PunishmentView(
                bEvent,
                localizationService,
                embedService,
                guildCacheView
            )
        ) {}
    }


    override suspend fun defineComponents() = messageComponents {
        row {
            +roleSelectMenu
        }
        row {
            +disableMute
            +goBackButton
        }
    }
}

class WarnPunishmentView(
    val event: ButtonInteractionEvent,
    val localizationService: LocalizationService,
    val embedService: EmbedService,
    val guildCacheView: GuildCacheView
) : ChildMessageView<PunishmentView> {
    private var site: Int = 102
    private val stringPool = "commands.configuration.embeds.conf_${site}"
    private val userLocale = event.userLocale


    override suspend fun createMessage() =
        getMessageBodyForPunishmentChildViews(localizationService, embedService, site, stringPool, userLocale)

    private val roleSelectMenu: EntitySelectMenu = EntitySelectMenu(
        "conf_warnRoleSelect",
        listOf(net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.ROLE),
        localizationService.getString(userLocale, "${stringPool}.warnRoleSelectPlaceholder")
    ) { _, eEvent ->
        newSuspendedTransaction {
            val guildPunishmentConfig = guildCacheView.getById(eEvent.guild?.idLong!!)?.guildPunishmentConfig
            when {
                guildPunishmentConfig == null -> GuildPunishmentConfig.new(eEvent.guild?.idLong) {
                    warnRole = eEvent.mentions.roles.component1().idLong
                }

                else -> guildPunishmentConfig.warnRole = eEvent.mentions.roles.component1().idLong
            }
        }
        MessageComponentCallbackResult.ChildView(
            PunishmentView(
                eEvent,
                localizationService,
                embedService,
                guildCacheView
            )
        ) {}
    }

    private val disableWarn: Button = Button(
        "conf_warnDisableButton",
        ButtonStyle.DANGER,
        localizationService.getString(userLocale, "${stringPool}.warnRoleDisableButtonLabel")
    ) { _, bEvent ->
        newSuspendedTransaction {
            val guildPunishmentConfig = guildCacheView.getById(bEvent.guild?.idLong!!)?.guildPunishmentConfig
            when {
                guildPunishmentConfig == null -> GuildPunishmentConfig.new(bEvent.guild?.idLong) {}
                else -> guildPunishmentConfig.warnRole = null
            }
            MessageComponentCallbackResult.ChildView(
                PunishmentView(
                    bEvent,
                    localizationService,
                    embedService,
                    guildCacheView
                )
            ) {}
        }
    }

    private val goBackButton: Button = Button(
        "conf_WarnToPunishmentBackButton",
        ButtonStyle.SECONDARY,
        localizationService.getString(userLocale, "action.backButton")
    ) { _, bEvent ->
        MessageComponentCallbackResult.ChildView(
            PunishmentView(
                bEvent,
                localizationService,
                embedService,
                guildCacheView
            )
        ) {}
    }

    override suspend fun defineComponents() = messageComponents {
        row {
            +roleSelectMenu
        }

        row {
            +disableWarn
            +goBackButton
        }
    }


}


@Serializable
data class OverviewState(
    var site: Int = 0
)

private object OverviewConfig : PersistentMessageConfig<OverviewState> {
    override val serializer = OverviewState.serializer()

}

private fun getMessageBodyForPunishmentChildViews(
    localizationService: LocalizationService,
    embedService: EmbedService,
    site: Int,
    stringPool: String,
    userLocale: DiscordLocale
) = messageBody {
    val title = localizationService.getString(userLocale, "${stringPool}.title")
    val description = localizationService.getString(userLocale, "${stringPool}.description")

    val args = mapOf(
        "title" to title,
        "description" to description
    )

    embeds += getConfEmbedFromSite(embedService, site, userLocale, args)
}

private fun getConfEmbedFromSite(
    embedService: EmbedService, site: Int, userLocale: DiscordLocale,
    args: Map<String, Any?>
): MessageEmbed {
    return embedService.getLocalizedMessageEmbed("command.conf.conf_${site}", userLocale, args).toMessageEmbed()
}





