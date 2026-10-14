package de.sparkarmy.jda.filter

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.model.GuildFeature
import de.sparkarmy.util.checkGuildMember
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

@BService
class ModerationCommandFilter(val guildCacheView: GuildCacheView) : ApplicationCommandFilter {
    override val global: Boolean get() = false

    override suspend fun checkSuspend(
        event: GenericCommandInteractionEvent,
        commandInfo: ApplicationCommandInfo
    ): String? {

        val guild = event.guild
        val member = event.member

        val check = checkGuildMember(member, guild)
        if (check != null) return check

        val dbGuild = guildCacheView.save(guild!!)

        if (dbGuild.guildFeatures.isNullOrEmpty() or !(dbGuild.guildFeatures?.contains(GuildFeature.MODERATION)
                ?: true)
        ) {
            event.reply("The 'Moderation-Feature' is not enabled on this server!").queue()
            return "Feature not enabled"
        }

        return null

    }
}