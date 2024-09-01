package de.sparkarmy.data.repository

import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.cacheBuilder
import de.sparkarmy.data.database.Database
import de.sparkarmy.data.database.entity.DiscordGuild
import de.sparkarmy.misc.createCoroutineScope
import de.sparkarmy.misc.virtualDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

class GuildRepository(database: Database) : AutoCloseable {
    private val job = SupervisorJob()
    private val coroutineScope = createCoroutineScope(virtualDispatcher, job)

    private val guildCache: Cache<BigDecimal, DiscordGuild> = cacheBuilder<BigDecimal, DiscordGuild> {
        useCallingContext = false
        scope = coroutineScope
        expireAfterAccess = 10.minutes
    }.build()

    fun getGuildDataBlocking(guildId: BigDecimal): DiscordGuild {
        return runBlocking {
            guildCache.get(guildId) {
                transaction {
                    DiscordGuild.findById(guildId)
                        ?: DiscordGuild.new(id = guildId) {}
                }
            }
        }
    }

    suspend fun getGuildDataAsync(guildId: BigDecimal): DiscordGuild {
        return guildCache.get(guildId) {
            newSuspendedTransaction {
                DiscordGuild.findById(guildId)
                    ?: DiscordGuild.new(id = guildId) {}
            }
        }
    }


    override fun close() {
        guildCache.invalidateAll()

        job.cancel("Shutting down")
    }

}
