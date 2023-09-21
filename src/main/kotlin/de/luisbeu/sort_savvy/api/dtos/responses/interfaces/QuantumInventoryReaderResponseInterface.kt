package de.luisbeu.sort_savvy.api.dtos.responses.interfaces

import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderContextDto

// Common interface to describe the quantum inventory reader response
interface QuantumInventoryReaderResponseInterface {
    val quantumInventoryReaderContext: QuantumInventoryReaderContextDto
}