package me.thatonedevil.handlers

import me.thatonedevil.screen.ButtonPositionScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft

object ParseButtonHandler {
    var parseButtonHovered = false
    private var wasLeftClicking = false

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.gui.screen() == null) {
                parseButtonHovered = false
                wasLeftClicking = false
                return@register
            }

            if (client.gui.screen() is ButtonPositionScreen) {
                parseButtonHovered = false
                return@register
            }

            val window = client.window.handle()

            val isLeftClicking = Minecraft.getInstance().mouseHandler.isLeftPressed

            if (client.player != null && isLeftClicking && !wasLeftClicking) {
                if (parseButtonHovered) {
                    ItemParseHandler.handleParseButton(client)
                }
            }

            wasLeftClicking = isLeftClicking
        }
    }
}


