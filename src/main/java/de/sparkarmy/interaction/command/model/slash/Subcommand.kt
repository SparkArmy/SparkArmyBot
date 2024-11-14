package de.sparkarmy.interaction.command.model.slash

import de.sparkarmy.interaction.command.AppCommandHandler
import de.sparkarmy.util.checkCommandFunctionParameters
import de.sparkarmy.util.findFunctionWithAnnotation
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import kotlin.reflect.KFunction

abstract class Subcommand(name: String, description: String) {
    val subcommandData = SubcommandData(name, description)
    val handler: Pair<Handler, KFunction<*>> = findFunctionWithAnnotation<Handler>()
        ?: throw IllegalStateException("Missing handler function")

    context(AppCommandHandler)
    fun initialize() {
        checkCommandFunctionParameters(handler.second, subcommandData.options)
    }
}