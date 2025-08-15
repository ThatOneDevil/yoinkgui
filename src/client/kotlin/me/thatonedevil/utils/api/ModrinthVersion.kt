package me.thatonedevil.utils.api

import com.google.gson.JsonArray
import com.google.gson.JsonElement

class ModrinthVersion(jsonElement: JsonElement) {

    private val updateVersion: String
    private val versions: JsonArray

    val cleanVersion: String

    init {
        val json = jsonElement.asJsonObject
        val versionNumber = json.get("version_number").asString
        cleanVersion = versionNumber.substringBefore("+")
        updateVersion = versionNumber
        versions = json.get("game_versions").asJsonArray
    }

    fun getUpdateLink(): String = "https://modrinth.com/mod/yoinkgui/version/$updateVersion"

    fun supportsGameVersion(gameVersion: String): Boolean {
        return versions.any { it.asString == gameVersion }
    }
}