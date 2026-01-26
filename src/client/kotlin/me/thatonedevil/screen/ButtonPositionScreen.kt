package me.thatonedevil.screen

import me.thatonedevil.YoinkGUIClient
import me.thatonedevil.config.YoinkGuiSettings
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

@Environment(EnvType.CLIENT)
class ButtonPositionScreen(private val parent: Screen?) : Screen(Text.literal("Position Yoink Button")) {

    override fun init() {
        super.init()
    }

    private val config: YoinkGuiSettings = YoinkGUIClient.yoinkGuiSettings
    private var dragging = false
    private var dragOffsetX = 0
    private var dragOffsetY = 0
    private var wasMousePressed = false

    private val baseButtonWidth = 160
    private val baseButtonHeight = 20

    private var buttonX: Int
        get() = config.buttonX.get()
        set(value) {
            config.buttonX.set(value)
        }

    private var buttonY: Int
        get() = config.buttonY.get()
        set(value) {
            config.buttonY.set(value)
        }

    private var scaleFactor: Float
        get() = config.buttonScaleFactor.get()
        set(value) {
            config.buttonScaleFactor.set(value)
        }

    private val scaledButtonWidth: Int
        get() = (baseButtonWidth * scaleFactor).toInt()

    private val scaledButtonHeight: Int
        get() = (baseButtonHeight * scaleFactor).toInt()

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        val window = client?.window?.handle

        if (window != null) {
            val isMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
            if (isMousePressed && !wasMousePressed) {
                if (isMouseOverButton(mouseX, mouseY)) {
                    dragging = true
                    dragOffsetX = mouseX - buttonX
                    dragOffsetY = mouseY - buttonY
                }
            } else if (!isMousePressed && wasMousePressed) {
                dragging = false
            }

            wasMousePressed = isMousePressed
        }

        // Handle dragging
        if (dragging) {
            buttonX = (mouseX - dragOffsetX).coerceIn(0, width - scaledButtonWidth)
            buttonY = (mouseY - dragOffsetY).coerceIn(0, height - scaledButtonHeight)
        }

        val buttonColor = if (isMouseOverButton(mouseX, mouseY)) 0xAA444444.toInt() else 0xAA000000.toInt()
        context.fill(
            buttonX,
            buttonY,
            buttonX + scaledButtonWidth,
            buttonY + scaledButtonHeight,
            buttonColor
        )
        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Yoink and Parse NBT into file"),
            buttonX + scaledButtonWidth / 2,
            buttonY + (scaledButtonHeight - 8) / 2,
            0xFFFFFFFF.toInt()
        )

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Drag the button to reposition it"),
            width / 2,
            20,
            0xFFFFFFFF.toInt()
        )

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Use mouse wheel to scale (Current: ${"%.2f".format(scaleFactor)}x)"),
            width / 2,
            35,
            0xFFFFFFFF.toInt()
        )

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Press ESC or ENTER to save and exit"),
            width / 2,
            50,
            0xFFFFFFFF.toInt()
        )

    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val delta = verticalAmount.toFloat() * 0.1f
        scaleFactor = (scaleFactor + delta).coerceIn(0.1f, 2.0f)

        buttonX = buttonX.coerceIn(0, width - scaledButtonWidth)
        buttonY = buttonY.coerceIn(0, height - scaledButtonHeight)

        return true
    }

    override fun close() {
        YoinkGuiSettings.saveToFile()
        client?.setScreen(parent)
    }

    private fun isMouseOverButton(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= buttonX && mouseX <= buttonX + scaledButtonWidth &&
               mouseY >= buttonY && mouseY <= buttonY + scaledButtonHeight
    }

}

