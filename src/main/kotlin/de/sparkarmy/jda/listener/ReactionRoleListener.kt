package de.sparkarmy.jda.listener

import de.sparkarmy.data.DBContext
import de.sparkarmy.database.entity.ReactionRole
import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.interactions.InteractionHook

private val logger = KotlinLogging.logger {}

@Handler
@BService
class ReactionRoleListener(
    private val dbContext: DBContext
) {


    @JDAButtonListener("ReactionRole: toggle")
    suspend fun onButtonClick(event: ButtonEvent, @ComponentData reactionRoleId: Long) {
        logger.info { "Received a reaction-role: $reactionRoleId" }

        event.deferReply().setEphemeral(true).queue { hook ->
            runBlocking { toggleRole(event, reactionRoleId, hook) }
        }
    }

    private suspend fun toggleRole(
        event: ButtonEvent,
        reactionRoleId: Long,
        hook: InteractionHook
    ) {

        logger.info { "Start fun toggleRole" }

        val guild = event.guild ?: run {
            hook.editOriginal("Dieser Button kann nur in einem Server verwendet werden.").queue()
            return
        }
        val member = event.member ?: run {
            hook.editOriginal("Konnte den Benutzer nicht finden.").queue()
            return
        }

        val reactionRole = dbContext.doTransaction {
            ReactionRole.findById(reactionRoleId)
        } ?: run {
            hook.editOriginal("Diese Reaction-Role wurde nicht gefunden.").queue()
            return
        }

        val role = event.jda.getRoleById(reactionRole.roleId) ?: run {
            hook.editOriginal("Die zugehörige Rolle wurde nicht gefunden.").queue()
            return
        }

        if (reactionRole.guildId.value != guild.idLong) {
            hook.editOriginal("Diese Reaction-Role gehört nicht zu diesem Server.").queue()
            return
        }

        if (role.guild.idLong != guild.idLong) {
            hook.editOriginal("Die zugehörige Rolle gehört nicht zu diesem Server.").queue()
            return
        }

        val hasRole = member.roles.any { it.idLong == role.idLong }

        try {
            if (hasRole) {
                guild.removeRoleFromMember(member, role).await()
                hook.editOriginal("Die Rolle ${role.name} wurde dir entfernt.").queue()
            } else {
                guild.addRoleToMember(member, role).await()
                hook.editOriginal("Dir wurde die Rolle ${role.name} hinzugef\u00fcgt.").queue()
            }
        } catch (e: Exception) {
            hook.editOriginal("Fehler beim Ändern der Rolle: ${e.message ?: e.javaClass.simpleName}").queue()
            e.printStackTrace()
        }
    }
}