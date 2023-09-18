package de.luisbeu.sort_savvy.api.plugins

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.config.SortSavvyConfigModel
import io.ktor.server.application.*
import io.ktor.server.auth.*

// Definitions for the auth plugin
fun Application.configureAuthentication() {
    install(Authentication) {
        // Define a bearer token authentication provider
        bearer {
            authenticate { tokenCredential ->
                // Compare with the generated token in the config
                if (tokenCredential.token == SortSavvy.LifecycleGlobals.getConfigManager().config.webserverBearerToken) {
                    // If the token matches we assign a dummy user id principal
                    // TODO: find better way or understand it
                    UserIdPrincipal("steve")
                } else {
                    // TODO: throw error and map it to error format
                    null
                }
            }
        }
    }
}