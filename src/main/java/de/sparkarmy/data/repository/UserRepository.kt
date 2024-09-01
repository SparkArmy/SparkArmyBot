package de.sparkarmy.data.repository

import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.cacheBuilder
import de.sparkarmy.data.database.Database
import de.sparkarmy.data.database.entity.DiscordUser
import de.sparkarmy.misc.createCoroutineScope
import de.sparkarmy.misc.virtualDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

class UserRepository(private val database: Database) : AutoCloseable {
    private val job = SupervisorJob()
    private val coroutineScope = createCoroutineScope(virtualDispatcher, job)

    private val userCache: Cache<BigDecimal, DiscordUser> = cacheBuilder<BigDecimal, DiscordUser> {
        useCallingContext = false
        scope = coroutineScope
        expireAfterAccess = 10.minutes
    }.build()

    fun getUserDataBlocking(userId: BigDecimal, uName: String): DiscordUser {
        return runBlocking {
            userCache.get(userId) {
                transaction {
                    DiscordUser.findById(userId)
                        ?: DiscordUser.new(id = userId) { userName = uName }
                }
            }
        }
    }

    fun getUserDataBlocking(user: User): DiscordUser {
        val id = BigDecimal.valueOf(user.idLong)
        return runBlocking {
            userCache.get(id) {
                transaction {
                    DiscordUser.findById(id)
                        ?: DiscordUser.new(id = id) { userName = user.name }
                }
            }
        }
    }

    suspend fun getGuildDataAsync(userId: BigDecimal, uName: String): DiscordUser {
        return userCache.get(userId) {
            newSuspendedTransaction {
                DiscordUser.findById(userId)
                    ?: DiscordUser.new(id = userId) { userName = uName }
            }
        }
    }

    override fun close() {
        userCache.invalidateAll()

        job.cancel("Shutting down")
    }
}