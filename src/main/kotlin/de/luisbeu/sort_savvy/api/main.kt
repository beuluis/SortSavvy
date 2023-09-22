package de.luisbeu.sort_savvy.api

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.api.plugins.configureAuthentication
import de.luisbeu.sort_savvy.api.plugins.configureExceptions
import de.luisbeu.sort_savvy.api.plugins.configureRouting
import de.luisbeu.sort_savvy.api.plugins.configureSerialization
import de.luisbeu.sort_savvy.config.ConfigManager
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object WebServer {
    private var applicationEngine: ApplicationEngine? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {
        // Use GlobalScope.launch to not block the main thread
        GlobalScope.launch {
            applicationEngine = embeddedServer(Netty, port = ConfigManager.getConfig().webserverPort, module = { module() })
            applicationEngine?.start(wait = true)
            SortSavvy.logger.info("Web Server started")
        }
    }

    fun stop() {
        applicationEngine?.stop(1000, 1000)
        SortSavvy.logger.info("Web Server stopped")
    }
}

// Assemble our server
fun Application.module() {
    configureAuthentication()
    configureExceptions()
    configureSerialization()
    configureRouting()
}