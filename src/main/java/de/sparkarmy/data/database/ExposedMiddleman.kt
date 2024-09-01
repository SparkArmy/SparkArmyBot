package de.sparkarmy.data.database

import de.sparkarmy.data.database.table.DiscordChannels.transform
import de.sparkarmy.data.database.table.DiscordGuilds
import de.sparkarmy.data.database.table.DiscordUsers
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import kotlin.reflect.KMutableProperty0

private val log = LoggerFactory.getLogger(Database::class.java)

fun createMissingTables(db: de.sparkarmy.data.database.Database) {
    transaction(db.exposed) {
        SchemaUtils.createMissingTablesAndColumns(DiscordUsers, DiscordGuilds)
    }

}

class TransactionContext<E : Entity<*>>(private val entity: E) {
    inline operator fun E.invoke(block: E.() -> Unit) {
        block()
    }

    fun <T> set(property: KMutableProperty0<T>, new: T) {
        property.set(new)
    }

    fun <T> update(property: KMutableProperty0<T>, block: T.() -> Unit) {
        val obj = property.get()
        block(obj)
        property.set(obj)
    }
}

fun <T> Column<List<T>>.asSet() = transform({ it.toList() }, { it.toSet().toList() })