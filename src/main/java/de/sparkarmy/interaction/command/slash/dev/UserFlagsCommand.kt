package de.sparkarmy.interaction.command.slash.dev

import de.sparkarmy.data.cache.UserCacheView
import de.sparkarmy.interaction.command.model.slash.Handler
import de.sparkarmy.interaction.command.model.slash.SlashCommand
import de.sparkarmy.interaction.command.model.slash.Subcommand
import de.sparkarmy.interaction.command.model.slash.dsl.subcommand.option
import de.sparkarmy.model.GuildFlag
import de.sparkarmy.model.UserFlag
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.koin.core.annotation.Single

@Single
class UserFlagsCommand(
    private val userCacheView: UserCacheView,
) : SlashCommand("flags", "Read or modify user flags") {
    init {
        requiredGuildFlags += GuildFlag.DEV_GUILD
        requiredUserFlags += UserFlag.ADMIN
        subcommand(AddUserFlags(), ListUserFlags(), RemoveUserFlags())
    }

    inner class ListUserFlags : Subcommand("list", "List a users' flags") {
        init {
            option<User>("user", "The user")
        }

        @Handler(ephemeral = true)
        suspend fun run(event: SlashCommandInteractionEvent, user: User): MessageCreateData {
            val dbUser = userCacheView.save(user) {
                user
            }

            return MessageCreate {
                embeds += Embed {
                    color = 0x55ff88
                    author {
                        name = user.name
                        iconUrl = user.effectiveAvatarUrl
                    }
                    field {
                        name = "User flags"
                        value = if (dbUser.userFlags.isEmpty()) "No flags" else buildString {
                            dbUser.userFlags.forEach { flag ->
                                append(flag.name)
                                append(" (Offset ${flag.offset})")
                                append('\n')
                            }
                        }.trim()
                    }
                }
            }
        }
    }

    inner class AddUserFlags : Subcommand("add", "Add flag to a user") {
        init {
            option<String>("flag", "The flag")
            option<User>("user", "The user")
        }

        @Handler
        suspend fun run(event: SlashCommandInteractionEvent, user: User, flag: String) {

        }
    }

    inner class RemoveUserFlags : Subcommand("remove", "Remove flag from user") {
        init {
            option<User>("user", "The user")
            option<String>("flag", "The flag")
        }

        @Handler
        suspend fun run(event: SlashCommandInteractionEvent, user: User, flag: String) {

        }
    }
}