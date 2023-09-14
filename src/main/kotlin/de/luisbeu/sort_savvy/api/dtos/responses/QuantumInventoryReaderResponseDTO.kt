package de.luisbeu.sort_savvy.api.dtos.responses

import de.luisbeu.sort_savvy.api.dtos.Coordinates
import de.luisbeu.sort_savvy.api.dtos.QuantumInventoryReaderScannedContent

data class QuantumInventoryReaderResponse(
    val quantumInventoryReaderId: String,
    val quantumInventoryReaderCoordinates: Coordinates,
    val primaryInventoryCoordinates: Coordinates?,
    val secondaryInventoryCoordinates: Coordinates?,
    val scannedContent: List<QuantumInventoryReaderScannedContent>
)