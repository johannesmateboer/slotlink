package badasintended.slotlink.init

import badasintended.slotlink.screen.LinkScreenHandler
import badasintended.slotlink.screen.RequestScreenHandler
import badasintended.slotlink.screen.TransferScreenHandler
import badasintended.slotlink.util.RedstoneMode
import badasintended.slotlink.util.Sort
import badasintended.slotlink.util.modId
import badasintended.slotlink.util.readStr
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

object Packets : Initializer {

    // C2S
    val RESIZE = modId("resize")
    val SORT = modId("sort")
    val SCROLL = modId("scroll")
    val MULTI_SLOT_CLICK = modId("multi_slot_click")
    val CRAFTING_RESULT_SLOT_CLICK = modId("crafting_result_slot_click")
    val CLEAR_CRAFTING_GRID = modId("clear_crafting_grid")
    val APPLY_RECIPE = modId("apply_recipe")
    val MOVE = modId("move")
    val RESTOCK = modId("restock")
    val FILTER_SLOT_CLICK = modId("filter_slot_click")
    val LINK_SETTINGS = modId("link_cable_settings")
    val TRANSFER_SETTINGS = modId("transfer_settings")

    // S2C
    val UPDATE_SLOT_NUMBERS = modId("update_slot_numbers")
    val UPDATE_VIEWED_STACK = modId("update_viewed_stack")
    val UPDATE_MAX_SCROLL = modId("update_max_scroll")
    val UPDATE_CURSOR = modId("update_cursor")

    override fun main() {
        s(SORT) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val sort = Sort.of(buf.readVarInt())
            val filter = buf.readStr()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.sort(sort, filter)
                }
            }
        }

        s(SCROLL) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val amount = buf.readVarInt()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.scroll(amount)
                }
            }
        }

        s(MULTI_SLOT_CLICK) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val index = buf.readVarInt()
            val button = buf.readVarInt()
            val quickMove = buf.readBoolean()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.multiSlotClick(index, button, quickMove)
                }
            }
        }

        s(APPLY_RECIPE) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val recipeId = buf.readIdentifier()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    val recipe = player.world.recipeManager.get(recipeId)
                    if (recipe.isPresent) handler.applyRecipe(recipe.get())
                }
            }
        }

        s(CRAFTING_RESULT_SLOT_CLICK) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val button = buf.readVarInt()
            val quickMove = buf.readBoolean()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.craftingResultSlotClick(button, quickMove)
                }
            }
        }

        s(RESIZE) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val viewedHeight = buf.readVarInt()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.resize(viewedHeight)
                }
            }
        }

        s(CLEAR_CRAFTING_GRID) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.clearCraftingGrid(true)
                }
            }
        }

        s(MOVE) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.move()
                }
            }
        }

        s(RESTOCK) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.restock()
                }
            }
        }

        s(FILTER_SLOT_CLICK) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val index = buf.readVarInt()
            val button = buf.readVarInt()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is LinkScreenHandler) {
                    handler.filterSlotClick(index, button)
                }
            }
        }

        s(LINK_SETTINGS) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val priority = buf.readVarInt()
            val blacklist = buf.readBoolean()

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is LinkScreenHandler) {
                    handler.priority = priority
                    handler.blacklist = blacklist
                }
            }
        }

        s(TRANSFER_SETTINGS) { server, player, _, buf, _ ->
            val syncId = buf.readVarInt()
            val redstone = RedstoneMode.of(buf.readVarInt())
            val side = Direction.byId(buf.readVarInt())

            server.execute {
                val handler = player.currentScreenHandler
                if (handler.syncId == syncId) if (handler is TransferScreenHandler) {
                    handler.side = side
                    handler.redstone = redstone
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    override fun client() {
        c(UPDATE_SLOT_NUMBERS) { client, _, buf, _ ->
            val syncId = buf.readVarInt()
            val total = buf.readVarInt()
            val filled = buf.readVarInt()

            client.execute {
                val handler = client.player!!.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.totalSlotSize = total
                    handler.filledSlotSize = filled
                }
            }
        }

        c(UPDATE_CURSOR) { client, _, buf, _ ->
            val stack = buf.readItemStack()

            client.execute {
                client.player!!.inventory.cursorStack = stack
            }
        }

        c(UPDATE_MAX_SCROLL) { client, _, buf, _ ->
            val syncId = buf.readVarInt()
            val maxScroll = buf.readVarInt()

            client.execute {
                val handler = client.player!!.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.maxScroll = maxScroll
                }
            }
        }

        c(UPDATE_VIEWED_STACK) { client, _, buf, _ ->
            val syncId = buf.readVarInt()
            val index = buf.readVarInt()
            val stack = buf.readItemStack()
            val count = buf.readVarInt()

            client.execute {
                val handler = client.player!!.currentScreenHandler
                if (handler.syncId == syncId) if (handler is RequestScreenHandler) {
                    handler.viewedStacks[index] = stack to count
                }
            }
        }
    }

    private fun s(id: Identifier, function: (MinecraftServer, ServerPlayerEntity, ServerPlayNetworkHandler, PacketByteBuf, PacketSender) -> Unit) {
        ServerPlayNetworking.registerGlobalReceiver(id, function)
    }

    @Environment(EnvType.CLIENT)
    private fun c(id: Identifier, function: (MinecraftClient, ClientPlayNetworkHandler, PacketByteBuf, PacketSender) -> Unit) {
        ClientPlayNetworking.registerGlobalReceiver(id, function)
    }

}
