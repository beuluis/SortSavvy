package de.luisbeu.sort_savvy.api.dtos.interfaces

import de.luisbeu.sort_savvy.api.dtos.responses.InventoryType

// Comment interface for data classes used to describe the scan result of quantum interface readers
interface QuantumInventoryReaderScannedInterface {
    val inventoryType: InventoryType
}
