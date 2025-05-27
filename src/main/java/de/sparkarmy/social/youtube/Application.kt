package de.sparkarmy.social.youtube

import io.ktor.serialization.kotlinx.xml.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.core.annotation.Single

@Single(createdAtStart = true)
class Application {

    init {
        startServer()
    }

    private fun startServer() {
        embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
            install(ContentNegotiation) {
                xml()
            }
            youTubePubSub()
        }.start()
    }
}