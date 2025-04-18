package de.sparkarmy.interaction.command.model.user

import de.sparkarmy.interaction.command.AppCommandHandler
import de.sparkarmy.interaction.command.model.AppCommand
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.model.GuildFlag
import de.sparkarmy.model.UserFlag
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.util.*

abstract class UserContextCommand(name: String) : AppCommand<UserContextInteractionEvent> {
    override val commandData = Commands.user(name)
    override val type = Command.Type.USER
    override val identifier = "user:$name"
    override val requiredGuildFlags: EnumSet<GuildFlag> = EnumSet.noneOf(GuildFlag::class.java)
    override val requiredUserFlags: EnumSet<UserFlag> = EnumSet.noneOf(UserFlag::class.java)
    override var feature: GuildFeature? = null

    context(_: AppCommandHandler) override fun initialize() {
    }
}