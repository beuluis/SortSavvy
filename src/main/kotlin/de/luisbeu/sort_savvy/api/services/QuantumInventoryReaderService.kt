package de.luisbeu.sort_savvy.api.services

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.api.dtos.Coordinates
import de.luisbeu.sort_savvy.api.dtos.responses.QuantumInventoryReaderResponse
import de.luisbeu.sort_savvy.api.exceptions.NoInventoryFoundToScan
import de.luisbeu.sort_savvy.api.exceptions.QuantumInventoryReaderNotFound

object QuantumInventoryReaderService {
    fun getInventoryContentByQuantumInventoryReaderId(quantumInventoryReaderId: String): QuantumInventoryReaderResponse {
        // Read the saved data
        val persistentManager = SortSavvy.LifecycleGlobals.getPersistentManager()

        // Try to get the data by id form the persisted data. Throw if not found
        val  positionalContext = persistentManager.getQuantumInventoryReaderData()[quantumInventoryReaderId] ?: throw QuantumInventoryReaderNotFound(quantumInventoryReaderId)

        // Get the entity based of out location information
        val (inventoryEntity, blockPositions) = InventoryService.getInventoryEntityFromScannerPos(
            positionalContext
        )

        // Throw if no inventory is found
        // TODO: maybe design a empty inventory data structure?
        if (inventoryEntity == null) {
            SortSavvy.LOGGER.info("No inventory found at x=${blockPositions.first.x} y=${blockPositions.first.y} z=${blockPositions.first.z}")
            throw NoInventoryFoundToScan(blockPositions.first)
        }

        // Retrieve the contents from the entity
        val scannedContent = InventoryService.getInventoryContents(inventoryEntity)

        // Assessable the return dto
        val (x, y, z) = positionalContext
        return QuantumInventoryReaderResponse(
            quantumInventoryReaderId,
            Coordinates(x, y, z),
            blockPositions.first,
            blockPositions.second,
            scannedContent
        )
    }

    fun getAllInventoryContentsFromQuantumInventoryReaders(): List<QuantumInventoryReaderResponse> {
        // Read the saved data
        val persistentManager = SortSavvy.LifecycleGlobals.getPersistentManager()

        // Get all data entries from the persisted data
        val  quantumInventoryReaderData = persistentManager.getQuantumInventoryReaderData()

        // Construct an empty list of the data class we want to return
        val quantumInventoryReaderResponses = mutableListOf<QuantumInventoryReaderResponse>()

        // TODO: handle error with no inv
        // Read and loop over our saved data
        for ((quantumInventoryReaderId) in quantumInventoryReaderData) {
            // Add it to the return list
            quantumInventoryReaderResponses.add(this.getInventoryContentByQuantumInventoryReaderId(quantumInventoryReaderId))
        }

        // Return empty or not
        return quantumInventoryReaderResponses
    }
}