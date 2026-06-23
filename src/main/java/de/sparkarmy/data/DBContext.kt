package de.sparkarmy.data

import de.sparkarmy.coroutines.virtualDispatcher
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.sql.Connection

private val log = KotlinLogging.logger { }

@BService(priority = 100)
open class DBContext(private val db: DatabaseSource) {

    private val connectionContextHolder = ThreadLocal<Connection?>()

    suspend fun <T> doTransaction(block: suspend Transaction.() -> T): T {
        return suspendTransaction(db = db.exposed, statement = block)
    }

    suspend fun <T> withConnection(block: suspend Connection.() -> T): T {
        val existingConnection = connectionContextHolder.get()

        return if (existingConnection != null) {
            block(existingConnection)
        } else {
            withContext(virtualDispatcher) {
                log.debug { "Getting connection from pool" }
                db.getConnection().use { connection ->
                    connectionContextHolder.set(connection)
                    try {
                        block(connection)
                    } finally {
                        log.debug { "Dropping connection" }
                        connectionContextHolder.remove() // Cleanup after the outermost block
                    }
                }
            }
        }
    }
}