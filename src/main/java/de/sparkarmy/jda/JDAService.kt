package de.sparkarmy.jda

import de.sparkarmy.coroutines.Virtual
import de.sparkarmy.coroutines.virtualExecutor
import de.sparkarmy.coroutines.virtualScheduledExecutor
import de.sparkarmy.misc.mapAsync
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val log = KotlinLogging.logger { }

@Single(createdAtStart = true)
class JDAService(
    private val config: JdaConfig,
    private val eventManager: JDAEventManager,
) : KoinComponent {
    lateinit var shardManager: ShardManager

    init {
        initialize()
    }

    fun initialize() {
        val token = config.token
        val intents = eventManager.requiredIntents
        val disabledCacheFlags = eventManager.disabledCacheFlags

        log.info { "Configured for gateway-intents: ${GatewayIntent.getRaw(intents)}" }
        log.info { "Cache flags: ${eventManager.enabledCacheFlags}" }

        shardManager = DefaultShardManagerBuilder.createDefault(token, intents)
            .disableCache(disabledCacheFlags)
            .setEventManagerProvider { eventManager }
            .setEventPassthrough(true)
            .setBulkDeleteSplittingEnabled(false)
            .setChunkingFilter(ChunkingFilter.NONE)
            .setEnableShutdownHook(false)
            .setCallbackPool(virtualExecutor)
            .setRateLimitElastic(virtualExecutor)
            .setGatewayPool(virtualScheduledExecutor)
            .setRateLimitScheduler(virtualScheduledExecutor)
            .setThreadFactory(Thread.ofVirtual().name("ShardManager").factory())
            .build()
    }

    fun shutdown() {
        val shards = shardManager.shards

        for (shard in shards)
            shard.shutdown()

        runBlocking {
            val jobs = shards.mapAsync(Dispatchers.Virtual) { shard ->
                if (!shard.awaitShutdown(10.seconds.toJavaDuration())) {
                    shard.shutdownNow()
                    shard.awaitShutdown()
                }
            }

            jobs.awaitAll()
            eventManager.cancel("Shutdown")
        }
    }
}