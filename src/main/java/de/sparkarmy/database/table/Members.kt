package de.sparkarmy.database.table

import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption

object Members: CompositeIdTable("table_member") {
    val user: Column<EntityID<Long>> = reference("pk_fk_mbr_user_id", Users, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val guild: Column<EntityID<Long>> = reference("pk_fk_mbr_guild_id", Guilds, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val memberFlags: Column<Long> = long("mbr_flags").default(0)

    init {
        addIdColumn(user)
        addIdColumn(guild)
    }

    override val primaryKey: PrimaryKey = PrimaryKey(user,guild)
}