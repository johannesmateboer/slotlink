package badasintended.slotlink.client.gui.widget

import badasintended.slotlink.init.Packets
import badasintended.slotlink.mixin.CraftingScreenHandlerAccessor
import badasintended.slotlink.screen.RequestScreenHandler
import badasintended.slotlink.util.c2s
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.Screen

@Environment(EnvType.CLIENT)
class CraftingResultSlotWidget(
    private val handler: RequestScreenHandler,
    x: Int, y: Int
) : SlotWidget(x, y, 26, handler.playerInventory, { (handler as CraftingScreenHandlerAccessor).result.getStack(0) }) {

    override fun onClick(button: Int) {
        c2s(Packets.CRAFTING_RESULT_SLOT_CLICK) {
            writeVarInt(handler.syncId)
            writeVarInt(button)
            writeBoolean(Screen.hasShiftDown())
        }
    }

}
