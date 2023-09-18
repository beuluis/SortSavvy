package de.luisbeu.sort_savvy

import de.luisbeu.sort_savvy.blocks.QuantumInventoryReader
import de.luisbeu.sort_savvy.config.ConfigManager
import de.luisbeu.sort_savvy.entities.QuantumInventoryReaderEntity
import de.luisbeu.sort_savvy.events.ServerStartedHandler
import de.luisbeu.sort_savvy.network.IdSetterScreenHandler
import de.luisbeu.sort_savvy.events.ServerStoppingHandler
import de.luisbeu.sort_savvy.network.IdSetterScreenSavedNetworkHandler
import de.luisbeu.sort_savvy.persistent.PersistentManager
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
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SortSavvy : ModInitializer {
    // Initialize the logger here to have access everywhere
    val LOGGER: Logger = LogManager.getLogger(Constants.MOD_NAME)

    // A companion object to store all resources only available on certain execution times. So watch out when you access them
    object LifecycleGlobals {
        // Minecraft server global. Expose it for webserver operations
        private var minecraftServer: MinecraftServer? = null

        // Add a getter that throws if we try to access it at the wrong time
        fun getMinecraftServer(): MinecraftServer = minecraftServer ?: run {
            val msg = "Tried to access the minecraft server global outside its lifecycle"
            LOGGER.error(msg)
            throw IllegalAccessError(msg)
        }

        fun setMinecraftServer(server: MinecraftServer) {
            minecraftServer = server

            // Also set all down stream dependencies new
            configManager = ConfigManager()
            persistentManager = PersistentManager()
        }

        // Config global
        private var configManager: ConfigManager? = null

        // Add a getter that throws if we try to access it at the wrong time
        fun getConfigManager(): ConfigManager = configManager ?: run {
            val msg = "Tried to access the config manager global outside its lifecycle"
            LOGGER.error(msg)
            throw IllegalAccessError(msg)
        }

        // Config global
        private var persistentManager: PersistentManager? = null

        // Add a getter that throws if we try to access it at the wrong time
        fun getPersistentManager(): PersistentManager = persistentManager ?: run {
            val msg = "Tried to access the persistent manager global outside its lifecycle"
            LOGGER.error(msg)
            throw IllegalAccessError(msg)
        }

        // Just to be sure set this to null so that our error throws if we try to access those at the wrong time
        fun destroy() {
            persistentManager?.saveData()

            minecraftServer = null
            configManager = null
            persistentManager = null
        }
    }

    // Collection of all needed hardcoded stuff
    object Constants {
        const val MOD_ID = "sort_savvy"
        const val MOD_NAME = "SortSavvy"

        val itemGroupId = Identifier(MOD_ID, "item_group")
        val quantumInventoryReaderId = Identifier(MOD_ID, "quantum_inventory_reader")
        val quantumInventoryReaderEntityId = Identifier(MOD_ID, "quantum_inventory_reader_entity")
        val idSetterScreenHandlerId = Identifier(MOD_ID, "id_setter_screen_handler")
        val idSetterScreenSavedNetworkHandlerId = Identifier(MOD_ID, "id_setter_screen_saved_network_handler")
    }

    // Blocks and Block Entities
    private val quantumInventoryReaderBlock = Registry.register(
        Registry.BLOCK, Constants.quantumInventoryReaderId, QuantumInventoryReader()
    )

    val quantumInventoryReaderBlockEntityType: BlockEntityType<QuantumInventoryReaderEntity> = Registry.register(
        Registry.BLOCK_ENTITY_TYPE,
        Constants.quantumInventoryReaderEntityId,
        FabricBlockEntityTypeBuilder.create(::QuantumInventoryReaderEntity, quantumInventoryReaderBlock).build()
    )

    // Screen Handlers
    var idSetterScreenHandlerType: ScreenHandlerType<IdSetterScreenHandler> =
        ExtendedScreenHandlerType(::IdSetterScreenHandler)

    // Initialization
    override fun onInitialize() {
        // Register a handler for the server starting event
        ServerLifecycleEvents.SERVER_STARTED.register(ServerStartedHandler())

        // Register a handler for the server stopping event
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerStoppingHandler())

        // Networking
        ServerPlayNetworking.registerGlobalReceiver(
            Constants.idSetterScreenSavedNetworkHandlerId, IdSetterScreenSavedNetworkHandler()
        )

        // Item Group
        val itemGroup =
            FabricItemGroupBuilder.create(Constants.itemGroupId).icon { ItemStack(quantumInventoryReaderBlock) }
                .build()

        // Block Items
        Registry.register(
            Registry.ITEM,
            Constants.quantumInventoryReaderId,
            BlockItem(quantumInventoryReaderBlock, Item.Settings().group(itemGroup))
        )

        // Screen Handlers
        idSetterScreenHandlerType = Registry.register(
            Registry.SCREEN_HANDLER,
            Constants.idSetterScreenHandlerId,
            idSetterScreenHandlerType
        )
    }

}
