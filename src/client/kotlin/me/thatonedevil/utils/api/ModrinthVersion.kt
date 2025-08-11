package me.thatonedevil.utils.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject

class ModrinthVersion(jsonElement: JsonElement) {
    var updateVersion: String? = null

    init {
        val json: JsonObject = jsonElement.asJsonObject
        this.updateVersion = json.get("version_number").asString.split("+").first() //gets the version number without the version
    }

    fun getUpdateLink(): String {
        return "https://modrinth.com/mod/yoinkgui/version/" + this.updateVersion
    }

}