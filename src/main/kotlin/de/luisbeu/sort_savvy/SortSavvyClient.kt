package de.luisbeu.sort_savvy

import de.luisbeu.sort_savvy.screens.QuantumInventoryReaderScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreens

// Class for all client related registrations
@Environment(EnvType.CLIENT)
object SortSavvyClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Screens
        HandledScreens.register(SortSavvy.quantumInventoryReaderScreenHandlerType, ::QuantumInventoryReaderScreen);
    }
}
