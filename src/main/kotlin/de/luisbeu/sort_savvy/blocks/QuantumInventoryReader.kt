package de.luisbeu.sort_savvy.blocks

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.entities.QuantumInventoryReaderEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class QuantumInventoryReader : BlockWithEntity(FabricBlockSettings.of(Material.WOOD).strength(0.4f)), BlockEntityProvider {

    // Create a new entity at the given position
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return QuantumInventoryReaderEntity(pos, state)
    }

    // Set the render type to model
    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }

    // Handle the block usage
    override fun onUse(
        state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult
    ): ActionResult {
        // Only do action on the server world
        if (!world.isClient) {
            // If player is sneaking change the direction
            if(player.isSneaking) {
                // Get the entity at that position
                val quantumInventoryReaderEntity = this.getBlockEntity(world, pos)?: run {
                    SortSavvy.logger.warn("No quantum inventory reader entity found at $pos could not update")
                    return ActionResult.FAIL
                }
                val serverPlayer = player as? ServerPlayerEntity

                // If we have a quantum inventory reader entity at this pos we update its position
                quantumInventoryReaderEntity.setToScanDirection(hit.side, serverPlayer)
            } else {
                // If player is not sneaking open the screen
                val screenHandlerFactory = state.createScreenHandlerFactory(world, pos)?: run {
                    SortSavvy.logger.warn("No screen handler could be created")
                    return ActionResult.FAIL
                }

                player.openHandledScreen(screenHandlerFactory)
            }

        }

        return ActionResult.SUCCESS
    }

    // Get the block entity at the given position
    private fun getBlockEntity (world: World, pos: BlockPos): QuantumInventoryReaderEntity? {
        val blockEntity = world.getBlockEntity(pos)

        return blockEntity as? QuantumInventoryReaderEntity
    }

    // This gets called when the block gets changed
    override fun onStateReplaced(
        state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean
    ) {
        // Check if block got broken
        if (state.block != newState.block) {
            // Get the entity at that position
            val quantumInventoryReaderEntity = this.getBlockEntity(world, pos)?: run {
                SortSavvy.logger.warn("No quantum inventory reader entity found at $pos could not delete")
                return
            }

            val player = world.getClosestPlayer(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), -1.0, false) as? ServerPlayerEntity

            // If we have a quantum inventory reader entity at this pos we delete it
            // TODO: dedicated delete function?
            quantumInventoryReaderEntity.setId("", player)

            super.onStateReplaced(state, world, pos, newState, moved)
        }
    }
}