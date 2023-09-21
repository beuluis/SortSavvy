package de.luisbeu.sort_savvy.api.dtos.responses

import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderContextDto
import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedInterface
import de.luisbeu.sort_savvy.api.dtos.responses.interfaces.QuantumInventoryReaderResponseInterface

// TODO: move
enum class InventoryType {
    CHEST,
    DOUBLE_CHEST,
    VAULT
}

// Response of the quantum inventory reader scan result
class QuantumInventoryReaderResponseDto (
    override val quantumInventoryReaderContext: QuantumInventoryReaderContextDto,
    val result: QuantumInventoryReaderScannedInterface,
): QuantumInventoryReaderResponseInterface