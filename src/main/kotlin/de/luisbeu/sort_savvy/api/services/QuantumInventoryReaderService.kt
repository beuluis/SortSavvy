package de.luisbeu.sort_savvy.api.services

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.api.dtos.Coordinates
import de.luisbeu.sort_savvy.api.dtos.responses.QuantumInventoryReaderResponse
import de.luisbeu.sort_savvy.api.exceptions.NoInventoryFoundToScan
import de.luisbeu.sort_savvy.api.exceptions.QuantumInventoryReaderNotFound
import de.luisbeu.sort_savvy.util.ServerState
import net.minecraft.server.MinecraftServer

object QuantumInventoryReaderService {
    fun getInventoryContentByQuantumInventoryReaderId(server: MinecraftServer, quantumInventoryReaderId: String): QuantumInventoryReaderResponse {
        // Read the saved nbt data
        val serverState = ServerState.getServerState(server)

        // Try to get the data by id form the server state. Throw if not found
        val  positionWithToScanDirection = serverState.quantumInventoryReaderData[quantumInventoryReaderId] ?: throw QuantumInventoryReaderNotFound(quantumInventoryReaderId)

        // Get the entity based of out location information
        val (inventoryEntity, blockPositions) = InventoryService.getInventoryEntityFromScannerPos(
            server,
            positionWithToScanDirection
        )

        // Throw if no inventory is found
        // TODO: maybe design a empty inventory data structure?
        if (inventoryEntity == null) {
            SortSavvy.LOGGER.info("No inventory found at x=${blockPositions.first.x} y=${blockPositions.first.y} z=${blockPositions.first.z}")
            throw NoInventoryFoundToScan(blockPositions.first)
        }

        // Retrieve the contents from the entity
        val scannedContent = InventoryService.getInventoryContents(inventoryEntity)

        // Asabmle the return dto
        val (x, y, z) = positionWithToScanDirection
        return QuantumInventoryReaderResponse(
            quantumInventoryReaderId,
            Coordinates(x, y, z),
            blockPositions.first,
            blockPositions.second,
            scannedContent
        )
    }

    fun getAllInventoryContentsFromQuantumInventoryReaders(server: MinecraftServer): List<QuantumInventoryReaderResponse> {
        // Read the saved nbt data
        val serverState = ServerState.getServerState(server)

        // Get all data entries from the server state
        val  quantumInventoryReaderData = serverState.quantumInventoryReaderData

        // Construct an empty list of the data class we want to return
        val quantumInventoryReaderResponses = mutableListOf<QuantumInventoryReaderResponse>()

        // TODO: handle error with no inv
        // Read and loop over our saved data
        for ((quantumInventoryReaderId) in quantumInventoryReaderData) {
            // Add it to the return list
            quantumInventoryReaderResponses.add(this.getInventoryContentByQuantumInventoryReaderId(server, quantumInventoryReaderId))
        }

        // Return empty or not
        return quantumInventoryReaderResponses
    }
}