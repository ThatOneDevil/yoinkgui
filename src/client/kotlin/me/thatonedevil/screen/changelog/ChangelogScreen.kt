package me.thatonedevil.screen.changelog

import me.thatonedevil.BuildConfig.VERSION
import me.thatonedevil.screen.VersionedScreen
import me.thatonedevil.utils.ErrorReporter
import me.thatonedevil.utils.LatestErrorLog
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.math.max

@Environment(EnvType.CLIENT)
class ChangelogScreen(parent: Screen?) : VersionedScreen("Changelog", parent) {
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
            ErrorReporter.report(e, "Failed to load changelog markdown.")
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

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractRenderState(context, mouseX, mouseY, a)
        val centerX = width / 2

        if (!this::content.isInitialized || content.isEmpty()) {
            context.centeredText(
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
                context.centeredText(
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

        context.centeredText(
            font,
            Component.literal("Press ESC to close"),
            centerX,
            height - 20,
            0xFFAAAAAA.toInt()
        )

        if (maxScroll > 0) {
            val scrollPercentage = (scrollOffset / maxScroll * 100).toInt()
            context.centeredText(
                font,
                Component.literal("↕ Scroll: $scrollPercentage%"),
                centerX,
                height - 10,
                0xFF888888.toInt()
            )
        }
    }
}
