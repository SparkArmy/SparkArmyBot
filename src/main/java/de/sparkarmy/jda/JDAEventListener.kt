package de.sparkarmy.jda

import de.sparkarmy.misc.toEnumSet
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.koin.core.component.KoinComponent
import java.util.*
import kotlin.collections.flatMap
import kotlin.collections.minus
import kotlin.jvm.java

interface JDAEventListener : CoroutineEventListener, KoinComponent {
    val intents: EnumSet<GatewayIntent>
        get() = EnumSet.noneOf(GatewayIntent::class.java)
    val cacheFlags: EnumSet<CacheFlag>
        get() = EnumSet.noneOf(CacheFlag::class.java)
}

fun Collection<JDAEventListener>.allIntents() = flatMap { it.intents }.toEnumSet()

fun Collection<JDAEventListener>.allCacheFlags() = flatMap { it.cacheFlags }.toEnumSet()

fun Collection<JDAEventListener>.disabledCacheFlags() = CacheFlag.entries.minus(allCacheFlags()).toEnumSet()