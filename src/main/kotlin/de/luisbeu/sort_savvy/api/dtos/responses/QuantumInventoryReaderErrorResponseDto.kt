package de.luisbeu.sort_savvy.api.dtos.responses

import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderContextDto
import de.luisbeu.sort_savvy.api.dtos.responses.interfaces.QuantumInventoryReaderResponseInterface

// An "error" like response hydrated with additional context
class QuantumInventoryReaderErrorResponseDto (
    override val quantumInventoryReaderContext: QuantumInventoryReaderContextDto,
    error: String,
    message: String,
): QuantumInventoryReaderResponseInterface, ExceptionResponseDto(error, message)