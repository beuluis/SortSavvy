package de.luisbeu.sort_savvy.blocks

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.entities.QuantumChestReaderEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class QuantumChestReader : BlockWithEntity(FabricBlockSettings.of(Material.WOOD).strength(0.4f)), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        // Spawn an entity on the same position as the placed block
        return QuantumChestReaderEntity(pos, state)
    }

    override fun getRenderType(state: BlockState): BlockRenderType {
        // The default is invisible so we set it to model
        return BlockRenderType.MODEL
    }

    override fun onUse(
        state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult
    ): ActionResult {
        // Only open the associated screen handler on the entity when we are on a client
        if (!world.isClient) {
            val screenHandlerFactory = state.createScreenHandlerFactory(world, pos)
            if (screenHandlerFactory != null) {
                try {
                    player.openHandledScreen(screenHandlerFactory)
                } catch (e: Exception) {
                    SortSavvy.LOGGER.error("Error opening quantum chest reader screen handler ${e.message}")
                }
            }
        }

        return ActionResult.SUCCESS
    }

    // This gets called when the block gets changed
    override fun onStateReplaced(
        state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean
    ) {
        // Check if block got broken
        if (state.block != newState.block) {
            // Get the entity at that position
            val blockEntity = world.getBlockEntity(pos)
            val player = world.getClosestPlayer(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), -1.0, false) as? ServerPlayerEntity

            // If we have a quantum chest reader entity at this pos we delete it
            if (blockEntity is QuantumChestReaderEntity) {
                blockEntity.setQuantumChestReaderId("", player)
            } else {
                if (blockEntity == null) {
                    SortSavvy.LOGGER.error("No quantum chest reader entity found at $pos could not delete")

                    return
                }

                SortSavvy.LOGGER.error("No quantum chest reader entity found at $pos found $blockEntity could not delete")
            }

            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }
}