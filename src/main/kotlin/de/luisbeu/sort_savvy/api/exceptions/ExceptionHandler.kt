package de.luisbeu.sort_savvy.api.exceptions

import de.luisbeu.sort_savvy.api.dtos.responses.ExceptionResponseDto
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
                    ExceptionResponseDto("id-parameter-not-provided", cause.message ?: "The id parameter was not provided")
                )
            }
            is QuantumInventoryReaderNotFoundException -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ExceptionResponseDto("id-no-found", cause.message ?: "The id parameter was not provided", cause.id)
                )
            }
            is NoBlockEntityFoundToScanException -> {
                call.respond(
                    HttpStatusCode.NotFound,
                    ExceptionResponseDto("no-inventory-found", cause.message ?: "No inventory found to scan", cause.scannedCoordinates)
                )
            }
            // Default to internal server error and hide the message to not leak data
            else -> {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ExceptionResponseDto("internal-server-error", "An unexpected internal server error occurred")
                )
            }
        }
    }
}