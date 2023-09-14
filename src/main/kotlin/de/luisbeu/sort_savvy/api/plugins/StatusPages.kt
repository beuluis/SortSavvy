package de.luisbeu.sort_savvy.api.plugins

import de.luisbeu.sort_savvy.api.exceptions.ExceptionHandler
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*

// Definitions for the status page plugin. Call the exception handler to take over logic
fun Application.configureExceptions() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // Call a top level error handler to map business errors to http errors
            ExceptionHandler.handle(call, cause)
        }
    }
}