package de.luisbeu.sort_savvy.api.dtos

import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedInterface
import de.luisbeu.sort_savvy.api.dtos.responses.InventoryType

// Extend the interface to be returnable and add additional inventory specific data
open class QuantumInventoryReaderItemVaultScannedDto (
    val scannedChestCoordinates: CoordinatesDto,
    val itemVaultControllerCoordinates: CoordinatesDto,
    val scannedContents: ItemDto,
): QuantumInventoryReaderScannedInterface {
    override val inventoryType: InventoryType = InventoryType.VAULT
}
