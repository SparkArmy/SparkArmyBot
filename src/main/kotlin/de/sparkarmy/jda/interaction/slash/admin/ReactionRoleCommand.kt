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
        if (guildEntity?.guildFeatures?.contains(GuildFeature.MOD_TICKET) != true) return

        manager.ofSubcommands("reactionrole") {
            description = "Verwaltet Reaction-Roles für Nachrichten"

            subcommand("create", ::create) {
                description = "Erstellt eine Reaction-Role für eine Nachricht"

                option("message") { description = "Die Nachricht (Link oder ID, optional)" }
                option("emoji") { description = "Das Emoji für den Button" }
                option("role") { description = "Die Rolle die vergeben werden soll" }
                option("description") { description = "Beschreibung der Reaction-Role" }
                option("buttonText") { description = "Text auf dem Button" }
            }

            subcommand("delete", ::delete) {
                description = "Löscht eine Reaction-Role von einer Nachricht"

                option("message") { description = "Die Nachricht (Link oder ID)" }
                option("role") { description = "Die Rolle die entfernt werden soll" }
            }

            subcommand("list", ::list) {
                description = "Listet alle Reaction-Roles des Servers"

                option("message") { description = "Die Nachricht (Link oder ID, optional)" }
            }
        }
    }

    suspend fun create(
        event: GuildSlashEvent,
        message: String? = null,
        emoji: String?,
        role: Role,
        description: String? = null,
        buttonText: String? = null
    ) {
        val member = event.member
        if (!member.hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("Du hast keine Berechtigung, Rollen zu verwalten.").setEphemeral(true).queue()
            return
        }

        val guild = event.guild
        guild.getRoleById(role.idLong) ?: run {
            event.reply("Die angegebene Rolle wurde nicht gefunden.").setEphemeral(true).queue()
            return
        }

        val (_, messageId) = if (message != null) {
            val messageId = parseMessageId(message) ?: run {
                event.reply("Konnte die Nachrichten-ID nicht aus der Eingabe ermitteln.").setEphemeral(true).queue()
                return
            }
            val channelId = getChannelIdForMessage(guild, messageId) ?: run {
                event.reply("Die Nachricht wurde nicht gefunden oder der Bot hat keinen Zugriff darauf.").setEphemeral(true).queue()
                return
            }
            channelId to messageId
        } else {
            val sent = event.channel.sendMessage("Reaction-Role wird erstellt...").await()
            sent.channelId to sent.idLong
        }

        manager.createReactionRole(guild, messageId, event.channelIdLong, role.idLong, emoji, description, buttonText)
        event.reply("Reaction-Role wurde erfolgreich erstellt.").setEphemeral(true).queue()
    }

    suspend fun delete(
        event: GuildSlashEvent,
        message: String,
        role: Role
    ) {
        val member = event.member
        if (!member.hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("Du hast keine Berechtigung, Rollen zu verwalten.").setEphemeral(true).queue()
            return
        }

        val messageId = parseMessageId(message) ?: run {
            event.reply("Konnte die Nachrichten-ID nicht aus der Eingabe ermitteln.").setEphemeral(true).queue()
            return
        }

        try {
            val deleted = manager.deleteReactionRole(event.guild.idLong, messageId, role.idLong)
            if (deleted) {
                event.reply("Reaction-Role wurde erfolgreich gelöscht.").setEphemeral(true).queue()
            } else {
                event.reply("Keine Reaction-Role für diese Kombination gefunden.").setEphemeral(true).queue()
            }
        } catch (e: Exception) {
            event.reply("Fehler beim Löschen der Reaction-Role: ${e.message}").setEphemeral(true).queue()
        }
    }

    suspend fun list(
        event: GuildSlashEvent,
        message: String? = null
    ) {
        val guild = event.guild

        if (message != null) {
            val messageId = parseMessageId(message) ?: run {
                event.reply("Konnte die Nachrichten-ID nicht aus der Eingabe ermitteln.").setEphemeral(true).queue()
                return
            }
            listForMessage(guild.idLong, messageId, event)
        } else {
            listForGuild(guild.idLong, event)
        }
    }

    private suspend fun listForMessage(guildId: Long, messageId: Long, event: GuildSlashEvent) {
        val roles = manager.getReactionRolesForMessage(guildId, messageId)
        val content = if (roles.isEmpty()) {
            "Für diese Nachricht sind keine Reaction-Roles konfiguriert."
        } else {
            "Reaction-Roles für diese Nachricht:\n" +
                roles.joinToString("\n") { r ->
                    val role = event.jda.getRoleById(r.roleId) ?: return@joinToString "Unbekannte Rolle (${r.roleId})"
                    "• ${r.emoji} → ${role.name}"
                }
        }
        event.reply(content).setEphemeral(true).queue()
    }

    private suspend fun listForGuild(guildId: Long, event: GuildSlashEvent) {
        val roles = manager.getReactionRolesForGuild(guildId)
        val content = if (roles.isEmpty()) {
            "Für diesen Server sind keine Reaction-Roles konfiguriert."
        } else {
            "Reaction-Roles für diesen Server:\n" +
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
