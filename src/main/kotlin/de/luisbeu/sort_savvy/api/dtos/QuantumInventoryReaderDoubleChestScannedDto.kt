package de.luisbeu.sort_savvy.api.dtos

import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedInterface
import de.luisbeu.sort_savvy.api.dtos.responses.InventoryType

// Extend the interface to be returnable and add additional inventory specific data
open class QuantumInventoryReaderDoubleChestScannedDto (
    val scannedChestCoordinates: CoordinatesDto,
    val counterPartChestCoordinates: CoordinatesDto,
    val scannedContents: List<ItemDto>,
): QuantumInventoryReaderScannedInterface {
    override val inventoryType: InventoryType = InventoryType.DOUBLE_CHEST
}
