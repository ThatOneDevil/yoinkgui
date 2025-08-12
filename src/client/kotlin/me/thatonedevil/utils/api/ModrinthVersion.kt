package me.thatonedevil.utils.api

import com.google.gson.JsonElement

class ModrinthVersion(jsonElement: JsonElement) {

    val updateVersion: String
    val cleanVersion: String

    init {
        val json = jsonElement.asJsonObject
        val versionNumber = json.get("version_number").asString
        cleanVersion = versionNumber.substringBefore("+")
        updateVersion = versionNumber
    }

    fun getUpdateLink(): String = "https://modrinth.com/mod/yoinkgui/version/$updateVersion"

}