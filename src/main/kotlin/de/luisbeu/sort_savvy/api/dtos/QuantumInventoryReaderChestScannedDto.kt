package de.luisbeu.sort_savvy.api.dtos

import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedInterface
import de.luisbeu.sort_savvy.api.dtos.responses.InventoryType

// Extend the interface to be returnable and add additional inventory specific data
open class QuantumInventoryReaderChestScannedDto (
    val scannedChestCoordinates: CoordinatesDto,
    val scannedContents: List<ScannedContentDto>,
): QuantumInventoryReaderScannedInterface {
    override val inventoryType: InventoryType = InventoryType.CHEST
}
