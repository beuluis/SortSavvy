package de.luisbeu.sort_savvy.util

import net.minecraft.util.Identifier


// Collection of all needed hardcoded stuff
object SortSavvyConstants {
    const val MOD_ID = "sort_savvy"
    const val MOD_NAME = "SortSavvy"

    val itemGroupId = Identifier(MOD_ID, "item_group")
    val quantumInventoryReaderId = Identifier(MOD_ID, "quantum_inventory_reader")
    val quantumInventoryReaderEntityId = Identifier(MOD_ID, "quantum_inventory_reader_entity")
    val quantumInventoryReaderScreenHandlerId = Identifier(MOD_ID, "quantum_inventory_reader_screen_handler")
    val quantumInventoryReaderSavedNetworkHandlerId = Identifier(MOD_ID, "quantum_inventory_reader_saved_network_handler")
}
