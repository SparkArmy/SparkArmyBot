package de.sparkarmy.data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.sparkarmy.config.DatabaseConfig
import org.jetbrains.exposed.sql.Database
import java.sql.Connection

class Database(private val config: DatabaseConfig) {
    private val dataSource: HikariDataSource
    val exposed: Database

    init {
        val (source, db) = connect()
        dataSource = source
        exposed = db
        createMissingTables(this)
    }

    private fun connect(): Pair<HikariDataSource, Database> {

        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://${config.url}"
            username = config.user
            password = config.password
            maximumPoolSize = 8
            connectionTimeout = 2000
            validationTimeout = 2000
            setInitializationFailTimeout(0)
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }

        val dataSource = HikariDataSource(hikariConfig)
        val database = Database.connect(dataSource)

        return dataSource to database
    }

    fun getConnectionFromPool(): Connection = dataSource.connection
}