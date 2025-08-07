package me.thatonedevil

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonElement
import jdk.jshell.execution.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thatonedevil.Utils.toClickable
import me.thatonedevil.Utils.toComponent
import net.minecraft.client.network.ClientPlayerEntity
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
        return colorCodes[lower] ?: if (color.startsWith("#")) {
            "<color:${color.uppercase()}>"
        } else ""
    }

    private fun getBooleanValue(element: JsonElement?): Boolean {
        return when {
            element == null -> false
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asNumber.toInt() == 1
                    primitive.isString -> {
                        val str = primitive.asString
                        str == "true" || str == "1" || str == "1b"
                    }
                    else -> false
                }
            }
            else -> false
        }
    }

    private fun detectGradient(component: JsonObject): String? {
        if (!component.has("extra")) return null
        if (component.getAsJsonArray("extra").size() < 4) return null

        val hexColors = mutableListOf<String>()

        fun collectHexColors(comp: JsonObject) {
            comp.get("color")?.asString?.let { color ->
                if (color.startsWith("#")) hexColors.add(color)
            }
            comp.get("extra")?.asJsonArray?.forEach { element ->
                if (element.isJsonObject) collectHexColors(element.asJsonObject)
            }
        }

        collectHexColors(component)

        return if (hexColors.size > 3) {
            "<gradient:${hexColors.first().uppercase()}:${hexColors.last().uppercase()}>"
        } else null
    }

    private fun extractText(comp: JsonObject): String = buildString {
        comp.get("text")?.asString?.let { append(it) }
        comp.get("extra")?.asJsonArray?.forEach { element ->
            if (element.isJsonObject) append(extractText(element.asJsonObject))
        }
    }

    private fun parseTextComponent(component: JsonObject): String = buildString {
        detectGradient(component)?.let { gradient ->
            append(gradient)
            append(extractText(component))
            return@buildString
        }

        val text = component.get("text")?.asString ?: ""
        val color = component.get("color")?.asString
        val bold = getBooleanValue(component.get("bold"))
        val italic = getBooleanValue(component.get("italic"))
        val underlined = getBooleanValue(component.get("underlined"))
        val strikethrough = getBooleanValue(component.get("strikethrough"))

        append(formatColor(color))
        if (bold) append("&l")
        if (italic) append("&o")
        if (underlined) append("&n")
        if (strikethrough) append("&m")
        append(text)

        component.get("extra")?.asJsonArray?.forEach { element ->
            if (element.isJsonObject) {
                append(parseTextComponent(element.asJsonObject))
            }
        }
    }

    private fun parseNewNBTFormat(raw: String): String = buildString {
        try {
            val nbtJson = gson.fromJson(raw, JsonObject::class.java)
            val components = nbtJson.getAsJsonObject("components") ?: return ""

            // Check if item has custom_name or lore, skip if neither exists
            val hasCustomName = components.has("minecraft:custom_name")
            val hasLore = components.has("minecraft:lore")

            if (!hasCustomName && !hasLore) return ""

            components.getAsJsonObject("minecraft:custom_name")?.let { customName ->
                append("Name: ").append(parseTextComponent(customName)).append("\n\n")
            }

            components.getAsJsonArray("minecraft:lore")?.let { lore ->
                append("Lore:\n")
                lore.forEachIndexed { i, element ->
                    val parsedLine = when {
                        element.isJsonPrimitive && element.asString.isEmpty() -> ""
                        element.isJsonObject -> parseTextComponent(element.asJsonObject)
                        element.isJsonPrimitive -> element.asString
                        else -> ""
                    }
                    append("Line $i: ").append(parsedLine).append("\n")
                }
            }
        } catch (e: Exception) {
            return "Error parsing new NBT format: ${e.message}\n"
        }
    }

    private fun parseNBTToString(raw: String): String = buildString {
        try {
            append(parseNewNBTFormat(raw))
        } catch (e: Exception) {
            append("Error parsing NBT: ${e.message}\n")
        }
    }

    suspend fun saveFormattedNBTToFile(player: ClientPlayerEntity, nbtDataList: List<String>, configDir: File) = withContext(Dispatchers.IO) {
        try {
            val time = LocalDateTime.now()
            val yoinkDir = File(configDir, "yoinkgui").apply { mkdirs() }
            val timestamp = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val file = File(yoinkDir, "formatted_nbt_$timestamp.txt")

            FileWriter(file).use { writer ->
                val itemsWithContent = mutableListOf<Pair<Int, String>>()

                // First pass: collect items that have formatted content
                nbtDataList.forEachIndexed { index, nbtData ->
                    val formatted = parseNBTToString(nbtData)
                    if (formatted.isNotBlank()) {
                        itemsWithContent.add(index + 1 to nbtData)
                    }
                }

                writer.write("=== Formatted NBT Data ===\n")
                writer.write("Generated: ${time}\n")
                writer.write("Total Items with content: ${itemsWithContent.size} (out of ${nbtDataList.size})\n\n")

                itemsWithContent.forEachIndexed { displayIndex, (originalIndex, nbtData) ->
                    writer.write("=== ITEM $originalIndex ===\n")
                    writer.write("Raw NBT:\n$nbtData\n\n")
                    writer.write("Formatted:\n${parseNBTToString(nbtData)}\n")
                    if (displayIndex < itemsWithContent.size - 1) {
                        writer.write("\n${"=".repeat(50)}\n\n")
                    }
                }
            }

            val parseDuration = Duration.between(time, LocalDateTime.now()).toMillis()
            Utils.sendChat(
                "\n<color:#FFA6CA>Formatted NBT data saved to:".toComponent(),
                " <color:#FFA6CA>Parse time: <color:#8968CD>${parseDuration}ms".toComponent(),
                "  <color:#8968CD>${file.absolutePath} &7&o(Click to copy)\n".toClickable(file.absolutePath.toString()))
        } catch (e: Exception) {
            println("Error saving NBT file: ${e.message}")
        }
    }
}