package de.luisbeu.sort_savvy.api.dtos.responses.interfaces

import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderContext

interface QuantumInventoryReaderResponseBase {
    val quantumInventoryReaderContext: QuantumInventoryReaderContext
}