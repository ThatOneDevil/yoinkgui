package me.thatonedevil.screen.changelog

import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.text.TextColor

object MarkdownLoader {

    private val TITLE = TextColor.fromRgb(0xF5F5F5)
    private val SECTION = TextColor.fromRgb(0xB8C7FF)
    private val SUBSECTION = TextColor.fromRgb(0xC7EDE6)
    private val BULLET = TextColor.fromRgb(0xDADADA)
    private val NORMAL = TextColor.fromRgb(0xB5B5B5)

    fun parse(lines: List<String>, wrapWidth: Int): List<Text> {
        return lines.flatMap { rawLine ->
            parseLine(rawLine.replace("`", ""), wrapWidth)
        }
    }

    private fun parseLine(line: String, wrapWidth: Int): List<Text> = when {
        line.startsWith("# ") ->
            listOf(createStyledText(line.removePrefix("# "), TITLE, bold = true))

        line.startsWith("## ") ->
            listOf(createStyledText(line.removePrefix("## "), SECTION, bold = true))

        line.startsWith("### ") ->
            listOf(createStyledText(line.removePrefix("### "), SUBSECTION, bold = true))

        line.startsWith("- ") || line.startsWith("* ") ->
            wrapAndStyle("• ${line.drop(2)}", wrapWidth, BULLET)

        line.isBlank() ->
            listOf(Text.empty())

        else ->
            wrapAndStyle(line, wrapWidth, NORMAL)
    }

    private fun createStyledText(text: String, color: TextColor, bold: Boolean = false): Text {
        return Text.literal(text).styled { style ->
            style.withColor(color).let { if (bold) it.withBold(true) else it }
        }
    }

    private fun wrapAndStyle(text: String, maxWidth: Int, color: TextColor): List<Text> {
        return wrapText(text, maxWidth).map { wrappedLine ->
            Text.literal(wrappedLine).styled { it.withColor(color) }
        }
    }

    private fun wrapText(text: String, maxWidth: Int): List<String> {
        if (text.isEmpty()) return listOf("")

        val renderer = MinecraftClient.getInstance().textRenderer
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"

            if (renderer.getWidth(testLine) > maxWidth) {
                // Line would be too long, finalize current line
                if (currentLine.isNotEmpty()) {
                    lines += currentLine.toString()
                    currentLine = StringBuilder(word)
                } else {
                    // Single word exceeds max width
                    lines += word
                }
            } else {
                currentLine = StringBuilder(testLine)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines += currentLine.toString()
        }

        return lines.ifEmpty { listOf("") }
    }
}