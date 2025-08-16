package de.sparkarmy.database.table

import de.sparkarmy.model.ReactionRoleMenuEntry
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.json.json

object ReactionRoleMenus : IdTable<Long>("table_reaction_role_menu") {
    override val id = reference("pk_fk_rrm_msg_id", Messages, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val entries = json<Array<ReactionRoleMenuEntry>>("rrm_entries", Json.Default)

    override val primaryKey = PrimaryKey(id)
}