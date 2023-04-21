package de.luisbeu.sort_savvy.web

import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.util.ServerState
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.block.ChestBlock
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.DoubleInventory
import net.minecraft.inventory.Inventory
import net.minecraft.nbt.NbtElement
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

// Data classes to be used in JSON response and requests
data class QuantumChestReaderScannedContent(
    val id: String,
    val amount: Int,
    val category: String?,
    val durability: Int?,
    val damage: Int?,
    val enchantments: List<NbtElement>?
)


data class Coordinates(
    val x: Int,
    val y: Int,
    val z: Int,
)

data class QuantumChestReaderResponse(
    val quantumChestReaderId: String,
    val quantumChestReaderCoordinates: Coordinates,
    val scannedContent: List<QuantumChestReaderScannedContent>
)

data class ErrorResponse(
    val error: String, val message: String, val context: Any? = null
)

// Iterate over the inventory and return all info's we get from ItemStack
private fun getInventoryContents(inventory: Inventory): List<QuantumChestReaderScannedContent> {
    return generateSequence(0) { it + 1 }
        .take(inventory.size())
        .map { inventory.getStack(it) }
        .filterNot { it.isEmpty }
        .map { stack ->
            // Get ID
            val itemId = Registry.ITEM.getId(stack.item).toString()
            // Get count
            val count = stack.count
            // Get category
            val category = stack.item.group?.name
            // Check if it can take damage and return the durability. If not return null will result in undefined in the JSON object
            val durability = if (stack.isDamageable) stack.maxDamage - stack.damage else null
            // Check if it can take damage and return it. If not return null will result in undefined in the JSON object
            val damage = if (stack.isDamageable) stack.damage else null
            // Check if it has enchantments and return them. If not return null will result in undefined in the JSON object
            val enchantments = if (stack.hasEnchantments()) stack.enchantments.toList() else null
            // Construct the data class
            QuantumChestReaderScannedContent(itemId, count, category, durability, damage, enchantments)
        }
        .toList()
}

private fun getInventoryEntity(server: MinecraftServer, x: Int, y: Int, z: Int): Inventory? {
    // Check the block pos
    val potentialInventoryPos = BlockPos(x, y , z)
    // Go over the chunk to also handle unloaded chunks
    val chunkPos = ChunkPos(potentialInventoryPos)
    val chunk = server.getWorld(World.OVERWORLD)?.getChunk(chunkPos.x, chunkPos.z)

    // Check if we have a chunk. TBH I don't know when this could happen but the types tell me it could
    if (chunk != null) {
        // Get our potential chest block entity
        val blockEntity = chunk.getBlockEntity(potentialInventoryPos)

        // Check if we have a entity with an inventory
        if (blockEntity is ChestBlockEntity) {
            // Handle double chests by determine the other chest half
            val facing = ChestBlock.getFacing(blockEntity.cachedState)
            val potentialDoublePos = potentialInventoryPos.offset(facing)
            // Get it again from the chunk for unloaded chunks
            val potentialDoubleBlockEntity = chunk.getBlockEntity(potentialDoublePos)

            // Check if we have a inventory again and if we are facing correctly
            if (potentialDoubleBlockEntity is ChestBlockEntity && ChestBlock.getFacing(potentialDoubleBlockEntity.cachedState) == facing.opposite) {
                return DoubleInventory(blockEntity, potentialDoubleBlockEntity)
            }
        }

        // Handle single chests
        if (blockEntity is Inventory) {
            return blockEntity
        }
    }

    // Nothing found or checks failed for whatever return null
    return null
}

// TODO: handle pos better
@OptIn(DelicateCoroutinesApi::class)
fun initWebServer(port: Int, server: MinecraftServer) {
    // Use GlobalScope.launch to not block the main thread
    GlobalScope.launch {
        embeddedServer(Netty, port) {
            install(ContentNegotiation) {
                // Use GSON to handle JSON
                gson()
            }
            routing {
                get("/quantum-chest-reader/all") {
                    // TODO: reuse function from other endpoint
                    // Get the server state to access our saved nbt data
                    val serverState = ServerState.getServerState(server)
                    val quantumChestReaderData = serverState.quantumChestReaderData

                    // construct a empty list of the data class we want to return
                    val quantumChestReaderResponses = mutableListOf<QuantumChestReaderResponse>()

                    // Read and loop over our saved data
                    for ((quantumChestReaderId, coordinates) in quantumChestReaderData) {
                        val (x, y, z) = coordinates
                        // See if we have an inventory at the pos
                        val inventoryEntity = getInventoryEntity(server, x, y + 1, z)

                        if (inventoryEntity != null) {
                            // If we have one scan the content
                            val scannedContent = getInventoryContents(inventoryEntity)

                            // Add it to the return list
                            quantumChestReaderResponses.add(
                                QuantumChestReaderResponse(
                                    quantumChestReaderId, Coordinates(x, y, z), scannedContent
                                )
                            )
                        }
                    }

                    // Response with what ever we have
                    call.respond(quantumChestReaderResponses)
                }

                get("/quantum-chest-reader/{quantumChestReaderId}") {
                    // Get the param
                    val quantumChestReaderId = call.parameters["quantumChestReaderId"]

                    // Validate the param
                    if (quantumChestReaderId == null) {
                        // If validation fails return an error
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("id-parameter-not-provided", "ID parameter not provided"))
                    } else {
                        // Read the saved nbt data
                        val serverState = ServerState.getServerState(server)
                        // Try to get the data associated with our id
                        val quantumChestReaderData = serverState.quantumChestReaderData[quantumChestReaderId]
                        if (quantumChestReaderData == null) {
                            // Return error if no associated data is found
                            call.respond(
                                HttpStatusCode.NotFound, ErrorResponse(
                                    "id-no-found", "QuantumChestReader with ID $quantumChestReaderId not found"
                                )
                            )
                        } else {
                            // See comments from above. This should be reused, but works for now and i want to do testing
                            val (x, y, z) = quantumChestReaderData
                            val inventoryEntity = getInventoryEntity(server, x, y + 1, z)

                            if (inventoryEntity != null) {
                                val scannedContent = getInventoryContents(inventoryEntity)
                                call.respond(
                                    QuantumChestReaderResponse(
                                        quantumChestReaderId, Coordinates(x, y, z), scannedContent
                                    )
                                )
                            } else {
                                // Return error if we don't find a inventory entity above the provided position
                                SortSavvy.LOGGER.info("No inventory found at x=$x y=${y + 1} z=$z")

                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponse(
                                        "no-inventory-found", "No inventory found at x=$x y=${y + 1} z=$z", Coordinates(x, y, z)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }.start(wait = true)
    }
}
