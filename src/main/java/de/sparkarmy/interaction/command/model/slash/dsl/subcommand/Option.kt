package de.sparkarmy.interaction.command.model.slash.dsl.subcommand

import de.sparkarmy.interaction.command.model.slash.Subcommand
import de.sparkarmy.util.createOption
import net.dv8tion.jda.api.interactions.commands.build.OptionData

inline fun <reified T> Subcommand.option(
    name: String,
    description: String,
    autocomplete: Boolean = false,
    builder: OptionData.() -> Unit = {}
) {
    val option = createOption<T>(name, description, autocomplete, builder)
    subcommandData.addOptions(option)
}