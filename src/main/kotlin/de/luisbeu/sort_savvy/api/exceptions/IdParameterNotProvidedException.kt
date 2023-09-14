package de.luisbeu.sort_savvy.api.exceptions

// Exception when the id parameter was not provided. Message can be overridden. If null the default message will be taken in the handler
open class IdParameterNotProvidedException(message: String? = null) : RuntimeException(message)