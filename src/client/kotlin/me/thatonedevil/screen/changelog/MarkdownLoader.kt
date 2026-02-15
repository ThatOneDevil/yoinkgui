package me.thatonedevil.screen.changelog

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor

object MarkdownLoader {

    private val TITLE = TextColor.fromRgb(0xF5F5F5)
    private val SECTION = TextColor.fromRgb(0xB8C7FF)
    private val SUBSECTION = TextColor.fromRgb(0xC7EDE6)
    private val BULLET = TextColor.fromRgb(0xDADADA)
    private val NORMAL = TextColor.fromRgb(0xB5B5B5)
    private val CODE_BLOCK = TextColor.fromRgb(0x7FE3CD)
    private val ITALIC = TextColor.fromRgb(0xFFD4A3)

    fun parse(lines: List<String>, wrapWidth: Int): List<Component> {
        return lines.flatMap { rawLine ->
            parseLine(rawLine, wrapWidth)
        }
    }

    private fun parseLine(line: String, wrapWidth: Int): List<Component> = when {
        line.startsWith("# ") ->
            listOf(createStyledText(line.removePrefix("# "), TITLE, bold = true))

        line.startsWith("## ") ->
            listOf(createStyledText(line.removePrefix("## "), SECTION, bold = true))

        line.startsWith("### ") ->
            listOf(createStyledText(line.removePrefix("### "), SUBSECTION, bold = true))

        line.startsWith("- ") || line.startsWith("* ") ->
            wrapAndStyleWithFormatting("• ${line.drop(2)}", wrapWidth, BULLET)

        line.isBlank() ->
            listOf(Component.empty())

        else ->
            wrapAndStyleWithFormatting(line, wrapWidth, NORMAL)
    }

    private fun createStyledText(text: String, color: TextColor, bold: Boolean = false): Component {
        return Component.literal(text).withStyle { style ->
            style.withColor(color).let { if (bold) it.withBold(true) else it }
        }
    }

    private fun wrapAndStyleWithFormatting(text: String, maxWidth: Int, color: TextColor): List<Component> {
        return wrapText(text, maxWidth).map { wrappedLine ->
            parseInlineFormatting(wrappedLine, color)
        }
    }

    private fun parseInlineFormatting(text: String, defaultColor: TextColor): Component {
        val result = Component.empty()
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
                    Component.literal(text.substring(lastIndex, start))
                        .withStyle { it.withColor(defaultColor) }
                )
            }

            // Add formatted text
            when (type) {
                "code" -> result.append(
                    Component.literal(content)
                        .withStyle { it.withColor(CODE_BLOCK) }
                )
                "italic" -> result.append(
                    Component.literal(content)
                        .withStyle { it.withColor(ITALIC).withItalic(true) }
                )
            }

            lastIndex = end + 1
        }

        // Add remaining text
        if (lastIndex < text.length) {
            result.append(
                Component.literal(text.substring(lastIndex))
                    .withStyle { it.withColor(defaultColor) }
            )
        }

        return result
    }

    private fun wrapText(text: String, maxWidth: Int): List<String> {
        if (text.isEmpty()) return listOf("")

        val renderer = Minecraft.getInstance().font
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"

            if (renderer.width(testLine) > maxWidth) {
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