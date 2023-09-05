package de.luisbeu.sort_savvy.events

import com.simibubi.create.AllBlocks
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity
import com.simibubi.create.content.logistics.funnel.FunnelFilterSlotPositioning
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour
import de.luisbeu.sort_savvy.web.initWebServer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


class ServerStartingHandler : ServerLifecycleEvents.ServerStarting {

    fun applyFilterToBlock(world: ServerWorld, pos: BlockPos?) {
        val blockState = world.getBlockState(pos)
        val filterBlock = blockState.block
        val smartBlockEntity = world.getBlockEntity(pos)
        if (smartBlockEntity is SmartBlockEntity) {
            val filterStack = ItemStack(Items.FLINT) // Example filter item
            val filteringBehaviour = FilteringBehaviour(smartBlockEntity, FunnelFilterSlotPositioning())
            filteringBehaviour.filter = filterStack;
            smartBlockEntity.addBehaviours(listOf(filteringBehaviour)) // Set the filter item in the filter block
            smartBlockEntity.markDirty()

            // Update the filters block's state to refresh its functionality
            world.updateNeighbors(pos, filterBlock)
        }
    }
    override fun onServerStarting(server: MinecraftServer) {
        val w = server.getWorld(World.OVERWORLD)
        if (w != null) {
            applyFilterToBlock(w, BlockPos(0, 64, 0))
        }
        // Initialize Web Server
        initWebServer(server)
    }
}