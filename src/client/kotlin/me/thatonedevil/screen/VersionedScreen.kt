package me.thatonedevil.screen

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

open class VersionedScreen(name: String, val parentScreen: Screen?) : Screen(Component.literal(name)) {

    val client = Minecraft.getInstance()

    val clientWindow = client.window.handle()

    open fun onMouseClicked(x: Double, y: Double, button: Int): Boolean = false
    open fun onKeyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean = false


    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        if (onMouseClicked(mouseButtonEvent.x, mouseButtonEvent.y, mouseButtonEvent.buttonInfo.button)) return true
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        if (onKeyPressed(keyEvent.key, keyEvent.key, keyEvent.modifiers)) return true
        return super.keyPressed(keyEvent)
    }

    override fun init() {
        super.init()
    }

    override fun onClose() {
        super.onClose()
        if (parentScreen != null) {
            minecraft.setScreen(parentScreen)
        }
    }
}