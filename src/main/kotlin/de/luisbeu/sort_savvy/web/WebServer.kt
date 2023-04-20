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
import net.minecraft.enchantment.Enchantment
import net.minecraft.inventory.DoubleInventory
import net.minecraft.inventory.Inventory
import net.minecraft.nbt.NbtElement
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

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

private fun getInventoryContents(inventory: Inventory): List<QuantumChestReaderScannedContent> {
    return generateSequence(0) { it + 1 }
        .take(inventory.size())
        .map { inventory.getStack(it) }
        .filterNot { it.isEmpty }
        .map { stack ->
            val itemId = Registry.ITEM.getId(stack.item).toString()
            val count = stack.count
            val category = stack.item.group?.name ?: "Uncategorized"
            val durability = if (stack.isDamageable) stack.maxDamage - stack.damage else null
            val damage = if (stack.isDamageable) stack.damage else null
            val enchantments = if (stack.hasEnchantments()) stack.enchantments.toList() else null
            QuantumChestReaderScannedContent(itemId, count, category, durability, damage, enchantments)
        }
        .toList()
}

private fun getInventoryEntity(server: MinecraftServer, x: Int, y: Int, z: Int): Inventory? {
    val potentialInventoryPos = BlockPos(x, y + 1, z)
    val chunkPos = ChunkPos(potentialInventoryPos)
    val chunk = server.getWorld(World.OVERWORLD)?.getChunk(chunkPos.x, chunkPos.z)

    if (chunk != null) {
        val blockEntity = chunk.getBlockEntity(potentialInventoryPos)

        if (blockEntity is ChestBlockEntity) {
            val facing = ChestBlock.getFacing(blockEntity.cachedState)
            val doubleChestPos = potentialInventoryPos.offset(facing)
            val doubleChestBlockEntity = chunk.getBlockEntity(doubleChestPos)

            if (doubleChestBlockEntity is ChestBlockEntity && ChestBlock.getFacing(doubleChestBlockEntity.cachedState) == facing.opposite) {
                return DoubleInventory(blockEntity, doubleChestBlockEntity)
            }
        }

        if (blockEntity is Inventory) {
            return blockEntity
        }
    }

    return null
}



@OptIn(DelicateCoroutinesApi::class)
fun initWebServer(port: Int, server: MinecraftServer) {
    GlobalScope.launch {
        embeddedServer(Netty, port) {
            install(ContentNegotiation) {
                gson()
            }
            routing {
                get("/quantum-chest-reader/all") {
                    val serverState = ServerState.getServerState(server)
                    val quantumChestReaderData = serverState.quantumChestReaderData
                    val quantumChestReaderResponses = mutableListOf<QuantumChestReaderResponse>()

                    for ((quantumChestReaderId, coordinates) in quantumChestReaderData) {
                        val (x, y, z) = coordinates
                        val inventoryEntity = getInventoryEntity(server, x, y, z)

                        if (inventoryEntity != null) {
                            val scannedContent = getInventoryContents(inventoryEntity)

                            quantumChestReaderResponses.add(
                                QuantumChestReaderResponse(
                                    quantumChestReaderId, Coordinates(x, y, z), scannedContent
                                )
                            )
                        }
                    }

                    call.respond(quantumChestReaderResponses)
                }

                get("/quantum-chest-reader/{quantumChestReaderId}") {
                    val quantumChestReaderId = call.parameters["quantumChestReaderId"]

                    if (quantumChestReaderId == null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("id-parameter-not-provided", "Id parameter not provided"))
                    } else {
                        val serverState = ServerState.getServerState(server)
                        val quantumChestReaderData = serverState.quantumChestReaderData[quantumChestReaderId]
                        if (quantumChestReaderData == null) {
                            call.respond(
                                HttpStatusCode.NotFound, ErrorResponse(
                                    "id-no-found", "Quantum chest with ID $quantumChestReaderId not found"
                                )
                            )
                        } else {
                            val (x, y, z) = quantumChestReaderData
                            val inventoryEntity = getInventoryEntity(server, x, y, z)

                            if (inventoryEntity != null) {
                                val scannedContent = getInventoryContents(inventoryEntity)
                                call.respond(
                                    QuantumChestReaderResponse(
                                        quantumChestReaderId, Coordinates(x, y, z), scannedContent
                                    )
                                )
                            } else {
                                SortSavvy.LOGGER.info("No inventory found at x=$x y=$y z=$z")

                                call.respond(
                                    HttpStatusCode.NotFound,
                                    ErrorResponse(
                                        "no-inventory-found", "No inventory found at x=$x y=$y z=$z", Coordinates(x, y, z)
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
