package me.thatonedevil.screen

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

open class VersionedScreen(name: String, val parentScreen: Screen?) : Screen(Component.literal(name)) {

    // in later versions minecraft is nullable
    val client = Minecraft.getInstance()

    //? if >=1.21.9 {
    val clientWindow = client.window.handle()
    //? } else {
    /*val clientWindow = client.window.window
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
    }*/
    //? } else {
    /*override fun renderBlurredBackground(f: Float) {
        super.renderBlurredBackground(f)
    }*/
    //? }



    override fun onClose() {
        super.onClose()

        if (parentScreen != null) {
            minecraft?.setScreen(parentScreen)
        }
    }
}