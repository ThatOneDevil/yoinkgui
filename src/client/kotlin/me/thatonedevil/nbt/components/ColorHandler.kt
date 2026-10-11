package me.thatonedevil.nbt.components

import com.google.gson.JsonObject

object ColorHandler : ComponentValueHandler {
    private val legacyColorCodes = mapOf(
        "black" to "&0",
        "dark_blue" to "&1",
        "dark_green" to "&2",
        "dark_aqua" to "&3",
        "dark_red" to "&4",
        "dark_purple" to "&5",
        "gold" to "&6",
        "gray" to "&7",
        "dark_gray" to "&8",
        "blue" to "&9",
        "green" to "&a",
        "aqua" to "&b",
        "red" to "&c",
        "light_purple" to "&d",
        "yellow" to "&e",
        "white" to "&f"
    )

    private val miniMessageColorCodes = mapOf(
        "black" to "<black>",
        "dark_blue" to "<dark_blue>",
        "dark_green" to "<dark_green>",
        "dark_aqua" to "<dark_aqua>",
        "dark_red" to "<dark_red>",
        "dark_purple" to "<dark_purple>",
        "gold" to "<gold>",
        "gray" to "<gray>",
        "dark_gray" to "<dark_gray>",
        "blue" to "<blue>",
        "green" to "<green>",
        "aqua" to "<aqua>",
        "red" to "<red>",
        "light_purple" to "<light_purple>",
        "yellow" to "<yellow>",
        "white" to "<white>"
    )

    private fun formatColor(color: String?): String {
        if (color == null) {
            return ""
        }

        val colorCodes = if (isLegacyFormat()) {
            legacyColorCodes
        } else {
            miniMessageColorCodes
        }

        colorCodes[color.lowercase()]?.let {
            return it
        }

        if (color.startsWith("#")) {
            return "<color:${color.uppercase()}>"
        }

        return ""
    }

    override fun handle(obj: JsonObject): HandlerResult {
        return HandlerResult(
            formatColor(obj.get("color")?.asString)
        )
    }
}