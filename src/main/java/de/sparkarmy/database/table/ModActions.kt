package de.sparkarmy.database.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object ModActions : IdTable<Long>("table_mod_action") {
    override val id = long("pk_mda_id").entityId()
    val offender: Column<EntityID<Long>> =
        reference("fk_mda_offender_id", Users, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val guild: Column<EntityID<Long>> =
        reference("fk_mda_guild_id", Guilds, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val moderator: Column<EntityID<Long>> =
        reference("fk_mda_moderator_id", Users, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val reason = varchar("mda_reason", 100).default("No Reason Provided")

}