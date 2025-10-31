package me.thatonedevil

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thatonedevil.utils.Utils.toClickable
import me.thatonedevil.utils.Utils.toComponent
import me.thatonedevil.YoinkGUI.logger
import me.thatonedevil.utils.Utils
import java.io.File
import java.io.FileWriter
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NBTParser {

    private val gson = Gson()

    private val colorCodes = mapOf(
        "black" to "&0", "dark_blue" to "&1", "dark_green" to "&2", "dark_aqua" to "&3",
        "dark_red" to "&4", "dark_purple" to "&5", "gold" to "&6", "gray" to "&7",
        "dark_gray" to "&8", "blue" to "&9", "green" to "&a", "aqua" to "&b",
        "red" to "&c", "light_purple" to "&d", "yellow" to "&e", "white" to "&f"
    )

    private fun formatColor(color: String?): String {
        val lower = color?.lowercase() ?: return ""
        return colorCodes[lower] ?: if (color.startsWith("#")) "<color:${color.uppercase()}>" else ""
    }

    private fun getBooleanValue(element: JsonElement?): Boolean {
        return when {
            element == null -> false
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asNumber.toInt() == 1
                    primitive.isString -> primitive.asString in listOf("true", "1", "1b")
                    else -> false
                }
            }
            else -> false
        }
    }

    private fun detectGradient(component: JsonObject): String? {
        if (!component.has("extra") || component.getAsJsonArray("extra").size() < 4) return null

        val hexColors = mutableListOf<String>()

        fun collectColors(obj: JsonObject) {
            obj.get("color")?.asString?.takeIf { it.startsWith("#") }?.let { hexColors.add(it) }
            obj.get("extra")?.asJsonArray?.forEach { el ->
                if (el.isJsonObject) collectColors(el.asJsonObject)
            }
        }

        collectColors(component)
        return if (hexColors.size > 3) "<gradient:${hexColors.first().uppercase()}:${hexColors.last().uppercase()}>" else null
    }

    private fun extractText(obj: JsonObject): String = buildString {
        obj.get("text")?.asString?.let { append(it) }
        obj.get("extra")?.asJsonArray?.forEach {
            if (it.isJsonObject) append(extractText(it.asJsonObject))
        }
    }

    private fun appendColorCodes(obj: JsonObject): String = buildString {
        if (getBooleanValue(obj.get("bold"))) append("&l")
        if (getBooleanValue(obj.get("italic"))) append("&o")
        if (getBooleanValue(obj.get("underlined"))) append("&n")
        if (getBooleanValue(obj.get("strikethrough"))) append("&m")
        if (getBooleanValue(obj.get("obfuscated"))) append("&k")
    }

    private fun parseTextComponent(obj: JsonObject): String = buildString {
        detectGradient(obj)?.let { gradientCode ->
            // Apply formatting codes before the gradient
            append(appendColorCodes(obj))
            append(gradientCode).append(extractText(obj))
            return@buildString
        }

        append(formatColor(obj.get("color")?.asString))
        append(appendColorCodes(obj))
        append(obj.get("text")?.asString ?: "")

        obj.get("extra")?.asJsonArray?.forEach {
            if (it.isJsonObject) append(parseTextComponent(it.asJsonObject))
        }
    }

    private fun parseJsonStringAsTextComponent(jsonString: String): String {
        return try {
            val parsed = gson.fromJson(jsonString, JsonObject::class.java)
            parseTextComponent(parsed)
        } catch (e: Exception) {
            jsonString
        }
    }

    private fun parseNewNBTFormat(raw: String): String = buildString {
        val json = gson.fromJson(raw, JsonObject::class.java)
        val components = json.getAsJsonObject("components") ?: return@buildString

        val hasName = components.has("minecraft:custom_name")
        val hasLore = components.has("minecraft:lore")
        if (!hasName && !hasLore) return@buildString

        components.get("minecraft:custom_name")?.let { customNameElement ->
            append("Name: ")
            when {
                customNameElement.isJsonObject -> append(parseTextComponent(customNameElement.asJsonObject))
                customNameElement.isJsonPrimitive -> append(parseJsonStringAsTextComponent(customNameElement.asString))
                else -> append("Unknown format")
            }
            append("\n\n")
        }

        components.getAsJsonArray("minecraft:lore")?.let { lore ->
            append("Lore:\n")
            lore.forEachIndexed { index, line ->
                val parsed = when {
                    line.isJsonPrimitive && line.asString.isBlank() -> ""
                    line.isJsonObject -> parseTextComponent(line.asJsonObject)
                    line.isJsonPrimitive -> parseJsonStringAsTextComponent(line.asString)
                    else -> ""
                }
                append("Line $index: ").append(parsed).append("\n")
            }
        }
    }


    suspend fun saveFormattedNBTToFile(nbtList: List<String>, configDir: File) = withContext(Dispatchers.IO) {
        try {
            val start = LocalDateTime.now()
            val formattedTime = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val yoinkDir = File(configDir, "yoinkgui").apply { mkdirs() }
            val fileName = "formatted_nbt_${formattedTime}.txt"
            val file = File(yoinkDir, fileName)

            FileWriter(file).use { writer ->
                // keep the original index so we can reference the raw NBT correctly later
                val contentItems = nbtList.mapIndexedNotNull { i, raw ->
                    val formatted = parseNewNBTFormat(raw)
                    if (formatted.isNotBlank()) Pair(i, formatted) else null
                }

                writer.write("=== Formatted NBT Data ===\n")
                writer.write("Generated: $formattedTime\n")
                writer.write("Items with content: ${contentItems.size} / ${nbtList.size}\n\n")
                writer.write("=== Details ===\n")
                writer.write("Mod Version: ${BuildConfig.VERSION}\n")
                writer.write("Minecraft Version: ${BuildConfig.MC_VERSION}\n\n")

                contentItems.forEachIndexed { outIdx, (originalIndex, formatted) ->
                    writer.write("=== ITEM ${originalIndex + 1} ===\n")
                    writer.write("Raw NBT: ${nbtList[originalIndex]}\n")
                    writer.write("\n$formatted\n")
                    if (outIdx < contentItems.lastIndex) writer.write("\n${"=".repeat(50)}\n\n")
                }
            }

            val duration = Duration.between(start, LocalDateTime.now()).toMillis()

            try {
                Utils.sendChat(
                    "\n<color:#FFA6CA>Formatted NBT data saved to:".toComponent(),
                    " <color:#FFA6CA>Parse time: <color:#8968CD>${duration}ms".toComponent(),
                    "  <color:#8968CD>${file.absolutePath} &7&o(Click to copy)\n".toClickable(file.absolutePath)
                )
            } catch (inner: Exception) {
                logger?.error("Error while sending save notification to chat: ${inner.message}", inner)
            }
        } catch (e: Exception) {
            logger?.error("Error saving NBT file: ${e.message}", e)
        }
    }
}
