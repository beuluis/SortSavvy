package de.luisbeu.sort_savvy.api.dtos.responses

import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderContext
import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedBase
import de.luisbeu.sort_savvy.api.dtos.responses.interfaces.QuantumInventoryReaderResponseBase

// TODO: move
enum class InventoryType {
    CHEST,
    DOUBLE_CHEST,
    VAULT
}

class QuantumInventoryReaderResponse (
    override val quantumInventoryReaderContext: QuantumInventoryReaderContext,
    val result: QuantumInventoryReaderScannedBase,
): QuantumInventoryReaderResponseBase