package me.thatonedevil.handlers

import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import me.thatonedevil.mixin.client.HandledScreenAccessor
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.gui.screen.ingame.*
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.slot.Slot

object KeyboardEventHandler {

    fun register() {
        //? if >=1.21.9 {
        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            ScreenKeyboardEvents.afterKeyPress(screen).register { screen, keyInput ->
                if (!isValidInventoryScreen(screen)) {
                    return@register
                }

                if (!yoinkGuiSettings.enableSingleItemYoink.get()) {
                    return@register
                }

                if (keyInput.key == InputUtil.GLFW_KEY_Y) {
                    val slot: Slot? = (screen as HandledScreenAccessor).`yoinkgui$getFocusedSlot`()
                    if (slot != null && !slot.stack.isEmpty) {
                        ItemParseHandler.handleSingleItemParse(slot.stack)
                    }
                }
            }
        }
        //? } else {
        /*ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            ScreenKeyboardEvents.afterKeyPress(screen).register { screen, key, _, _ ->
                if (!isValidInventoryScreen(screen)) {
                    return@register
                }

                if (!yoinkGuiSettings.enableSingleItemYoink.get()) {
                    return@register
                }

                if (key == InputUtil.GLFW_KEY_Y) {
                    val slot: Slot? = (screen as HandledScreenAccessor).`yoinkgui$getFocusedSlot`()
                    if (slot != null && !slot.stack.isEmpty) {
                        ItemParseHandler.handleSingleItemParse(slot.stack)
                    }
                }
            }

        }*/
        //?}
    }



    private fun isValidInventoryScreen(screen: Any): Boolean {
        return screen is InventoryScreen
                || screen is GenericContainerScreen
                || screen is MerchantScreen
                || screen is CreativeInventoryScreen
                || screen is ShulkerBoxScreen
    }
}

