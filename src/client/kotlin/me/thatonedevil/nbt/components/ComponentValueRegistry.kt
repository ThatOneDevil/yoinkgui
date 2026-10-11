package me.thatonedevil.nbt.components

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.utils.Utils.debug

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

        // Registration order matters.
        if (YoinkGuiSettings.toggleColorParser.get()) {
            register(ColorHandler)
        }

        if (YoinkGuiSettings.toggleStyleParser.get()) {
            register(StyleHandler)
        }

        if (YoinkGuiSettings.toggleShadowParser.get()) {
            register(ShadowHandler)
        }

        if (YoinkGuiSettings.toggleGradientParser.get()) {
            register(GradientHandler)
        }

        register(TransitionHandler)

        register(TextHandler)

        debug(
            "ComponentValueRegistry handlers refreshed. Current handlers: " +
                    handlers.map { it.name }
        )
    }

    fun process(obj: JsonObject): HandlerResult {
        val builder = StringBuilder()

        for (handler in handlers) {
            val result = handler.handle(obj)

            if (result.text.isNotEmpty()) {
                builder.append(result.text)
            }

            if (result.stopPropagation) {
                return HandlerResult(builder.toString(), true)
            }
        }

        return HandlerResult(builder.toString())
    }

    internal fun getBooleanValue(element: JsonElement?): Boolean {
        if (element == null || !element.isJsonPrimitive) {
            return false
        }

        val primitive = element.asJsonPrimitive

        return when {
            primitive.isBoolean -> primitive.asBoolean
            primitive.isNumber -> primitive.asNumber.toInt() == 1
            primitive.isString -> primitive.asString in listOf("true", "1", "1b")
            else -> false
        }
    }

    internal fun collectHexColors(component: JsonObject): List<String> {
        val hexColors = mutableListOf<String>()

        fun collect(obj: JsonObject) {
            obj.get("color")
                ?.asString
                ?.takeIf { it.startsWith("#") }
                ?.let(hexColors::add)

            obj.get("extra")
                ?.asJsonArray
                ?.forEach { element ->
                    if (element.isJsonObject) {
                        collect(element.asJsonObject)
                    }
                }
        }

        collect(component)

        return hexColors
    }
}

internal fun isLegacyFormat(): Boolean =
    YoinkGuiSettings.formatOption.get() == "LEGACY"
