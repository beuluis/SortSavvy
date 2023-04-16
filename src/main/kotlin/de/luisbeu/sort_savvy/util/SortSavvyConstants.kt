package de.luisbeu.sort_savvy.util

import net.minecraft.util.Identifier


// Collection of all needed hardcoded stuff
object SortSavvyConstants {
    const val MOD_ID = "sort_savvy"
    const val MOD_NAME = "SortSavvy"

    val itemGroupId = Identifier(MOD_ID, "item_group")
    val quantumChestReaderId = Identifier(MOD_ID, "quantum_chest_reader")
    val quantumChestReaderEntityId = Identifier(MOD_ID, "quantum_chest_reader_entity")
    val quantumChestReaderScreenHandlerId = Identifier(MOD_ID, "quantum_chest_reader_screen_handler")
    val quantumChestReaderSavedNetworkHandlerId = Identifier(MOD_ID, "quantum_chest_reader_saved_network_handler")
}
