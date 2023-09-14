package de.luisbeu.sort_savvy

import de.luisbeu.sort_savvy.config.SortSavvyConfig
import de.luisbeu.sort_savvy.blocks.QuantumInventoryReader
import de.luisbeu.sort_savvy.entities.QuantumInventoryReaderEntity
import de.luisbeu.sort_savvy.network.QuantumInventoryReaderScreenHandler
import de.luisbeu.sort_savvy.events.ServerStartingHandler
import de.luisbeu.sort_savvy.events.ServerStoppingHandler
import de.luisbeu.sort_savvy.network.QuantumInventoryReaderSavedNetworkHandler
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

    // Blocks and Block Entities
    private val quantumInventoryReaderBlock = Registry.register(
        Registry.BLOCK, SortSavvyConstants.quantumInventoryReaderId, QuantumInventoryReader()
    )

    val quantumInventoryReaderBlockEntityType: BlockEntityType<QuantumInventoryReaderEntity> = Registry.register(
        Registry.BLOCK_ENTITY_TYPE,
        SortSavvyConstants.quantumInventoryReaderEntityId,
        FabricBlockEntityTypeBuilder.create(::QuantumInventoryReaderEntity, quantumInventoryReaderBlock).build()
    )

    // Screen Handlers
    var quantumInventoryReaderScreenHandlerType: ScreenHandlerType<QuantumInventoryReaderScreenHandler> =
        ExtendedScreenHandlerType(::QuantumInventoryReaderScreenHandler)

    // Initialization
    override fun onInitialize() {
        // Register a handler for the server starting event
        ServerLifecycleEvents.SERVER_STARTING.register(ServerStartingHandler())

        // Register a handler for the server stopping event
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerStoppingHandler())


        // Networking
        ServerPlayNetworking.registerGlobalReceiver(
            SortSavvyConstants.quantumInventoryReaderSavedNetworkHandlerId, QuantumInventoryReaderSavedNetworkHandler()
        )

        // Item Group
        val itemGroup =
            FabricItemGroupBuilder.create(SortSavvyConstants.itemGroupId).icon { ItemStack(quantumInventoryReaderBlock) }
                .build()

        // Block Items
        Registry.register(
            Registry.ITEM,
            SortSavvyConstants.quantumInventoryReaderId,
            BlockItem(quantumInventoryReaderBlock, Item.Settings().group(itemGroup))
        )

        // Screen Handlers
        quantumInventoryReaderScreenHandlerType = Registry.register(
            Registry.SCREEN_HANDLER,
            SortSavvyConstants.quantumInventoryReaderScreenHandlerId,
            quantumInventoryReaderScreenHandlerType
        )
    }

}
