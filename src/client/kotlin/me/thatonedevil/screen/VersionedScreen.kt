package me.thatonedevil.screen

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
//? }

open class VersionedScreen(name: String, val parentScreen: Screen?) : Screen(Component.literal(name)) {

    val client = Minecraft.getInstance()

    //? if >=1.21.9 {
    val clientWindow = client.window.handle()
    //? } else {
    /*val clientWindow = client.window.window
    *///? }

    open fun onMouseClicked(x: Double, y: Double, button: Int): Boolean = false
    open fun onKeyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean = false


    //? if =1.21.9 {
    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        if (onMouseClicked(mouseButtonEvent.x, mouseButtonEvent.y, mouseButtonEvent.buttonInfo.button)) return true
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        if (onKeyPressed(keyEvent.key, keyEvent.key, keyEvent.modifiers)) return true
        return super.keyPressed(keyEvent)
    }
    //? } else {
    /*override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
        if (onMouseClicked(d, e, i)) return true
        return super.mouseClicked(d, e, i)
    }

    override fun keyPressed(i: Int, j: Int, k: Int): Boolean {
        if (onKeyPressed(i, j, k)) return true
        return super.keyPressed(i, j, k)
    }
    *///? }

    override fun init() {
        super.init()
    }

    //? if =1.21.9 || =1.21.8 {
    override fun renderBlurredBackground(guiGraphics: GuiGraphics) {
        super.renderBlurredBackground(guiGraphics)
    }
    //? } elif =1.21.5 || =1.21.4 {
    /*override fun renderBlurredBackground() {
        super.renderBlurredBackground()
    }
    *///? } else {
    /*override fun renderBlurredBackground(f: Float) {
        super.renderBlurredBackground(f)
    }
    *///? }

    override fun onClose() {
        super.onClose()
        if (parentScreen != null) {
            minecraft?.setScreen(parentScreen)
        }
    }
}