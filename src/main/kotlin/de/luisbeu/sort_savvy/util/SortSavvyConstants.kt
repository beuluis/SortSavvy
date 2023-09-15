package de.luisbeu.sort_savvy.util

import net.minecraft.util.Identifier


// Collection of all needed hardcoded stuff
object SortSavvyConstants {
    const val MOD_ID = "sort_savvy"
    const val MOD_NAME = "SortSavvy"

    val itemGroupId = Identifier(MOD_ID, "item_group")
    val quantumInventoryReaderId = Identifier(MOD_ID, "quantum_inventory_reader")
    val quantumInventoryReaderEntityId = Identifier(MOD_ID, "quantum_inventory_reader_entity")
    val idSetterScreenHandlerId = Identifier(MOD_ID, "id_setter_screen_handler")
    val idSetterScreenSavedNetworkHandlerId = Identifier(MOD_ID, "id_setter_screen_saved_network_handler")
}
