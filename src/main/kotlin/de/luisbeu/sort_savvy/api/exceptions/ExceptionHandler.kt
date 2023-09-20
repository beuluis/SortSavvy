package de.luisbeu.sort_savvy.api.exceptions

import de.luisbeu.sort_savvy.api.dtos.responses.ExceptionResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

// Top level exception handler to go through possible causes to map the business exceptions to http ones
object ExceptionHandler {
    suspend fun handle(
        call: ApplicationCall,
        cause: Throwable,
    ) {
        when (cause) {
            is IdParameterNotProvidedException -> {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ExceptionResponse("id-parameter-not-provided", cause.message ?: "The id parameter was not provided")
                )
            }
            is QuantumInventoryReaderNotFound -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ExceptionResponse("id-no-found", cause.message ?: "The id parameter was not provided", cause.id)
                )
            }
            is NoBlockEntityFoundToScan -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ExceptionResponse("no-inventory-found", cause.message ?: "No inventory found to scan", cause.scannedCoordinates)
                )
            }
            // Default to internal server error and hide the message to not leak data
            else -> {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ExceptionResponse("internal-server-error", "An unexpected internal server error occurred")
                )
            }
        }
    }
}