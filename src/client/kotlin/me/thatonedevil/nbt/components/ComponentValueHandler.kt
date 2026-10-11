package me.thatonedevil.nbt.components

import com.google.gson.JsonObject

interface ComponentValueHandler {
    val name: String
        get() = javaClass.simpleName

    fun handle(obj: JsonObject): HandlerResult
}

data class HandlerResult(
    val text: String,
    val stopPropagation: Boolean = false
)