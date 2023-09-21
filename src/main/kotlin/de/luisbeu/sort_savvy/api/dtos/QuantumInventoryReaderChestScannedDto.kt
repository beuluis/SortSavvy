package de.luisbeu.sort_savvy.api.dtos

import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedBase
import de.luisbeu.sort_savvy.api.dtos.responses.InventoryType

open class QuantumInventoryReaderChestScanned (
    val scannedChestCoordinates: Coordinates,
    val scannedContents: List<ScannedContent>,
): QuantumInventoryReaderScannedBase {
    override val inventoryType: InventoryType = InventoryType.CHEST
}
