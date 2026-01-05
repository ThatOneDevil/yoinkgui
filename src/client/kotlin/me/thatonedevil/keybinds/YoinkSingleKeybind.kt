package me.thatonedevil.keybinds

import me.thatonedevil.utils.Utils
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

class YoinkSingleKeybind : Key{

    override fun keyName(): String {
        return "key.yoinkgui.yoinksingle"
    }

    override fun key(): Int {
        return GLFW.GLFW_KEY_X
    }

    override fun whenPressed() {
        val client = MinecraftClient.getInstance()
        Utils.sendChat("/yoink single keybind pressed")
    }

}