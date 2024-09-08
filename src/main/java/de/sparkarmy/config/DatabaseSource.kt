package de.sparkarmy.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.sparkarmy.data.db.tables.Users
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseSource(config: Config) {
    private val dbConfig = config.database
    private val dataSource: HikariDataSource
    val exposed: Database

    init {
        dataSource = getDataSource()
        createFlyway("bc","bc_database_scripts").migrate()

        val flyway = Flyway.configure().dataSource(dataSource).load()
        flyway.migrate()

        val (source,db) = connect()
        exposed = db

        createMissingTables()
    }

    private fun getDataSource(): HikariDataSource{
        val host = dbConfig.host
        val port = dbConfig.port
        val dbName = dbConfig.database

        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://$host:$port/$dbName"
            schema = dbConfig.schema
            username = dbConfig.username
            password = dbConfig.password
            maximumPoolSize = 8
        }
        return HikariDataSource(hikariConfig)
    }

    private fun connect(): Pair<HikariDataSource, Database> {
        val database = Database.connect(dataSource)
        return dataSource to database
    }

    private fun createFlyway(schema: String,scriptsLocation: String): Flyway = Flyway.configure()
        .dataSource(dataSource)
        .schemas(schema)
        .locations(scriptsLocation)
        .validateMigrationNaming(true)
        .load()

    private fun createMissingTables(){
        transaction(exposed) {
            SchemaUtils.createMissingTablesAndColumns(Users)
        }

    }
}