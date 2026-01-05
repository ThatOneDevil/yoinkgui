package me.thatonedevil.keybinds

import me.thatonedevil.utils.Utils
import org.lwjgl.glfw.GLFW

class YoinkSingleKeybind : Key{

    override fun keyName(): String {
        return "key.yoinkgui.yoinksingle"
    }

    override fun key(): Int {
        return GLFW.GLFW_KEY_Y
    }

    override fun whenPressed() {
        Utils.sendChat("/yoink single keybind pressed")
    }

}