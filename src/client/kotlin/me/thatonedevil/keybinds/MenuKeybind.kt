package me.thatonedevil.keybinds

import me.thatonedevil.gui.ButtonPositionScreen
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
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

        client.setScreen(ButtonPositionScreen(client.currentScreen))
    }

}