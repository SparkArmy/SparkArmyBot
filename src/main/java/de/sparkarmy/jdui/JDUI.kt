package de.sparkarmy.jdui

import at.xirado.jdui.ContextBuilder
import at.xirado.jdui.JDUIListener
import at.xirado.jdui.config.JDUIConfig
import at.xirado.jdui.config.PersistenceConfig
import at.xirado.jdui.config.Secret
import at.xirado.jdui.config.ViewData
import at.xirado.jdui.config.jdui
import at.xirado.jdui.event.DefaultEventAdapter
import at.xirado.jdui.utils.hexStringToByteArray
import com.github.benmanes.caffeine.cache.Scheduler
import de.mkammerer.snowflakeid.SnowflakeIdGenerator
import de.sparkarmy.coroutines.virtualDispatcher
import de.sparkarmy.data.cache.PersistentMessageCacheView
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.hooks.EventListener
import org.koin.core.annotation.Single

private val log = KotlinLogging.logger{}

@Single
fun provideJDUI(cacheView: PersistentMessageCacheView, config: JduiConfig): EventListener {
    val password = config.password
    val salt = hexStringToByteArray(config.salt)
    val config = jdui {
        this.persistenceConfig = createPersistenceConfig(cacheView)
        this.secret = Secret(password,salt)
    }
    log.info { config }
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