package de.sparkarmy

import dev.reformator.stacktracedecoroutinator.jvm.DecoroutinatorJvmApi
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun main() {
    DecoroutinatorJvmApi.install()
    startKoin {
        modules(Module().module)
    }
}