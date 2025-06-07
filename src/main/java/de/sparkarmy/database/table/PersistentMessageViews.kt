package de.sparkarmy.database.table

import org.jetbrains.exposed.v1.core.dao.id.IdTable

object PersistentMessageViews : IdTable<Long>("table_persistent_views") {
    override val id = long("pk_pvs_id").entityId()
    val data        = binary("pvs_data")
    val className   = varchar("pvs_class_name", 100)

    override val primaryKey = PrimaryKey(id)
}