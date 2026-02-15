package me.thatonedevil.handlers

import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import me.thatonedevil.mixin.client.AbstractContainerScreenAccessor
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.gui.screens.inventory.MerchantScreen
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen
import net.minecraft.world.inventory.Slot

object SingleItemYoinkHandler {

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

                if (keyInput.key == InputConstants.KEY_Y) {
                    val slot: Slot? = (screen as AbstractContainerScreenAccessor).`yoinkgui$getHoveredSlot`()
                    if (slot != null && !slot.item.isEmpty) {
                        ItemParseHandler.handleSingleItemParse(slot.item)
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
                || screen is ContainerScreen
                || screen is MerchantScreen
                || screen is CreativeModeInventoryScreen
                || screen is ShulkerBoxScreen
    }
}

