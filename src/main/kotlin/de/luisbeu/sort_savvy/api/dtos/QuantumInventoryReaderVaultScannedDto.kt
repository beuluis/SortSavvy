package de.luisbeu.sort_savvy.api.dtos

import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedInterface
import de.luisbeu.sort_savvy.api.dtos.responses.InventoryType

// Extend the interface to be returnable and add additional inventory specific data
open class QuantumInventoryReaderVaultScannedDto (
    val scannedChestCoordinates: CoordinatesDto,
    val vaultControllerCoordinates: CoordinatesDto,
    val scannedContents: Any, // TODO: replace when content is known
): QuantumInventoryReaderScannedInterface {
    override val inventoryType: InventoryType = InventoryType.VAULT
}
