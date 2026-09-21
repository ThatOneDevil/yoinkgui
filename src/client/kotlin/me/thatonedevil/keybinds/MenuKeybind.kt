package me.thatonedevil.keybinds

import com.mojang.blaze3d.platform.InputConstants
import me.thatonedevil.screen.ButtonPositionScreen
import net.minecraft.client.Minecraft

class MenuKeybind : Key {

    override fun keyName(): String {
        return "key.yoinkgui.position"
    }

    override fun key(): Int {
        return InputConstants.KEY_M
    }

    override fun whenPressed() {
        val client = Minecraft.getInstance()

        client.gui.setScreen(ButtonPositionScreen(client.gui.screen()))
    }

}