package me.thatonedevil.nbt

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import me.thatonedevil.nbt.components.ComponentValueRegistry
import me.thatonedevil.utils.ErrorReporter

object NBTParser {
    private val gson = Gson()

    private const val MINECRAFT_CUSTOM_NAME = "minecraft:custom_name"
    private const val MINECRAFT_ITEM_NAME = "minecraft:item_name"
    private const val MINECRAFT_LORE = "minecraft:lore"
    private const val COMPONENTS_KEY = "components"

    data class ParsedItem(
        val raw: String,
        val formatted: String
    )

    private fun parseTextComponent(obj: JsonObject): String = buildString {
        val result = ComponentValueRegistry.process(obj)
        append(result.text)

        if (result.stopPropagation) {
            return@buildString
        }

        obj.get("extra")
            ?.asJsonArray
            ?.forEach { element ->
                if (element.isJsonObject) {
                    append(parseTextComponent(element.asJsonObject))
                }
            }
    }

    private fun parseJsonStringAsTextComponent(jsonString: String?): String {
        if (jsonString == null) {
            return ""
        }

        return try {
            val element = gson.fromJson(
                jsonString,
                com.google.gson.JsonElement::class.java
            )

            when {
                element == null -> ""
                element.isJsonPrimitive -> element.asString
                element.isJsonObject -> parseTextComponent(element.asJsonObject)
                else -> jsonString
            }
        } catch (e: JsonSyntaxException) {
            ErrorReporter.reportDebug(
                e,
                "Failed to parse JSON string as text component",
                "Failed to parse JSON string as text component: $jsonString"
            )

            jsonString
        }
    }

    private fun extractItemName(components: JsonObject): String {
        val nameElement = components.get(MINECRAFT_CUSTOM_NAME)
            ?: components.get(MINECRAFT_ITEM_NAME)
            ?: return "Unknown"

        return when {
            nameElement.isJsonObject ->
                parseTextComponent(nameElement.asJsonObject)

            nameElement.isJsonPrimitive ->
                parseJsonStringAsTextComponent(nameElement.asString)

            else -> "Unknown format"
        }
    }

    private fun extractItemLore(components: JsonObject): String? {
        val loreArray =
            components.getAsJsonArray(MINECRAFT_LORE) ?: return null

        return buildString {
            append("Lore:\n")

            loreArray.forEachIndexed { index, line ->
                val parsed = when {
                    line.isJsonPrimitive && line.asString.isBlank() -> ""
                    line.isJsonObject ->
                        parseTextComponent(line.asJsonObject)

                    line.isJsonPrimitive ->
                        parseJsonStringAsTextComponent(line.asString)

                    else -> ""
                }

                append("Line $index: $parsed\n")
            }
        }
    }

    fun parse(raw: String): String? {
        return try {
            val json = gson.fromJson(raw, JsonObject::class.java)

            val components =
                json.getAsJsonObject(COMPONENTS_KEY) ?: return null

            val hasName =
                components.has(MINECRAFT_CUSTOM_NAME) ||
                        components.has(MINECRAFT_ITEM_NAME)

            val hasLore = components.has(MINECRAFT_LORE)

            if (!hasName && !hasLore) {
                return null
            }

            buildString {
                if (hasName) {
                    append("Name: ${extractItemName(components)}\n\n")
                }

                extractItemLore(components)?.let(::append)
            }
        } catch (e: JsonSyntaxException) {
            ErrorReporter.reportDebug(
                e,
                "Failed to parse NBT format",
                "Failed to parse NBT format: $raw"
            )

            null
        }
    }

    fun parseItems(rawItems: List<String>): List<ParsedItem> {
        return rawItems.mapNotNull { raw ->
            parse(raw)?.let { formatted ->
                ParsedItem(
                    raw = raw,
                    formatted = formatted
                )
            }
        }
    }
}