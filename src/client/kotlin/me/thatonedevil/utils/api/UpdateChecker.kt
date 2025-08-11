package me.thatonedevil.utils.api

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.thatonedevil.BuildConfig
import me.thatonedevil.utils.Utils.sendChat
import me.thatonedevil.utils.Utils.toClickURL
import me.thatonedevil.utils.Utils.toComponent
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents
import net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI

object UpdateChecker {

    private var currentUpdateVersion: ModrinthVersion? = null

    private suspend fun getUpdateVersion(): ModrinthVersion? {
        if (currentUpdateVersion != null) {
            return currentUpdateVersion
        }

        return withContext(Dispatchers.IO) {
            val latest = getLatestVersionFromModrinth()
            if (latest == null) {
                null
            } else if (latest.updateVersion?.equals(BuildConfig.VERSION) == true) {
                null
            } else {
                currentUpdateVersion = latest
                currentUpdateVersion
            }
        }
    }

    fun setupJoinListener() {
        ClientLoginConnectionEvents.INIT.register { _, _ ->
            runBlocking {
                getUpdateVersion()?.let { version ->
                    sendChat(
                        "\n<color:#FFA6CA>A new update is available: &m&c${version.updateVersion}&r &a${BuildConfig.VERSION}".toComponent(),
                        "<color:#8968CD>${version.getUpdateLink()} &7&o(Click to open)\n".toClickURL(version.getUpdateLink())
                    )
                } ?: run {
                    sendChat("&aYou are using the latest version of YoinkGUI!".toComponent())
                }
            }
        }
    }

    private fun getLatestVersionFromModrinth(): ModrinthVersion? {
        try {
            val url = URI("https://api.modrinth.com/v2/project/5j4oEPp2/version").toURL()
            val reader = BufferedReader(InputStreamReader(url.openStream()))
            val elements: JsonArray = Gson().fromJson(reader, JsonArray::class.java)
            val latestVersion: JsonElement = elements.get(0)
            return ModrinthVersion(latestVersion)
        } catch (e: IOException) {
            logger("&cChecking for update failed!")
        }
        return null
    }
}