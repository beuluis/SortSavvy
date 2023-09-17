package de.luisbeu.sort_savvy

import de.luisbeu.sort_savvy.blocks.QuantumInventoryReader
import de.luisbeu.sort_savvy.config.SortSavvyConfig
import de.luisbeu.sort_savvy.config.SortSavvyConfigModel
import de.luisbeu.sort_savvy.entities.QuantumInventoryReaderEntity
import de.luisbeu.sort_savvy.events.ServerStartedHandler
import de.luisbeu.sort_savvy.network.IdSetterScreenHandler
import de.luisbeu.sort_savvy.events.ServerStoppingHandler
import de.luisbeu.sort_savvy.network.IdSetterScreenSavedNetworkHandler
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
import net.minecraft.server.MinecraftServer
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object SortSavvy : ModInitializer {
    // Initialize the logger here to have access everywhere
    val LOGGER: Logger = LogManager.getLogger(SortSavvyConstants.MOD_NAME)

    // A companion object to store all resources only available on certain execution times. So watch out when you access them
    object LifecycleGlobals {
        // Minecraft server global. Expose it for webserver operations
        private var MINECRAFT_SERVER: MinecraftServer? = null
        fun getMinecraftServer(): MinecraftServer = MINECRAFT_SERVER ?: run {
            val msg = "Tried to access the minecraft server global outside its lifecycle"
            LOGGER.error(msg)
            throw IllegalAccessError(msg)
        }
        fun setMinecraftServer(server: MinecraftServer) {
            MINECRAFT_SERVER = server
            CONFIG = SortSavvyConfig().getConfig()
        }

        // Config global
        private var CONFIG: SortSavvyConfigModel? = null
        fun getConfig(): SortSavvyConfigModel = CONFIG ?: run {
            val msg = "Tried to access the config global outside its lifecycle"
            LOGGER.error(msg)
            throw IllegalAccessError(msg)
        }

        // Just to be sure set this to null so that our error throws if we try to access those at the wrong time
        fun destroy() {
            MINECRAFT_SERVER = null
            CONFIG = null
        }
    }

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
            SortSavvyConstants.idSetterScreenSavedNetworkHandlerId, IdSetterScreenSavedNetworkHandler()
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
        idSetterScreenHandlerType = Registry.register(
            Registry.SCREEN_HANDLER,
            SortSavvyConstants.idSetterScreenHandlerId,
            idSetterScreenHandlerType
        )
    }

}
