package me.thatonedevil.nbt.components

import com.google.gson.JsonObject

object StyleHandler : ComponentValueHandler {
    override fun handle(obj: JsonObject): HandlerResult {
        val builder = StringBuilder()
        val legacy = isLegacyFormat()

        if (ComponentValueRegistry.getBooleanValue(obj.get("bold"))) {
            builder.append(if (legacy) "&l" else "<bold>")
        }

        if (ComponentValueRegistry.getBooleanValue(obj.get("italic"))) {
            builder.append(if (legacy) "&o" else "<italic>")
        }

        if (ComponentValueRegistry.getBooleanValue(obj.get("underlined"))) {
            builder.append(if (legacy) "&n" else "<underlined>")
        }

        if (ComponentValueRegistry.getBooleanValue(obj.get("strikethrough"))) {
            builder.append(if (legacy) "&m" else "<strikethrough>")
        }

        if (ComponentValueRegistry.getBooleanValue(obj.get("obfuscated"))) {
            builder.append(if (legacy) "&k" else "<obfuscated>")
        }

        return HandlerResult(builder.toString())
    }
}