package me.thatonedevil.nbt

import com.google.gson.JsonElement
import com.google.gson.JsonObject

interface ComponentValueHandler {
    fun handle(obj: JsonObject): HandlerResult
}

data class HandlerResult(val text: String, val stopPropagation: Boolean = false)

object ComponentValueRegistry {
    private val handlers = mutableListOf<ComponentValueHandler>()

    init {
        // Registration order matters for the final string composition. Keep as close to original behaviour as possible.
        register(ColorHandler)
        register(StyleHandler)
        register(GradientHandler)
        register(TextHandler)
    }

    fun register(handler: ComponentValueHandler) {
        handlers.add(handler)
    }

    fun process(obj: JsonObject): HandlerResult {
        val builder = StringBuilder()
        handlers.forEach { h ->
            val res = h.handle(obj)
            if (res.text.isNotEmpty()) builder.append(res.text)
            if (res.stopPropagation) return HandlerResult(builder.toString(), true)
        }
        return HandlerResult(builder.toString(), false)
    }

    internal fun getBooleanValue(element: JsonElement?): Boolean {
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

    internal fun collectHexColors(component: JsonObject): List<String> {
        val hexColors = mutableListOf<String>()
        fun collect(obj: JsonObject) {
            obj.get("color")?.asString?.takeIf { it.startsWith("#") }?.let { hexColors.add(it) }
            obj.get("extra")?.asJsonArray?.forEach { el ->
                if (el.isJsonObject) collect(el.asJsonObject)
            }
        }
        collect(component)
        return hexColors
    }
}


object ColorHandler : ComponentValueHandler {
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

    override fun handle(obj: JsonObject): HandlerResult {
        val color = obj.get("color")?.asString
        return HandlerResult(formatColor(color))
    }
}

object StyleHandler : ComponentValueHandler {
    override fun handle(obj: JsonObject): HandlerResult {
        val b = StringBuilder()
        if (ComponentValueRegistry.getBooleanValue(obj.get("bold"))) b.append("&l")

        // Default to italic when the `italic` field is missing. If `italic` is present and
        val el = obj.get("italic")
        val isItalic = el == null || ComponentValueRegistry.getBooleanValue(el)
        if (isItalic) b.append("&o")

        if (ComponentValueRegistry.getBooleanValue(obj.get("underlined"))) b.append("&n")
        if (ComponentValueRegistry.getBooleanValue(obj.get("strikethrough"))) b.append("&m")
        if (ComponentValueRegistry.getBooleanValue(obj.get("obfuscated"))) b.append("&k")
        return HandlerResult(b.toString())
    }
}

object GradientHandler : ComponentValueHandler {
    override fun handle(obj: JsonObject): HandlerResult {
        val extra = obj.get("extra")
        if (extra == null || !extra.isJsonArray || extra.asJsonArray.size() < 4) return HandlerResult("")

        val hexColors = ComponentValueRegistry.collectHexColors(obj)
        if (hexColors.size > 3) {
            // Apply style codes before the gradient (to match previous behaviour)
            val styleCodes = StyleHandler.handle(obj).text
            val gradientCode = "<gradient:${hexColors.first().uppercase()}:${hexColors.last().uppercase()}>"

            // Build the full text by extracting text recursively from `extra` elements
            fun extractTextFrom(obj: JsonObject): String {
                val sb = StringBuilder()
                obj.get("text")?.asString?.let { sb.append(it) }
                obj.get("extra")?.asJsonArray?.forEach { el ->
                    if (el.isJsonObject) sb.append(extractTextFrom(el.asJsonObject))
                }
                return sb.toString()
            }

            val extracted = extractTextFrom(obj)
            return HandlerResult(styleCodes + gradientCode + extracted, true)
        }
        return HandlerResult("")
    }
}

object TextHandler : ComponentValueHandler {
    override fun handle(obj: JsonObject): HandlerResult {
        return HandlerResult(obj.get("text")?.asString ?: "")
    }
}
