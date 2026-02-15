package me.thatonedevil.nbt

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thatonedevil.BuildConfig
import me.thatonedevil.YoinkGUIClient
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.utils.LatestErrorLog
import me.thatonedevil.utils.Utils
import me.thatonedevil.utils.Utils.toClickCopy
import me.thatonedevil.utils.Utils.toComponent
import me.thatonedevil.utils.api.UpdateChecker
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NBTParser {

    private val gson = Gson()
    private val timeFormatter = DateTimeFormatter.ofPattern("MM-dd HH-mm-ss")

    private const val MINECRAFT_CUSTOM_NAME = "minecraft:custom_name"
    private const val MINECRAFT_ITEM_NAME = "minecraft:item_name"
    private const val MINECRAFT_LORE = "minecraft:lore"
    private const val COMPONENTS_KEY = "components"

    private data class ParsedItem(
        val raw: String,
        val formatted: String
    )

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

    private fun parseJsonStringAsTextComponent(jsonString: String?): String {
        if (jsonString == null) return ""

        return try {
            val element = gson.fromJson(jsonString, com.google.gson.JsonElement::class.java)

            when {
                element == null -> ""
                element.isJsonPrimitive -> element.asString
                element.isJsonObject -> parseTextComponent(element.asJsonObject)
                else -> jsonString
            }
        } catch (e: JsonSyntaxException) {
            LatestErrorLog.record(e, "Failed to parse JSON string as text component")
            YoinkGUIClient.logger.debug("Failed to parse JSON string as text component: $jsonString", e)
            jsonString
        }
    }

    private fun extractItemName(components: JsonObject): String? {
        val nameElement = components.get(MINECRAFT_CUSTOM_NAME)
            ?: components.get(MINECRAFT_ITEM_NAME)
            ?: return "Unknown"

        return when {
            nameElement.isJsonObject -> parseTextComponent(nameElement.asJsonObject)
            nameElement.isJsonPrimitive -> parseJsonStringAsTextComponent(nameElement.asString)
            else -> "Unknown format"
        }
    }

    private fun extractItemLore(components: JsonObject): String? {
        val loreArray = components.getAsJsonArray(MINECRAFT_LORE) ?: return null

        return buildString {
            append("Lore:\n")
            loreArray.forEachIndexed { index, line ->
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

    private fun parseNewNBTFormat(raw: String): String? {
        return try {
            val json = gson.fromJson(raw, JsonObject::class.java)
            val components = json.getAsJsonObject(COMPONENTS_KEY) ?: return null

            val hasName = components.has(MINECRAFT_CUSTOM_NAME) || components.has(MINECRAFT_ITEM_NAME)
            val hasLore = components.has(MINECRAFT_LORE)

            if (!hasName && !hasLore) return null

            buildString {
                extractItemName(components)?.let { name ->
                    append("Name: $name\n\n")
                }

                extractItemLore(components)?.let { lore ->
                    append(lore)
                }
            }
        } catch (e: JsonSyntaxException) {
            LatestErrorLog.record(e, "Failed to parse NBT format")
            YoinkGUIClient.logger.debug("Failed to parse NBT format: $raw", e)
            null
        }
    }

    private fun parseItems(rawItems: List<String>): List<ParsedItem> {
        return rawItems.mapNotNull { raw ->
            parseNewNBTFormat(raw)?.let { formatted ->
                ParsedItem(raw, formatted)
            }
        }
    }

    private fun writeFileHeader(
        writer: BufferedWriter,
        formattedTime: String,
        itemCount: Int,
        totalCount: Int
    ) {
        writer.write("=== Formatted NBT Data ===\n")
        writer.write("Generated: $formattedTime\n")
        writer.write("Items with content: $itemCount / $totalCount\n\n")
        writer.write("=== Details ===\n")
        writer.write("Mod Version: ${BuildConfig.VERSION}\n")
        writer.write("Minecraft Version: ${BuildConfig.MC_VERSION}\n\n")
    }

    private fun writeItem(
        writer: BufferedWriter,
        index: Int,
        item: ParsedItem,
        includeRawNbt: Boolean,
        isLastItem: Boolean
    ) {
        writer.write("=== ITEM ${index + 1} ===\n")

        if (includeRawNbt) {
            writer.write("Raw NBT: ${item.raw}\n")
        }

        writer.write("\n${item.formatted}\n")

        if (!isLastItem) {
            writer.write("\n${"=".repeat(50)}\n\n")
        }
    }

    private fun ensureDirectoryExists(dirPath: String): File {
        return File(dirPath).apply {
            if (!exists() && !mkdirs()) {
                throw IOException("Failed to create directory: $absolutePath")
            }
        }
    }

    private fun generateFilename(formattedTime: String): String {
        return "${UpdateChecker.serverName}-${formattedTime}.txt"
    }

    private fun sendSuccessMessage(file: File, duration: Long) {
        Utils.sendChat(
            "\n<color:#FFA6CA>Formatted NBT data saved to:".toComponent(),
            " <color:#FFA6CA>Parse time: <color:#8968CD>${duration}ms".toComponent(),
            "  <color:#8968CD>${file.absolutePath} &7&o(Click to copy)\n".toClickCopy(file.absolutePath)
        )
    }

    private suspend fun saveNbtFile(
        configDir: String,
        rawItems: List<String>
    ): Result<File> = withContext(Dispatchers.IO) {
        val start = LocalDateTime.now()
        val formattedTime = start.format(timeFormatter)

        try {
            val yoinkDir = ensureDirectoryExists(configDir)

            val file = File(yoinkDir, generateFilename(formattedTime))

            val items = parseItems(rawItems)

            file.bufferedWriter().use { writer ->
                writeFileHeader(writer, formattedTime, items.size, rawItems.size)

                items.forEachIndexed { index, item ->
                    writeItem(
                        writer = writer,
                        index = index,
                        item = item,
                        includeRawNbt = YoinkGuiSettings.includeRawNbt.get(),
                        isLastItem = index == items.lastIndex
                    )
                }
            }

            val duration = Duration.between(start, LocalDateTime.now()).toMillis()
            sendSuccessMessage(file, duration)

            Result.success(file)
        } catch (e: Exception) {
            LatestErrorLog.record(e, "Error saving NBT file to $configDir")
            YoinkGUIClient.logger.error("Error saving NBT file: ${e.message}", e)
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