package de.sparkarmy.database.table

import org.jetbrains.exposed.dao.id.IdTable

object HashedCommands : IdTable<String>("table_command_hashes") {
    override val id = varchar("pk_cmh_identifier", 100).entityId()
    val hash        = char("cmh_hash", 64)
}