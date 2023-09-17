package de.luisbeu.sort_savvy.api.plugins

import de.luisbeu.sort_savvy.api.exceptions.IdParameterNotProvidedException
import de.luisbeu.sort_savvy.api.services.QuantumInventoryReaderService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.minecraft.server.MinecraftServer

// Routing definitions. Call services to separate them from http handling
fun Application.configureRouting() {
    routing {
        authenticate {
            /*get("/testing") {
                        val blockPos = BlockPos(0, 64, 0)

                        // Go over the chunk to also handle unloaded chunks
                        val chunkPos = ChunkPos(blockPos)
                        val chunk = server.getWorld(World.OVERWORLD)?.getChunk(chunkPos.x, chunkPos.z)

                        // Check if we have a chunk
                        if (chunk != null) {
                            val blockEntity = chunk.getBlockEntity(blockPos)
                            if (blockEntity is SmartBlockEntity) {
                                val filterStack = AllItems.FILTER.get().defaultStack

                                val nbt = filterStack.getOrCreateNbt()
                                nbt.putBoolean("RespectNBT", true)
                                nbt.putBoolean("Blacklist", true)
                                filterStack.writeNbt(nbt)

                                val newInv = ItemStackHandler(18)
                                val transaction = Transaction.openOuter()
                                newInv.insert(ItemVariant.of(ItemStack(Items.FLINT)), 1, transaction) // stack for nbt
                                transaction.commit() // propper handling https://fabricmc.net/wiki/tutorial:transfer-api_storage
                                filterStack.setSubNbt("Items", newInv.serializeNBT())

                                val behaviour = blockEntity.getBehaviour(FilteringBehaviour.TYPE)
                                behaviour.filter = filterStack
                            }
                            // chunk.world.updateNeighbors(blockPos, chunk.world.getBlockState(blockPos).block)
                        }
                        call.respond("run!")
                    }*/
            get("/quantum-inventory-reader/{quantumInventoryReaderId}") {
                // Get the param and throw if not provided
                val quantumInventoryReaderId = call.parameters["quantumInventoryReaderId"] ?: throw IdParameterNotProvidedException()
                call.respond(QuantumInventoryReaderService.getInventoryContentByQuantumInventoryReaderId(quantumInventoryReaderId))
            }
            get("/quantum-inventory-reader/all") {
                call.respond(QuantumInventoryReaderService.getAllInventoryContentsFromQuantumInventoryReaders())
            }
        }
    }
}