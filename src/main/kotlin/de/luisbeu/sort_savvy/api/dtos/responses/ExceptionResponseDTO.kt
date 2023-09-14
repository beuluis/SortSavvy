package de.luisbeu.sort_savvy.api.dtos.responses

data class ExceptionResponseDTO (
    val error: String, val message: String, val context: Any? = null
)
