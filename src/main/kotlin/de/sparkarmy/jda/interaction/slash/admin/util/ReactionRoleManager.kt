package de.sparkarmy.jda.interaction.slash.admin.util

import de.sparkarmy.data.cache.GuildCacheView
import de.sparkarmy.database.entity.ReactionRole
import de.sparkarmy.database.table.ReactionRoles
import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction


@BService
class ReactionRoleManager(
    private val context: BContext,
    private val guildCacheView: GuildCacheView,
    private val buttons: Buttons
) {

    private companion object {
        const val MAX_BUTTONS_PER_ROW = 5
        const val MAX_ROWS = 5
    }

    suspend fun createReactionRole(
        guild: Guild,
        messageId: Long,
        channelId: Long,
        roleId: Long,
        emoji: String?,
        description: String? = null,
        buttonText: String? = null
    ): Long {
        val dbGuild = guildCacheView.save(guild)
        val reactionRoleId = suspendTransaction {
            val reactionRole = ReactionRole.new {
                this.guildId = dbGuild.id
                this.messageId = messageId
                this.channelId = channelId
                this.roleId = roleId
                this.emoji = emoji
                this.description = description
                this.buttonText = buttonText
            }
            return@suspendTransaction reactionRole.id.value
        }
        updateMessageComponents(guild.idLong, messageId)
        return reactionRoleId
    }

    suspend fun deleteReactionRole(guildId: Long, messageId: Long, roleId: Long): Boolean {
        val channelId = suspendTransaction {
            val entities = ReactionRole.find {
                ReactionRoles.guildId eq guildId and
                    ReactionRoles.messageId.eq(messageId) and
                    ReactionRoles.roleId.eq(roleId)
            }
            val cid = entities.firstOrNull()?.channelId
            entities.forEach { it.delete() }
            return@suspendTransaction cid
        }
        if (channelId != null) {
            val channel = context.jda.getTextChannelById(channelId) ?: return true
            try {
                val message = channel.retrieveMessageById(messageId).await()
                val remaining = getReactionRolesForMessage(guildId, messageId)
                if (remaining.isEmpty()) {
                    message.editMessageComponents().queue()
                } else {
                    message.editMessageComponents(buildActionRows(remaining)).queue()
                }
            } catch (_: Exception) {
                return false
            }
        }
        return true
    }

    suspend fun getReactionRolesForMessage(guildId: Long, messageId: Long): List<ReactionRole> {
        return suspendTransaction {
            ReactionRole.find {
                ReactionRoles.guildId.eq(guildId) and
                    ReactionRoles.messageId.eq(messageId)
            }.toList()
        }
    }

    suspend fun getReactionRolesForGuild(guildId: Long): List<ReactionRole> {
        return suspendTransaction {
            ReactionRole.find { ReactionRoles.guildId eq guildId }.toList()
        }
    }

    private suspend fun updateMessageComponents(guildId: Long, messageId: Long) {
        val roles = getReactionRolesForMessage(guildId, messageId)
        if (roles.isEmpty()) {
            removeMessageComponents(guildId, messageId)
            return
        }

        val rows = buildActionRows(roles)
        val channelId = roles.first().channelId

        val channel = context.jda.getTextChannelById(channelId) ?: return

        val message = channel.retrieveMessageById(messageId).await() ?: return


        val content = roles.joinToString("\n") { r ->
            val role = context.jda.getRoleById(r.roleId)
            val name = role?.name ?: r.roleId.toString()
            if (r.description.isNullOrBlank()) name else "$name — ${r.description}"
        }
        message.editMessage(content).setComponents(rows).queue()
    }

    private suspend fun removeMessageComponents(guildId: Long, messageId: Long) {
        val roles = getReactionRolesForMessage(guildId, messageId)
        if (roles.isEmpty()) return

        val channelId = roles.first().channelId
        val channel = context.jda.getTextChannelById(channelId) ?: return

        val message = channel.retrieveMessageById(messageId).await() ?: return

        message.editMessageComponents().queue()
    }

    private suspend fun buildActionRows(roles: List<ReactionRole>): List<ActionRow> {
        val rows = mutableListOf<ActionRow>()
        var currentRow = mutableListOf<Button>()

        for (role in roles) {
            if (currentRow.size >= MAX_BUTTONS_PER_ROW) {
                rows.add(ActionRow.of(currentRow))
                currentRow = mutableListOf()
            }
            if (rows.size >= MAX_ROWS) break

            val roleName = context.jda.getRoleById(role.roleId)?.name ?: role.roleId.toString()
            val label = role.buttonText ?: roleName

            val button = buttons.secondary(label)
                .withEmoji(
                    when {
                        !role.emoji.isNullOrEmpty() -> net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode(role.emoji!!)
                        else -> null
                    }
                )
                .persistent {
                    bindTo("ReactionRole: toggle", role.id.value)
                }
            currentRow.add(button)
        }

        if (currentRow.isNotEmpty() && rows.size < MAX_ROWS) {
            rows.add(ActionRow.of(currentRow))
        }

        return rows
    }
}
