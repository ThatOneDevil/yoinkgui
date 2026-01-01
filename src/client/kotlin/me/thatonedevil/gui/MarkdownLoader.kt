package me.thatonedevil.gui

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
        val result = mutableListOf<Text>()

        for (rawLine in lines) {
            // Remove all backticks from the line
            val line = rawLine.replace("`", "")

            when {
                line.startsWith("# ") -> {
                    result += createStyledText(line.removePrefix("# "), TITLE, bold = true)
                }

                line.startsWith("## ") -> {
                    result += createStyledText(line.removePrefix("## "), SECTION, bold = true)
                }

                line.startsWith("### ") -> {
                    result += createStyledText(line.removePrefix("### "), SUBSECTION, bold = true)
                }

                line.startsWith("- ") || line.startsWith("* ") -> {
                    result += wrapAndStyle("• ${line.drop(2)}", wrapWidth, BULLET)
                }

                line.isBlank() -> {
                    result += Text.empty()
                }

                else -> {
                    result += wrapAndStyle(line, wrapWidth, NORMAL)
                }
            }
        }

        return result
    }

    /**
     * Creates a styled text with the specified color and optional bold formatting.
     */
    private fun createStyledText(text: String, color: TextColor, bold: Boolean = false): Text {
        return Text.literal(text).styled { style ->
            var s = style.withColor(color)
            if (bold) s = s.withBold(true)
            s
        }
    }

    /**
     * Wraps text to fit within the specified width and applies styling to each line.
     */
    private fun wrapAndStyle(text: String, maxWidth: Int, color: TextColor): List<Text> {
        return wrapText(text, maxWidth).map { wrappedLine ->
            Text.literal(wrappedLine).styled { style -> style.withColor(color) }
        }
    }

    /**
     * Wraps text to fit within the specified pixel width, breaking on word boundaries.
     * Returns a list of strings (not Text objects).
     */
    private fun wrapText(text: String, maxWidth: Int): List<String> {
        if (text.isEmpty()) return listOf("")

        val renderer = MinecraftClient.getInstance().textRenderer
        val words = text.split(" ")

        val lines = mutableListOf<String>()
        var current = ""

        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"

            if (renderer.getWidth(test) > maxWidth) {
                if (current.isNotEmpty()) {
                    lines += current
                    current = word
                } else {
                    lines += word
                }
            } else {
                current = test
            }
        }

        if (current.isNotEmpty()) lines += current

        return lines
    }
}
