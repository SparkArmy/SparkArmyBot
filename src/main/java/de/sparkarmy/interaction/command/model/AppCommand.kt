package de.sparkarmy.interaction.command.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import de.sparkarmy.database.entity.Guild
import de.sparkarmy.database.entity.User
import de.sparkarmy.embed.EmbedService
import de.sparkarmy.i18n.LocalizationService
import de.sparkarmy.interaction.command.AppCommandHandler
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.model.GuildFlag
import de.sparkarmy.model.UserFlag
import de.sparkarmy.util.sha256
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.koin.core.component.KoinComponent
import java.util.*

private val objectMapper = ObjectMapper().apply {
    configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
}

private val log = KotlinLogging.logger {}

interface AppCommand<E : GenericCommandInteractionEvent> : KoinComponent {
    val commandData: CommandData
    val type: Command.Type
    val identifier: String
    var feature: GuildFeature?
    val requiredGuildFlags: EnumSet<GuildFlag>
    val requiredUserFlags: EnumSet<UserFlag>

    context(_: AppCommandHandler)
    fun initialize()

    context(_: AppCommandHandler)
    fun computeHash(): String {
        val commandMap = commandData.toData().toMap()
        val json = objectMapper.writeValueAsString(commandMap)

        return json.sha256()
    }

    suspend fun execute(event: E)
}

fun AppCommand<*>.contexts(vararg types: InteractionContextType) = contexts(types.toList())

fun AppCommand<*>.contexts(types: Collection<InteractionContextType>) {
    commandData.setContexts(types)
}

fun AppCommand<*>.integrationTypes(vararg types: IntegrationType) = integrationTypes(types.toList())

fun AppCommand<*>.integrationTypes(types: Collection<IntegrationType>) {
    commandData.setIntegrationTypes(types)
}

fun AppCommand<*>.isGlobal() = requiredGuildFlags.isEmpty() && feature == null

fun AppCommand<*>.isGuildCommandFor(guild: Guild, defaultFeatures: EnumSet<GuildFeature>): Boolean {
    if (isGlobal()) return false

    val guildFeatures = guild.guildFeatures ?: defaultFeatures

    feature?.let {
        if (it !in guildFeatures)
            return false
    }

    return requiredGuildFlags.all { it in guild.guildFlags }
}

fun AppCommand<*>.canExecute(user: User): Boolean {
    return requiredUserFlags.all { it in user.userFlags }
}

fun GenericCommandInteractionEvent.getIdentifier() = when (this) {
    is SlashCommandInteractionEvent -> "slash:$name"
    is MessageContextInteractionEvent -> "message:$name"
    is UserContextInteractionEvent -> "user:$name"
    else -> throw IllegalStateException("Unsupported interaction")
}

val AppCommand<*>.embedService: EmbedService
    get() = getKoin().get()

val AppCommand<*>.localizationService: LocalizationService
    get() = getKoin().get()