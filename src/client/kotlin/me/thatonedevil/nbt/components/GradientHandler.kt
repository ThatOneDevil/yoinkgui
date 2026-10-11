package me.thatonedevil.nbt.components

import com.google.gson.JsonObject

object GradientHandler : ComponentValueHandler {
    override fun handle(obj: JsonObject): HandlerResult {
        val extra = obj.get("extra")

        if (
            extra == null ||
            !extra.isJsonArray ||
            extra.asJsonArray.size() < 4
        ) {
            return HandlerResult("")
        }

        val hexColors =
            ComponentValueRegistry.collectHexColors(obj)

        if (hexColors.size <= 3) {
            return HandlerResult("")
        }

        val styleCodes = StyleHandler.handle(obj).text

        val gradientCode =
            "<gradient:${hexColors.first().uppercase()}:" +
                    "${hexColors.last().uppercase()}>"

        return HandlerResult(
            styleCodes + gradientCode + extractText(obj),
            stopPropagation = true
        )
    }

    private fun extractText(obj: JsonObject): String {
        val builder = StringBuilder()

        obj.get("text")
            ?.asString
            ?.let(builder::append)

        obj.get("extra")
            ?.asJsonArray
            ?.forEach { element ->
                if (element.isJsonObject) {
                    builder.append(
                        extractText(element.asJsonObject)
                    )
                }
            }

        return builder.toString()
    }
}