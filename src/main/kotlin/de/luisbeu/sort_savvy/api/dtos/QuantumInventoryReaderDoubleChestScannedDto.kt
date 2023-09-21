package de.luisbeu.sort_savvy.api.dtos

import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedBase
import de.luisbeu.sort_savvy.api.dtos.responses.InventoryType

open class QuantumInventoryReaderDoubleChestScanned (
    val scannedChestCoordinates: Coordinates,
    val counterPartChestCoordinates: Coordinates,
    val scannedContents: List<ScannedContent>,
): QuantumInventoryReaderScannedBase {
    override val inventoryType: InventoryType = InventoryType.DOUBLE_CHEST
}
