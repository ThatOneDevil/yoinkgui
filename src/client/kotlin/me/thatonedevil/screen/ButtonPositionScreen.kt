package me.thatonedevil.screen

import com.mojang.blaze3d.platform.InputConstants
import me.thatonedevil.YoinkGUIClient
import me.thatonedevil.config.YoinkGuiSettings
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class ButtonPositionScreen(parent: Screen?) : VersionedScreen("Position Yoink Button", parent) {
    private val config: YoinkGuiSettings = YoinkGUIClient.yoinkGuiSettings

    private var dragging = false
    private var dragOffsetX = 0.0
    private var dragOffsetY = 0.0
    private lateinit var previewButton: Button

    private val baseButtonWidth = 160
    private val buttonHeight = 20

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
        get() = (baseButtonWidth * scaleFactor).toInt().coerceAtLeast(20)

    override fun init() {
        super.init()

        previewButton = Button.builder(Component.literal("Yoink and Parse NBT into file")) { _ -> }
            .bounds(buttonX, buttonY, scaledButtonWidth, buttonHeight)
            .build()

        addRenderableWidget(previewButton)
        clampButtonPosition()
        updatePreviewButton()
    }

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)

        graphics.centeredText(
            font,
            Component.literal("Drag the button to reposition it"),
            width / 2,
            20,
            0xFFFFFFFF.toInt()
        )
        graphics.centeredText(
            font,
            Component.literal("Use mouse wheel to scale (Current: ${"%.2f".format(scaleFactor)}x)"),
            width / 2,
            35,
            0xFFFFFFFF.toInt()
        )
        graphics.centeredText(
            font,
            Component.literal("Press ESC or ENTER to save and exit"),
            width / 2,
            50,
            0xFFFFFFFF.toInt()
        )
    }

    override fun onMouseClicked(x: Double, y: Double, button: Int): Boolean {
        if (!::previewButton.isInitialized
            || button != InputConstants.MOUSE_BUTTON_LEFT
            || !previewButton.isMouseOver(x, y)
        ) {
            return false
        }

        dragging = true
        dragOffsetX = x - buttonX
        dragOffsetY = y - buttonY
        return true
    }

    override fun onMouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        if (!dragging || event.buttonInfo().button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return false
        }

        buttonX = (event.x() - dragOffsetX).toInt()
        buttonY = (event.y() - dragOffsetY).toInt()
        clampButtonPosition()
        updatePreviewButton()
        return true
    }

    override fun onMouseReleased(event: MouseButtonEvent): Boolean {
        if (event.buttonInfo().button() == InputConstants.MOUSE_BUTTON_LEFT) {
            dragging = false
        }

        return false
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        scaleFactor = (scaleFactor + verticalAmount.toFloat() * 0.1f).coerceIn(0.1f, 2.0f)
        clampButtonPosition()
        updatePreviewButton()
        return true
    }

    override fun onKeyPressed(key: Int): Boolean {
        if (key == InputConstants.KEY_DOWN) {
            onClose()
            return true
        }

        return false
    }

    override fun onClose() {
        YoinkGuiSettings.saveToFile()
        super.onClose()
    }

    private fun updatePreviewButton() {
        if (!::previewButton.isInitialized) {
            return
        }

        previewButton.setSize(scaledButtonWidth, buttonHeight)
        previewButton.setPosition(buttonX, buttonY)
    }

    private fun clampButtonPosition() {
        buttonX = buttonX.coerceIn(0, (width - scaledButtonWidth).coerceAtLeast(0))
        buttonY = buttonY.coerceIn(0, (height - buttonHeight).coerceAtLeast(0))
    }
}
