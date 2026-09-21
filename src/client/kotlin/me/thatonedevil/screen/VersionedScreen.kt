package me.thatonedevil.screen

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

open class VersionedScreen(name: String, val parentScreen: Screen?) : Screen(Component.literal(name)) {
    val client = Minecraft.getInstance()

    open fun onMouseClicked(x: Double, y: Double, button: Int): Boolean = false
    open fun onMouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean = false
    open fun onMouseReleased(event: MouseButtonEvent): Boolean = false
    open fun onKeyPressed(key: Int): Boolean = false

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        if (onMouseClicked(event.x(), event.y(), event.buttonInfo().button())) {
            return true
        }

        return super.mouseClicked(event, doubleClick)
    }

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        if (onMouseDragged(event, dragX, dragY)) {
            return true
        }

        return super.mouseDragged(event, dragX, dragY)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (onMouseReleased(event)) {
            return true
        }

        return super.mouseReleased(event)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (onKeyPressed(event.key())) {
            return true
        }

        return super.keyPressed(event)
    }

    override fun init() {
        super.init()
    }

    override fun onClose() {
        super.onClose()
        parentScreen?.let { minecraft.gui.setScreen(it) }
    }
}
