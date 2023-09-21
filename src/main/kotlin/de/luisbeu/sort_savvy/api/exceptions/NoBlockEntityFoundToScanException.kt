package de.luisbeu.sort_savvy.api.exceptions

import de.luisbeu.sort_savvy.api.dtos.CoordinatesDto

// Exception when no inventory could be found at the position to scan. Message can be overridden. If null the default message will be taken in the handler
// Coordinates are provided as additionally error context
open class NoBlockEntityFoundToScanException(val scannedCoordinates: CoordinatesDto, message: String? = null) : RuntimeException(message)