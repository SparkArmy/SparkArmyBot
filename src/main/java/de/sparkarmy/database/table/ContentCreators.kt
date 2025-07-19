package de.sparkarmy.database.table

import de.sparkarmy.model.PlatformType
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object ContentCreators : IdTable<String>("table_content_creator") {
    override val id = varchar("pk_cct_id", 500).entityId()
    val name = varchar("cct_name", 200)
    val platform = enumeration<PlatformType>("cct_platform")
}