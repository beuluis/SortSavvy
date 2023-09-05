package de.luisbeu.sort_savvy

import SortSavvyConfig
import de.luisbeu.sort_savvy.blocks.QuantumChestReader
import de.luisbeu.sort_savvy.entities.QuantumChestReaderEntity
import de.luisbeu.sort_savvy.network.QuantumChestReaderScreenHandler
import de.luisbeu.sort_savvy.events.ServerStartingHandler
import de.luisbeu.sort_savvy.network.QuantumChestReaderSavedNetworkHandler
import de.luisbeu.sort_savvy.util.SortSavvyConstants
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SortSavvy : ModInitializer {
    // Initialize the logger here to have access everywhere
    val LOGGER: Logger = LogManager.getLogger(SortSavvyConstants.MOD_NAME)
    val CONFIG = SortSavvyConfig().getConfig()

    // Blocks and Block Entities
    private val quantumChestReaderBlock = Registry.register(
        Registry.BLOCK, SortSavvyConstants.quantumChestReaderId, QuantumChestReader()
    )

    val quantumChestReaderBlockEntityType: BlockEntityType<QuantumChestReaderEntity> = Registry.register(
        Registry.BLOCK_ENTITY_TYPE,
        SortSavvyConstants.quantumChestReaderEntityId,
        FabricBlockEntityTypeBuilder.create(::QuantumChestReaderEntity, quantumChestReaderBlock).build()
    )

    // Screen Handlers
    var quantumChestReaderScreenHandlerType: ScreenHandlerType<QuantumChestReaderScreenHandler> =
        ExtendedScreenHandlerType(::QuantumChestReaderScreenHandler)

    // Initialization
    override fun onInitialize() {
        // Register a handler for the server starting event
        ServerLifecycleEvents.SERVER_STARTING.register(ServerStartingHandler())


        // Networking
        ServerPlayNetworking.registerGlobalReceiver(
            SortSavvyConstants.quantumChestReaderSavedNetworkHandlerId, QuantumChestReaderSavedNetworkHandler()
        )

        // Item Group
        val itemGroup =
            FabricItemGroupBuilder.create(SortSavvyConstants.itemGroupId).icon { ItemStack(quantumChestReaderBlock) }
                .build()

        // Block Items
        Registry.register(
            Registry.ITEM,
            SortSavvyConstants.quantumChestReaderId,
            BlockItem(quantumChestReaderBlock, Item.Settings().group(itemGroup))
        )

        // Screen Handlers
        quantumChestReaderScreenHandlerType = Registry.register(
            Registry.SCREEN_HANDLER,
            SortSavvyConstants.quantumChestReaderScreenHandlerId,
            quantumChestReaderScreenHandlerType
        )
    }

}
