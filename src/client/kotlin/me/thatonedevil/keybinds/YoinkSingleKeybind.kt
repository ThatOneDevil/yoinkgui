package me.thatonedevil.keybinds

import me.thatonedevil.utils.Utils
import me.thatonedevil.utils.Utils.sendChat
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

class YoinkSingleKeybind : Key{

    override fun keyName(): String {
        return "key.yoinkgui.yoinksingle"
    }

    override fun key(): Int {
        return GLFW.GLFW_KEY_Y
    }

    override fun whenPressed() {}

}