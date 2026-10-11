package me.thatonedevil.nbt.components

import com.google.gson.JsonObject

object ShadowHandler : ComponentValueHandler {
    private val namedColors = mapOf(
        0x000000 to "black",
        0x0000AA to "dark_blue",
        0x00AA00 to "dark_green",
        0x00AAAA to "dark_aqua",
        0xAA0000 to "dark_red",
        0xAA00AA to "dark_purple",
        0xFFAA00 to "gold",
        0xAAAAAA to "gray",
        0x555555 to "dark_gray",
        0x5555FF to "blue",
        0x55FF55 to "green",
        0x55FFFF to "aqua",
        0xFF5555 to "red",
        0xFF55FF to "light_purple",
        0xFFFF55 to "yellow",
        0xFFFFFF to "white"
    )

    override fun handle(obj: JsonObject): HandlerResult {
        val shadowColorElement =
            obj.get("shadow_color") ?: return HandlerResult("")

        if (!shadowColorElement.isJsonPrimitive) {
            return HandlerResult("")
        }

        val shadowColor = shadowColorElement.asInt

        val alpha = (shadowColor shr 24) and 0xFF
        val red = (shadowColor shr 16) and 0xFF
        val green = (shadowColor shr 8) and 0xFF
        val blue = shadowColor and 0xFF

        val alphaDecimal = String.format(
            "%.2f",
            alpha / 255.0
        ).trimEnd('0').trimEnd('.')

        val rgb = (red shl 16) or (green shl 8) or blue

        val color = namedColors[rgb]
            ?: String.format(
                "%02X%02X%02X",
                red,
                green,
                blue
            ).lowercase()

        return HandlerResult(
            "<shadow:#$color:$alphaDecimal>"
        )
    }
}