package de.sparkarmy.misc

import de.sparkarmy.Main
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val log = LoggerFactory.getLogger(Main::class.java)

val virtualExecutor by lazy { Executors.newVirtualThreadPerTaskExecutor() }
val virtualDispatcher by lazy { virtualExecutor.asCoroutineDispatcher() }

fun createCoroutineScope(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    job: Job = SupervisorJob(),
    errorHandler: CoroutineExceptionHandler? = null,
    context: CoroutineContext = EmptyCoroutineContext,
): CoroutineScope {
    val handler = errorHandler ?: CoroutineExceptionHandler { _, throwable ->
        log.error("Uncaught exception from coroutine", throwable)
        if (throwable is Error) {
            job.cancel()
            throw throwable
        }
    }
    return CoroutineScope(dispatcher + job + handler + context)
}