package de.sparkarmy.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger { }

// Interfaced service used to retrieve an SQL Connection
@BService
class DatabaseSource(config: Config) : HikariSourceSupplier {
    val exposed : Database

    override val source = HikariDataSource(HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        val url = "jdbc:postgresql://${config.database.host}:${config.database.port}/${config.database.database}"
        jdbcUrl = url
        username = config.database.username
        password = config.database.password

        // At most 2 JDBC connections, the database will suspend/block if all connections are used
        maximumPoolSize = 2
        // Emits a warning and does a thread/coroutine dump after the duration
        leakDetectionThreshold = 10.seconds.inWholeMilliseconds
    })

    init {
        //Migrate BC tables
        createFlyway("bc", "bc_database_scripts").migrate()

        //You can use the same function for your database, you have to change the schema and scripts location
        createFlyway(config.database.schema, "db/migration").migrate()


        exposed = Database.connect(source)

        logger.info { "Created database source" }
    }

    suspend fun <T> doTransaction(block: suspend Transaction.() -> T) : T {
        return suspendTransaction(db = exposed, statement = block)
    }

    private fun createFlyway(schema: String, scriptsLocation: String): Flyway = Flyway.configure()
        .dataSource(source)
        .schemas(schema)
        .locations(scriptsLocation)
        .validateMigrationNaming(true)
        .loggers("slf4j")
        .load()
}