package de.luisbeu.sort_savvy.api.dtos.responses

open class ExceptionResponse (
    val error: String, val message: String, val context: Any? = null
)
