package me.thatonedevil.nbt.components

import com.google.gson.JsonObject

object TextHandler : ComponentValueHandler {
    override fun handle(obj: JsonObject): HandlerResult {
        return HandlerResult(
            obj.get("text")?.asString ?: ""
        )
    }
}