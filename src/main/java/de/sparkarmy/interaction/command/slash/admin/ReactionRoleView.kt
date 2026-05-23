package de.sparkarmy.interaction.command.slash.admin

import at.xirado.jdui.component.ViewContainer
import at.xirado.jdui.component.message.button
import at.xirado.jdui.component.message.section
import at.xirado.jdui.component.message.text
import at.xirado.jdui.state.state
import at.xirado.jdui.view.definition.function.view
import at.xirado.jdui.view.replyView
import de.sparkarmy.database.entity.ReactionRoleMenu
import de.sparkarmy.model.JsonReactionRoleMenu
import de.sparkarmy.util.headerFirst
import dev.minn.jda.ktx.coroutines.await
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.events.Events
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import okhttp3.internal.concurrent.Task
import org.jetbrains.exposed.v1.dao.withHook
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.reflect.KProperty

private val logger = KotlinLogging.logger {  }

fun reactionRole() = view {
    var messageId: Long by state { 0 }
    var menu: JsonReactionRoleMenu? by state { null }

    compose {
        menu = this@view.context.get<JsonReactionRoleMenu>()
            ?: suspendTransaction {
                val menuEntry = ReactionRoleMenu[messageId]
                JsonReactionRoleMenu(
                    menuEntry.description,
                    menuEntry.entries.toList()
                )
            }
        +text(headerFirst(menu!!.description))
        if (menu!!.entries.size > 3) {
            for (menuEntry in menu!!.entries) {

            }
        } else {
            for (menuEntry in menu!!.entries) {
                +section(
                    button(ButtonStyle.SECONDARY, menuEntry.header) {

                    }
                ) {
                    +text(menuEntry.header)
                    +text(menuEntry.description)
                }
            }
        }
    }

}