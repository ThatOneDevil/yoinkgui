package me.thatonedevil.screen.changelog


import me.thatonedevil.BuildConfig.VERSION
import me.thatonedevil.utils.LatestErrorLog
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.math.max

@Environment(EnvType.CLIENT)
class ChangelogScreen(private val parent: Screen?) : Screen(Component.literal("Changelog")) {
    private lateinit var content: List<Component>
    private var scrollOffset = 0.0
    private var maxScroll = 0.0

    private val lineHeight = 12
    private val topPadding = 20
    private val bottomPadding = 30
    private val scrollSpeed = 20.0

    override fun init() {
        try {
            val resourceStream = javaClass.getResourceAsStream("/changelogs/${VERSION}.md")

            if (resourceStream != null) {
                content = resourceStream.bufferedReader().use {
                    MarkdownLoader.parse(it.readLines(), width - 40)
                }
                updateMaxScroll()
            }
        } catch (e: Exception) {
            LatestErrorLog.record(e, "Failed to load changelog markdown.")
            content = emptyList()
        }
    }

    private fun updateMaxScroll() {
        if (!this::content.isInitialized) return

        val contentHeight = content.size * lineHeight
        val viewportHeight = height - topPadding - bottomPadding
        maxScroll = max(0.0, (contentHeight - viewportHeight).toDouble())
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        scrollOffset = (scrollOffset - verticalAmount * scrollSpeed).coerceIn(0.0, maxScroll)
        return true
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val centerX = width / 2

        if (!this::content.isInitialized || content.isEmpty()) {
            context.drawCenteredString(
                font,
                Component.literal("No changelog available."),
                centerX,
                height / 2,
                0xFFE0E0E0.toInt()
            )
            return
        }

        val scissorTop = topPadding
        val scissorBottom = height - bottomPadding

        context.enableScissor(0, scissorTop, width, scissorBottom)

        var y = topPadding - scrollOffset.toInt()

        content.forEach { line ->
            if (y + lineHeight > scissorTop && y < scissorBottom) {
                context.drawCenteredString(
                    font,
                    line,
                    centerX,
                    y,
                    0xFFE0E0E0.toInt()
                )
            }
            y += lineHeight
        }

        context.disableScissor()

        context.drawCenteredString(
            font,
            Component.literal("Press ESC to close"),
            centerX,
            height - 20,
            0xFFAAAAAA.toInt()
        )

        if (maxScroll > 0) {
            val scrollPercentage = (scrollOffset / maxScroll * 100).toInt()
            context.drawCenteredString(
                font,
                Component.literal("↕ Scroll: $scrollPercentage%"),
                centerX,
                height - 10,
                0xFF888888.toInt()
            )
        }
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }
}
