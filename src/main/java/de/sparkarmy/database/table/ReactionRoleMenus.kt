package de.sparkarmy.database.table

import de.sparkarmy.model.ReactionRoleMenuEntry
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.json.json

object ReactionRoleMenus : IdTable<Long>("table_reaction_role_menu") {
    override val id = reference("pk_fk_rrm_msg_id", Messages, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val entries = json<Array<ReactionRoleMenuEntry>>("rrm_entries", Json.Default)
    val description = varchar("rrm_description", 500).default("not provided")
    val guildId = reference("fk_rrm_guild_id", Guilds, ReferenceOption.CASCADE, ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)
}