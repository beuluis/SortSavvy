package de.luisbeu.sort_savvy.api.dtos.responses

import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderContext
import de.luisbeu.sort_savvy.api.dtos.responses.interfaces.QuantumInventoryReaderResponseBase

class QuantumInventoryReaderErrorResponse (
    override val quantumInventoryReaderContext: QuantumInventoryReaderContext,
    error: String,
    message: String,
): QuantumInventoryReaderResponseBase, ExceptionResponse(error, message)