package de.sparkarmy.jdui

import at.xirado.jdui.ContextBuilder
import at.xirado.jdui.JDUIListener
import at.xirado.jdui.config.JDUIConfig
import at.xirado.jdui.config.PersistenceConfig
import at.xirado.jdui.config.Secret
import at.xirado.jdui.config.ViewData
import at.xirado.jdui.event.DefaultEventAdapter
import com.github.benmanes.caffeine.cache.Scheduler
import de.mkammerer.snowflakeid.SnowflakeIdGenerator
import de.sparkarmy.coroutines.virtualDispatcher
import de.sparkarmy.data.cache.PersistentMessageCacheView
import net.dv8tion.jda.api.hooks.EventListener
import org.koin.core.annotation.Single


@Single
fun provideJDUI(cacheView: PersistentMessageCacheView, config: JduiConfig): EventListener {
    val config = JDUIConfig(
        snowflakeGenerator = SnowflakeIdGenerator.createDefault(1023),
        dispatcher = virtualDispatcher,
        eventAdapter = DefaultEventAdapter,
        scheduler = Scheduler.systemScheduler(),
        secret = Secret(config.password, ByteArray(32)),
        context = ContextBuilder().context,
        persistenceConfig = createPersistenceConfig(cacheView)


    )
    return JDUIListener(config)
}

private fun createPersistenceConfig(cacheView: PersistentMessageCacheView) = object : PersistenceConfig {
    override suspend fun retrieveState(id: Long): ViewData? {
        return cacheView.getById(id)?.let { ViewData(id, it.data) }
    }

    override suspend fun save(viewData: ViewData) {
        val id = viewData.id
        val data = viewData.data
        val clazz = viewData.javaClass.name
        cacheView.save(id, data, clazz)
    }
}