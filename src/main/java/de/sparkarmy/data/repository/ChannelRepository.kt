package de.sparkarmy.data.repository

import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.cacheBuilder
import de.sparkarmy.data.database.Database
import de.sparkarmy.data.database.entity.DiscordChannel
import de.sparkarmy.data.database.entity.DiscordGuild
import de.sparkarmy.data.database.table.DiscordChannels
import de.sparkarmy.misc.createCoroutineScope
import de.sparkarmy.misc.virtualDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

class ChannelRepository(database: Database) : AutoCloseable {
    private val job = SupervisorJob()
    private val coroutineScope = createCoroutineScope(virtualDispatcher, job)

    private val channelCache: Cache<BigDecimal, DiscordChannel> = cacheBuilder<BigDecimal, DiscordChannel> {
        useCallingContext = false
        scope = coroutineScope
        expireAfterAccess = 10.minutes
    }.build()

    fun getChannelDataBlocking(channelId: BigDecimal, gId: BigDecimal): DiscordChannel {
        return runBlocking {
            channelCache.get(channelId) {
                transaction {
                    DiscordChannel.findById(channelId)
                        ?: DiscordChannel.new(id = channelId) {
                            discordGuildId = DiscordGuild.findById(gId) ?: DiscordGuild.new(gId) {}
                        }
                }
            }
        }
    }

    fun getChannelDataBlocking(channel: GuildChannel): DiscordChannel {
        val channelId = channel.id.toBigDecimal()
        val guildId = channel.guild.id.toBigDecimal()
        return runBlocking {
            channelCache.get(channelId) {
                transaction {
                    DiscordChannel.findById(channelId)
                        ?: DiscordChannel.new(id = channelId) {
                            discordGuildId = DiscordGuild.findById(guildId) ?: DiscordGuild.new(guildId) {}
                        }
                }
            }
        }
    }

    fun getGuildDiscordChannelList(guildId: BigDecimal): List<DiscordChannel> {
        return transaction {
            DiscordChannel.find(DiscordChannels.guildId eq guildId).toList()
        }
    }

    override fun close() {
        channelCache.invalidateAll()

        job.cancel("Shutting down")
    }

}
