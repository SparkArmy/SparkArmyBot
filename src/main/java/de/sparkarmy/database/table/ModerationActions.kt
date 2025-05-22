package de.sparkarmy.database.table

import de.sparkarmy.data.bitField
import de.sparkarmy.model.ModerationActionType
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.charLength

object ModerationActions : IdTable<Long>("table_moderation_action") {
    override val id = long("psm_id").entityId().autoIncrement()
    val type = short("psm_type").bitField<Short, ModerationActionType>()
    val offender = reference("fk_psm_offender_user_id", Users, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val moderator = reference("fk_psm_moderator_user_id", Users, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val guild = reference("fk_psm_guild_id", Guilds, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val reason = varchar("psm_reason", 512).check { it.charLength().greater(10) }
}