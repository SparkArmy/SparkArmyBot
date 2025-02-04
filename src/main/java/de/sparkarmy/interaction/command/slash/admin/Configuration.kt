package de.sparkarmy.interaction.command.slash.admin

import de.sparkarmy.interaction.command.model.slash.SlashCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import org.koin.core.annotation.Single

@Single
class Configuration : SlashCommand("configuration", "Configures the server configs") {

    init {
        commandData.defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
    }
}