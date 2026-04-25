package me.thatonedevil.keybinds

import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import me.thatonedevil.handlers.ItemParseHandler
import me.thatonedevil.mixin.client.AbstractContainerScreenAccessor
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.inventory.*
import org.lwjgl.glfw.GLFW

class YoinkSingleKeybind : Key {

    companion object {
        @JvmStatic
        lateinit var keyMapping: KeyMapping
    }

    override fun keyName(): String = "key.yoinkgui.yoinksingle"
    override fun key(): Int = GLFW.GLFW_KEY_Y
    override fun whenPressed() {}

    override fun register(): KeyMapping {
        keyMapping = super.register()
        return keyMapping
    }

    override fun registerScreenEvents() {
        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            ScreenKeyboardEvents.afterKeyPress(screen).register { screen, keyInput ->
                if (!isValidInventoryScreen(screen)) return@register
                if (!yoinkGuiSettings.enableSingleItemYoink.get()) return@register

                if (keyMapping.matches(keyInput)) {
                    val slot = (screen as AbstractContainerScreenAccessor).`yoinkgui$getHoveredSlot`()
                    if (slot != null && !slot.item.isEmpty) {
                        ItemParseHandler.handleSingleItemParse(slot.item)
                    }
                }
            }
        }
    }

    private fun isValidInventoryScreen(screen: Any) =
        screen is InventoryScreen
                || screen is ContainerScreen
                || screen is MerchantScreen
                || screen is CreativeModeInventoryScreen
                || screen is ShulkerBoxScreen
}