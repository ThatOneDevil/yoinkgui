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
    private val CODE_BLOCK = TextColor.fromRgb(0x7FE3CD)
    private val ITALIC = TextColor.fromRgb(0xFFD4A3) // Soft peach/gold color

    fun parse(lines: List<String>, wrapWidth: Int): List<Text> {
        return lines.flatMap { rawLine ->
            parseLine(rawLine, wrapWidth)
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
            wrapAndStyleWithFormatting("• ${line.drop(2)}", wrapWidth, BULLET)

        line.isBlank() ->
            listOf(Text.empty())

        else ->
            wrapAndStyleWithFormatting(line, wrapWidth, NORMAL)
    }

    private fun createStyledText(text: String, color: TextColor, bold: Boolean = false): Text {
        return Text.literal(text).styled { style ->
            style.withColor(color).let { if (bold) it.withBold(true) else it }
        }
    }

    private fun wrapAndStyleWithFormatting(text: String, maxWidth: Int, color: TextColor): List<Text> {
        return wrapText(text, maxWidth).map { wrappedLine ->
            parseInlineFormatting(wrappedLine, color)
        }
    }

    private fun parseInlineFormatting(text: String, defaultColor: TextColor): Text {
        val result = Text.empty()
        val codeRegex = "`([^`]+)`".toRegex()
        val italicRegex = "\\*([^*]+)\\*".toRegex()

        // Find all matches and sort by position
        val codeMatches = codeRegex.findAll(text).map {
            Triple(it.range.first, it.range.last, Pair("code", it.groupValues[1]))
        }
        val italicMatches = italicRegex.findAll(text).map {
            Triple(it.range.first, it.range.last, Pair("italic", it.groupValues[1]))
        }

        val allMatches = (codeMatches + italicMatches).sortedBy { it.first }

        var lastIndex = 0

        allMatches.forEach { (start, end, typeAndContent) ->
            val (type, content) = typeAndContent

            // Add text before this match
            if (start > lastIndex) {
                result.append(
                    Text.literal(text.substring(lastIndex, start))
                        .styled { it.withColor(defaultColor) }
                )
            }

            // Add formatted text
            when (type) {
                "code" -> result.append(
                    Text.literal(content)
                        .styled { it.withColor(CODE_BLOCK) }
                )
                "italic" -> result.append(
                    Text.literal(content)
                        .styled { it.withColor(ITALIC).withItalic(true) }
                )
            }

            lastIndex = end + 1
        }

        // Add remaining text
        if (lastIndex < text.length) {
            result.append(
                Text.literal(text.substring(lastIndex))
                    .styled { it.withColor(defaultColor) }
            )
        }

        return result
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
                // Line would be too long
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