package me.thatonedevil.keybinds

import me.thatonedevil.gui.ButtonPositionScreen
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

class MenuKeybind : Key{

    override fun keyName(): String {
        return "key.yoinkgui.position"
    }

    override fun key(): Int {
        return GLFW.GLFW_KEY_M
    }

    override fun whenPressed() {
        val client = MinecraftClient.getInstance()

        // Close screen when button pressed again while in ButtonPositionScreen
        if (client.currentScreen is ButtonPositionScreen) {
            client.setScreen(null)
            return
        }

        client.setScreen(ButtonPositionScreen(client.currentScreen))
    }

}