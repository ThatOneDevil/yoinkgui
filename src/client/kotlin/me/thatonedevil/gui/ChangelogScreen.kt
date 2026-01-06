package me.thatonedevil.gui


import me.thatonedevil.BuildConfig.VERSION
import me.thatonedevil.gui.MarkdownLoader.parse
import me.thatonedevil.utils.LatestErrorLog
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import kotlin.math.max

@Environment(EnvType.CLIENT)
class ChangelogScreen(private val parent: Screen?) : Screen(Text.literal("Changelog")) {
    private lateinit var content: List<Text>
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
                    parse(it.readLines(), width - 40)
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

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val centerX = width / 2

        if (!this::content.isInitialized || content.isEmpty()) {
            context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("No changelog available."),
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
                context.drawCenteredTextWithShadow(
                    textRenderer,
                    line,
                    centerX,
                    y,
                    0xFFE0E0E0.toInt()
                )
            }
            y += lineHeight
        }

        context.disableScissor()

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Press ESC to close"),
            centerX,
            height - 20,
            0xFFAAAAAA.toInt()
        )

        if (maxScroll > 0) {
            val scrollPercentage = (scrollOffset / maxScroll * 100).toInt()
            context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("↕ Scroll: $scrollPercentage%"),
                centerX,
                height - 10,
                0xFF888888.toInt()
            )
        }
    }

    override fun close() {
        client?.setScreen(parent)
    }
}
