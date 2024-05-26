package de.sparkarmy.log

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import de.sparkarmy.utils.Util
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

fun initWebhookLogger(url: String) {
    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    val encoder = PatternLayoutEncoder().apply {
        this.context = lc
        this.pattern = "%highlight(%-5level) %msg"
        start()
    }

    WebhookAppender(url, encoder).apply {
        context = lc
        start()

        val logger = LoggerFactory.getLogger("ROOT") as Logger

        logger.addAppender(this)
    }
}

open class WebhookAppender(
    val url: String,
    private val encoder: PatternLayoutEncoder
) : AppenderBase<ILoggingEvent>() {
    companion object {
        private val guard = ThreadLocal.withInitial { false }
    }

    private val buffer = StringBuilder(2000)
    private lateinit var client: WebhookClient
    private lateinit var pool: ScheduledExecutorService

    override fun append(event: ILoggingEvent) {
        if (guard.get()) return
        val msg = encoder.encode(event).toString(Charsets.UTF_8)
        synchronized(buffer) {
            msg.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                if (buffer.length + line.length > 1900)
                    flush()
                buffer.append(line).append("\n")
            }
        }
    }

    private fun flush() = synchronized(buffer) {
        if (buffer.isEmpty()) return@synchronized
        val message = "```ansi\n${buffer}```"
        buffer.setLength(0)
        client.send(message).exceptionally { null }
    }

    override fun start() {

        pool = Executors.newSingleThreadScheduledExecutor {
            thread(start = false, isDaemon = true, name = "WebhookAppender") {
                guard.set(true)
                it.run()
            }
        }

        client = WebhookClientBuilder(url)
            .setWait(false)
            .setExecutorService(pool)
            .build()

        pool.scheduleAtFixedRate(this::flush, 5000, 5000, TimeUnit.MILLISECONDS)
        encoder.start()
        Util.logger.atInfo().log(buffer.toString())
        super.start()
    }

    override fun stop() {
        if (::pool.isInitialized)
            pool.shutdown()
        super.stop()
    }
}
