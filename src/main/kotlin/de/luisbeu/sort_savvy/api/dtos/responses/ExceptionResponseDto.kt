package de.luisbeu.sort_savvy.api.dtos.responses

// Base class for all exception like responses
open class ExceptionResponseDto (
    val error: String, val message: String, val context: Any? = null
)
