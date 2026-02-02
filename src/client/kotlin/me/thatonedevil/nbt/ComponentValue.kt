package me.thatonedevil.nbt

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.utils.Utils.debug

interface ComponentValueHandler {
    val name: String
        get() = this.javaClass.simpleName
    fun handle(obj: JsonObject): HandlerResult
}

data class HandlerResult(val text: String, val stopPropagation: Boolean = false)

object ComponentValueRegistry {
    private val handlers = mutableListOf<ComponentValueHandler>()

    init {
        refreshHandlers()
    }

    fun register(handler: ComponentValueHandler) {
        handlers.add(handler)
    }

    fun refreshHandlers() {
        handlers.clear()
        // Registration order matters for the final string composition. Keep as close to original behaviour as possible.
        if (YoinkGuiSettings.toggleColorParser.get()) { register(ColorHandler) }
        if (YoinkGuiSettings.toggleStyleParser.get()) { register(StyleHandler) }
        if (YoinkGuiSettings.toggleShadowParser.get()) { register(ShadowHandler) }
        if (YoinkGuiSettings.toggleGradientParser.get()) { register(GradientHandler) }
        register(TextHandler)

        debug("ComponentValueRegistry handlers refreshed. Current handlers: ${handlers.map { it.name }}")
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

private fun isLegacyFormat(): Boolean = YoinkGuiSettings.formatOption.get() == "LEGACY"

object ColorHandler : ComponentValueHandler {
    private val LEGACY_COLOR_CODES = mapOf(
        "black" to "&0", "dark_blue" to "&1", "dark_green" to "&2", "dark_aqua" to "&3",
        "dark_red" to "&4", "dark_purple" to "&5", "gold" to "&6", "gray" to "&7",
        "dark_gray" to "&8", "blue" to "&9", "green" to "&a", "aqua" to "&b",
        "red" to "&c", "light_purple" to "&d", "yellow" to "&e", "white" to "&f"
    )

    private val MINIMESSAGE_COLOR_CODES = mapOf(
        "black" to "<black>", "dark_blue" to "<dark_blue>", "dark_green" to "<dark_green>", "dark_aqua" to "<dark_aqua>",
        "dark_red" to "<dark_red>", "dark_purple" to "<dark_purple>", "gold" to "<gold>", "gray" to "<gray>",
        "dark_gray" to "<dark_gray>", "blue" to "<blue>", "green" to "<green>", "aqua" to "<aqua>",
        "red" to "<red>", "light_purple" to "<light_purple>", "yellow" to "<yellow>", "white" to "<white>"
    )

    private fun formatColor(color: String?): String {
        if (color == null) return ""
        val lower = color.lowercase()
        val map = if (isLegacyFormat()) LEGACY_COLOR_CODES else MINIMESSAGE_COLOR_CODES

        // Named color
        map[lower]?.let { return it }

        if (color.startsWith("#")) {
            return if (isLegacyFormat()) {
                // Legacy can't represent hex reliably and messyly, so skip
                ""
            } else {
                "<color:${color.uppercase()}>"
            }
        }

        return ""
    }

    override fun handle(obj: JsonObject): HandlerResult {
        val color = obj.get("color")?.asString
        return HandlerResult(formatColor(color))
    }
}

object StyleHandler : ComponentValueHandler {
    override fun handle(obj: JsonObject): HandlerResult {
        val b = StringBuilder()
        val legacy = isLegacyFormat()

        if (ComponentValueRegistry.getBooleanValue(obj.get("bold"))) b.append(if (legacy) "&l" else "<bold>")

        val isItalic = ComponentValueRegistry.getBooleanValue(obj.get("italic"))
        if (isItalic) b.append(if (legacy) "&o" else "<italic>")

        if (ComponentValueRegistry.getBooleanValue(obj.get("underlined"))) b.append(if (legacy) "&n" else "<underlined>")
        if (ComponentValueRegistry.getBooleanValue(obj.get("strikethrough"))) b.append(if (legacy) "&m" else "<strikethrough>")
        if (ComponentValueRegistry.getBooleanValue(obj.get("obfuscated"))) b.append(if (legacy) "&k" else "<obfuscated>")

        return HandlerResult(b.toString())
    }
}

object ShadowHandler : ComponentValueHandler {
    private val namedColors = mapOf(
        0x000000 to "black", 0x0000AA to "dark_blue", 0x00AA00 to "dark_green", 0x00AAAA to "dark_aqua",
        0xAA0000 to "dark_red", 0xAA00AA to "dark_purple", 0xFFAA00 to "gold", 0xAAAAAA to "gray",
        0x555555 to "dark_gray", 0x5555FF to "blue", 0x55FF55 to "green", 0x55FFFF to "aqua",
        0xFF5555 to "red", 0xFF55FF to "light_purple", 0xFFFF55 to "yellow", 0xFFFFFF to "white"
    )

    override fun handle(obj: JsonObject): HandlerResult {
        val shadowColorElement = obj.get("shadow_color") ?: return HandlerResult("")

        if (!shadowColorElement.isJsonPrimitive) return HandlerResult("")

        // shadow_color is a signed 32-bit int representing ARGB
        val shadowColorInt = shadowColorElement.asInt

        // Extract ARGB components
        val alpha = (shadowColorInt shr 24) and 0xFF
        val red = (shadowColorInt shr 16) and 0xFF
        val green = (shadowColorInt shr 8) and 0xFF
        val blue = shadowColorInt and 0xFF

        // Convert alpha from 0-255 to 0.0-1.0
        val alphaDecimal = String.format("%.2f", alpha / 255.0).trimEnd('0').trimEnd('.')

        // Check if RGB matches a named color
        val rgbInt = (red shl 16) or (green shl 8) or blue
        val colorString = namedColors[rgbInt] ?: String.format("%02X%02X%02X", red, green, blue).lowercase()

        return HandlerResult("<shadow:$colorString:$alphaDecimal>")
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
