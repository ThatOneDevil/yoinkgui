package me.thatonedevil

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thatonedevil.YoinkGUIClient.logger
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.utils.Utils.toClickCopy
import me.thatonedevil.utils.Utils.toComponent
import me.thatonedevil.utils.Utils
import me.thatonedevil.nbt.ComponentValueRegistry
import me.thatonedevil.utils.LatestErrorLog
import me.thatonedevil.utils.api.UpdateChecker.serverName
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NBTParser {

    private val gson = Gson()
    private val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH-mm-ss")

    private fun parseTextComponent(obj: JsonObject): String = buildString {
        val result = ComponentValueRegistry.process(obj)
        append(result.text)
        if (result.stopPropagation) return@buildString

        obj.get("extra")?.asJsonArray?.forEach { element ->
            if (element.isJsonObject) {
                append(parseTextComponent(element.asJsonObject))
            }
        }
    }

    private fun parseJsonStringAsTextComponent(jsonString: String): String {
        return try {
            val parsed = gson.fromJson(jsonString, JsonObject::class.java)
            parseTextComponent(parsed)
        } catch (e: JsonSyntaxException) {
            logger.debug("Failed to parse JSON string as text component: $jsonString", e)
            jsonString
        }
    }

    private fun parseNewNBTFormat(raw: String): String? {
        return try {
            val json = gson.fromJson(raw, JsonObject::class.java)
            val components = json.getAsJsonObject("components") ?: return null

            val hasName = components.has("minecraft:custom_name") || components.has("minecraft:item_name")
            val hasLore = components.has("minecraft:lore")
            if (!hasName && !hasLore) return null

            buildString {
                // Parse name
                val nameElement = components.get("minecraft:custom_name")
                    ?: components.get("minecraft:item_name")
                nameElement?.let { customNameElement ->
                    append("Name: ")
                    when {
                        customNameElement.isJsonObject ->
                            append(parseTextComponent(customNameElement.asJsonObject))
                        customNameElement.isJsonPrimitive ->
                            append(parseJsonStringAsTextComponent(customNameElement.asString))
                        else -> append("Unknown format")
                    }
                    append("\n\n")
                }

                // Parse lore
                components.getAsJsonArray("minecraft:lore")?.let { lore ->
                    append("Lore:\n")
                    lore.forEachIndexed { index, line ->
                        val parsed = when {
                            line.isJsonPrimitive && line.asString.isBlank() -> ""
                            line.isJsonObject -> parseTextComponent(line.asJsonObject)
                            line.isJsonPrimitive -> parseJsonStringAsTextComponent(line.asString)
                            else -> ""
                        }
                        append("Line $index: $parsed\n")
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            logger.debug("Failed to parse NBT format: $raw", e)
            null
        }
    }

    private data class ParsedItem(val raw: String, val formatted: String)

    private suspend fun saveNbtFile(
        configDir: String,
        rawItems: List<String>
    ): Result<File> = withContext(Dispatchers.IO) {
        val start = LocalDateTime.now()
        val formattedTime = start.format(timeFormatter)

        try {
            val yoinkDir = File(configDir).apply {
                if (!exists() && !mkdirs()) {
                    throw IOException("Failed to create directory: $absolutePath")
                }
            }

            val file = File(yoinkDir, "${serverName}-${formattedTime}.txt")

            // Parse items before writing
            val items = rawItems.mapNotNull { raw ->
                parseNewNBTFormat(raw)?.let { formatted ->
                    ParsedItem(raw, formatted)
                }
            }

            // Write file
            file.bufferedWriter().use { writer ->
                writer.write("=== Formatted NBT Data ===\n")
                writer.write("Generated: $formattedTime\n")
                writer.write("Items with content: ${items.size} / ${rawItems.size}\n\n")

                writer.write("=== Details ===\n")
                writer.write("Mod Version: ${BuildConfig.VERSION}\n")
                writer.write("Minecraft Version: ${BuildConfig.MC_VERSION}\n\n")

                items.forEachIndexed { index, (raw, formatted) ->
                    writer.write("=== ITEM ${index + 1} ===\n")
                    if (YoinkGuiSettings.includeRawNbt.get()) {
                        writer.write("Raw NBT: $raw\n")
                    }

                    writer.write("\n$formatted\n")

                    if (index < items.lastIndex) {
                        writer.write("\n${"=".repeat(50)}\n\n")
                    }
                }
            }

            val duration = Duration.between(start, LocalDateTime.now()).toMillis()

            Utils.sendChat(
                "\n<color:#FFA6CA>Formatted NBT data saved to:".toComponent(),
                " <color:#FFA6CA>Parse time: <color:#8968CD>${duration}ms".toComponent(),
                "  <color:#8968CD>${file.absolutePath} &7&o(Click to copy)\n".toClickCopy(file.absolutePath)
            )

            Result.success(file)
        } catch (e: Exception) {
            LatestErrorLog.record(e, "Error saving NBT file to $configDir")
            logger.error("Error saving NBT file: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveFormattedNBTToFile(
        nbtList: List<String>,
        configDir: String
    ): Result<File> = saveNbtFile("$configDir/yoinkgui", nbtList)

    suspend fun saveSingleItem(
        rawNbt: String,
        configDir: String
    ): Result<File> = saveNbtFile("$configDir/yoinkgui/items", listOf(rawNbt))
}