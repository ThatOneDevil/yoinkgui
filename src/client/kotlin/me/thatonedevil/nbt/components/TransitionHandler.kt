package me.thatonedevil.nbt.components

import com.google.gson.JsonObject

object TransitionHandler : ComponentValueHandler {
    private val namedColors = setOf(
        "black",
        "dark_blue",
        "dark_green",
        "dark_aqua",
        "dark_red",
        "dark_purple",
        "gold",
        "gray",
        "dark_gray",
        "blue",
        "green",
        "aqua",
        "red",
        "light_purple",
        "yellow",
        "white"
    )

    override fun handle(obj: JsonObject): HandlerResult {
        if (obj.get("extra")?.isJsonArray != true) {
            return HandlerResult("")
        }

        val colors = collectColors(obj)
        if (colors.isEmpty()) {
            return HandlerResult("")
        }

        val phase = obj.get("phase")
            ?.takeIf { it.isJsonPrimitive }
            ?.asString
            ?.takeIf { it.toDoubleOrNull()?.let { value -> value in -1.0..1.0 } == true }

        val transition = buildString {
            append("<transition:")
            append(colors.joinToString(":") { color ->
                if (color.startsWith("#")) color.uppercase() else color.lowercase()
            })
            phase?.let { append(":").append(it) }
            append(">")
        }

        return HandlerResult(
            StyleHandler.handle(obj).text +
                    transition +
                    extractText(obj) +
                    "</transition>",
            stopPropagation = true
        )
    }

    private fun collectColors(component: JsonObject): List<String> {
        val colors = mutableListOf<String>()

        fun collect(obj: JsonObject) {
            obj.get("color")
                ?.takeIf { it.isJsonPrimitive }
                ?.asString
                ?.takeIf { it.startsWith("#") || it.lowercase() in namedColors }
                ?.let(colors::add)

            obj.get("extra")
                ?.takeIf { it.isJsonArray }
                ?.asJsonArray
                ?.forEach { element ->
                    if (element.isJsonObject) {
                        collect(element.asJsonObject)
                    }
                }
        }

        collect(component)
        return colors
    }

    private fun extractText(obj: JsonObject): String = buildString {
        obj.get("text")
            ?.takeIf { it.isJsonPrimitive }
            ?.asString
            ?.let(::append)

        obj.get("extra")
            ?.takeIf { it.isJsonArray }
            ?.asJsonArray
            ?.forEach { element ->
                if (element.isJsonObject) {
                    append(extractText(element.asJsonObject))
                }
            }
    }
}
