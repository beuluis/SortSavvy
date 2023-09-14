package de.luisbeu.sort_savvy.api.plugins

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

// Definitions for the serialization plugin
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        // Use GSON to handle JSON
        gson()
    }
}