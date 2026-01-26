package me.thatonedevil.handlers

import me.thatonedevil.screen.ButtonPositionScreen
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.lwjgl.glfw.GLFW

object ParseButtonHandler {
    var parseButtonHovered = false
    private var wasLeftClicking = false

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                parseButtonHovered = false
                wasLeftClicking = false
                return@register
            }

            if (client.currentScreen is ButtonPositionScreen) {
                parseButtonHovered = false
                return@register
            }

            val isLeftClicking = GLFW.glfwGetMouseButton(client.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

            if (client.player != null && isLeftClicking && !wasLeftClicking) {
                if (parseButtonHovered) {
                    ItemParseHandler.handleParseButton(client)
                }
            }

            wasLeftClicking = isLeftClicking
        }
    }
}


