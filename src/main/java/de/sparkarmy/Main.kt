package de.sparkarmy

import de.sparkarmy.jda.JDAService
import dev.reformator.stacktracedecoroutinator.jvm.DecoroutinatorJvmApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun main() {
    DecoroutinatorJvmApi.install()
    startKoin {
        modules(Module().module)
    }

    Application().initialize()
}

class Application : KoinComponent {
    private val jdaService: JDAService by inject()

    fun initialize() {
        jdaService.initialize()
    }
}