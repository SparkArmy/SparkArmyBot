package de.sparkarmy.jda.interaction.slash.admin

import de.sparkarmy.database.entity.Guild
import de.sparkarmy.database.table.Guilds
import de.sparkarmy.jda.interaction.slash.admin.util.ReactionRoleManager
import de.sparkarmy.model.GuildFeature
import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GuildApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.localization.Localization
import io.github.freya022.botcommands.api.localization.annotations.LocalizationBundle
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.context.sendLocalized
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import net.dv8tion.jda.api.entities.Guild as JDAGuild

@Command
@BService
class ReactionRoleCommand(
    private val manager: ReactionRoleManager
) : GuildApplicationCommandProvider {

    override fun declareGuildApplicationCommands(manager: GuildApplicationCommandManager) {
        val guildEntity = transaction {
            Guild.find { Guilds.id eq manager.guild.idLong }.firstOrNull()
        }
        if (guildEntity?.guildFeatures?.contains(GuildFeature.REACTIONROLE) != true) return

        manager.ofSubcommands("reactionrole") {

            userPermissions += Permission.MANAGE_CHANNEL

            subcommand("create", ::create) {
                customOption("localizationContext")
                option("emoji") { }
                option("role") { }
                option("message") { }
                option("description") { }
                option("buttonText") { }
            }

            subcommand("delete", ::delete) {
                option("message") { }
                option("role") { }
            }

            subcommand("list", ::list) {
                customOption("localizationContext")
                option("message") { }
            }
        }
    }

    suspend fun create(
        event: GuildSlashEvent,
        @LocalizationBundle("EventMessages", prefix = "reactionrole") localizationContext: AppLocalizationContext,
        emoji: String?,
        role: Role,
        message: String? = null,
        description: String? = null,
        buttonText: String? = null
    ) {
        val member = event.member
        if (!member.hasPermission(Permission.MANAGE_ROLES)) {
            event.replyUser("reactionrole.permission_denied").queue()
            return
        }

        val guild = event.guild
        guild.getRoleById(role.idLong) ?: run {
            event.replyUser("reactionrole.role_not_found").setEphemeral(true).queue()
            return
        }

        val (_, messageId) = if (message != null) {
            val messageId = parseMessageId(message) ?: run {
                event.replyUser("reactionrole.message_id_parse_failed").setEphemeral(true).queue()
                return
            }
            val channelId = getChannelIdForMessage(guild, messageId) ?: run {
                event.replyUser("reactionrole.message_not_found").setEphemeral(true).queue()
                return
            }
            channelId to messageId
        } else {
            val sent = event.channel.sendLocalized(localizationContext, "create").await()
            sent.channelId to sent.idLong
        }

        manager.createReactionRole(guild, messageId, event.channelIdLong, role.idLong, emoji, description, buttonText)
        event.replyUser("reactionrole.created").setEphemeral(true).queue()
    }

    suspend fun delete(
        event: GuildSlashEvent,
        message: String,
        role: Role,
    ) {
        val member = event.member
        if (!member.hasPermission(Permission.MANAGE_ROLES)) {
            event.replyUser("reactionrole.permission_denied").setEphemeral(true).queue()
            return
        }

        val messageId = parseMessageId(message) ?: run {
            event.replyUser("reactionrole.message_id_parse_failed").setEphemeral(true).queue()
            return
        }

        try {
            val deleted = manager.deleteReactionRole(event.guild.idLong, messageId, role.idLong)
            if (deleted) {
                event.replyUser("reactionrole.deleted").setEphemeral(true).queue()
            } else {
                event.replyUser("reactionrole.not_found_combo").setEphemeral(true).queue()
            }
        } catch (e: Exception) {
            event.replyUser("reactionrole.delete_error", Localization.Entry("error", e.message ?: ""))
                .setEphemeral(true).queue()
        }
    }

    suspend fun list(
        event: GuildSlashEvent,
        @LocalizationBundle("EventMessages") localizationContext: AppLocalizationContext,
        message: String? = null
    ) {
        val guild = event.guild

        if (message != null) {
            val messageId = parseMessageId(message) ?: run {
                event.replyUser("reactionrole.message_id_parse_failed").setEphemeral(true).queue()
                return
            }
            listForMessage(guild.idLong, messageId, event, localizationContext)
        } else {
            listForGuild(guild.idLong, event)
        }
    }

    private suspend fun listForMessage(
        guildId: Long,
        messageId: Long,
        event: GuildSlashEvent,
        localizationContext: AppLocalizationContext
    ) {
        val roles = manager.getReactionRolesForMessage(guildId, messageId)
        val content = if (roles.isEmpty()) {
            localizationContext.localizeUser("reactionrole.list_empty_message")
//            "FÃ¼r diese Nachricht sind keine Reaction-Roles konfiguriert."
        } else {
            "Reaction-Roles für diese Nachricht:\n" +
                roles.joinToString("\n") { r ->
                    val role = event.jda.getRoleById(r.roleId) ?: return@joinToString "Unbekannte Rolle (${r.roleId})"
                    "â€¢ ${r.emoji} â†’ ${role.name}"
                }
        }
        event.reply(content).setEphemeral(true).queue()
    }

    private suspend fun listForGuild(guildId: Long, event: GuildSlashEvent) {
        val roles = manager.getReactionRolesForGuild(guildId)
        val content = if (roles.isEmpty()) {
            "FÃ¼r diesen Server sind keine Reaction-Roles konfiguriert."
        } else {
            "Reaction-Roles fÃ¼r diesen Server:\n" +
                roles.groupBy { it.messageId }
                    .map { (messageId, messageRoles) ->
                        val channel = event.jda.getTextChannelById(messageRoles.first().channelId)
                        val channelName = channel?.name ?: "Unbekannter Kanal"
                        "- Kanal #$channelName (Nachricht $messageId): ${messageRoles.joinToString(", ") { r ->
                            val role = event.jda.getRoleById(r.roleId) ?: return@joinToString "?"
                            "${r.emoji} ${role.name}"
                        }}"
                    }
                    .joinToString("\n")
        }
        event.reply(content).setEphemeral(true).queue()
    }

    private fun parseMessageId(input: String): Long? {
        val id = input.toLongOrNull()
        if (id != null) return id

        val pattern = """discord(?:app)?\.com/channels/(\d+)/(\d+)/(\d+)""".toRegex()
        val match = pattern.find(input)
        if (match != null) {
            return match.groupValues[3].toLongOrNull()
        }

        return null
    }

    private suspend fun getChannelIdForMessage(guild: JDAGuild, messageId: Long): Long? {
        return guild.textChannels.firstNotNullOfOrNull { channel ->
            channel.retrieveMessageById(messageId).await().channelIdLong
        }
    }
}
