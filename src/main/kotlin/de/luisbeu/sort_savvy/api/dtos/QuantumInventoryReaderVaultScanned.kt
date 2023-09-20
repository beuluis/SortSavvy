package de.luisbeu.sort_savvy.api.dtos

import de.luisbeu.sort_savvy.api.dtos.interfaces.QuantumInventoryReaderScannedBase
import de.luisbeu.sort_savvy.api.dtos.responses.InventoryType

open class QuantumInventoryReaderVaultScanned (
    val scannedChestCoordinates: Coordinates,
    val vaultControllerCoordinates: Coordinates,
    val scannedContents: Any, // TODO: replace when content is known
): QuantumInventoryReaderScannedBase {
    override val inventoryType: InventoryType = InventoryType.VAULT
}
