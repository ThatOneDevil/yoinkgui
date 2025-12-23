package me.thatonedevil

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thatonedevil.YoinkGUIClient.logger
import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import me.thatonedevil.utils.Utils.toClickable
import me.thatonedevil.utils.Utils.toComponent
import me.thatonedevil.utils.Utils
import me.thatonedevil.nbt.ComponentValueRegistry
import me.thatonedevil.utils.api.UpdateChecker.serverName
import net.minecraft.nbt.NbtElement
import java.io.File
import java.io.FileWriter
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NBTParser {

    private val gson = Gson()

    private fun parseTextComponent(obj: JsonObject): String = buildString {
        val result = ComponentValueRegistry.process(obj)
        if (result.text.isNotEmpty()) {
            append(result.text)
            if (result.stopPropagation) return@buildString
        }

        append(obj.get("text")?.asString ?: "")

        obj.get("extra")?.asJsonArray?.forEach {
            if (it.isJsonObject) append(parseTextComponent(it.asJsonObject))
        }
    }

    private fun parseJsonStringAsTextComponent(jsonString: String): String {
        return try {
            val parsed = gson.fromJson(jsonString, JsonObject::class.java)
            parseTextComponent(parsed)
        } catch (_: Exception) {
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
        val start = LocalDateTime.now()
        val formattedTime = start.format(DateTimeFormatter.ofPattern("MM-dd HH-mm-ss"))
        try {
            val yoinkDir = File(configDir, "yoinkgui").apply { mkdirs() }
            val fileName = "${serverName}-${formattedTime}.txt"
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
                logger.error("Error while sending save notification to chat: ${inner.message}", inner)
            }
        } catch (e: Exception) {
            logger.error("Error saving NBT file: ${e.message}", e)
        }
    }
}
