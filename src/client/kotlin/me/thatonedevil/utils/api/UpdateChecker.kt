package me.thatonedevil.utils.api

import com.google.gson.Gson
import com.google.gson.JsonArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.thatonedevil.BuildConfig
import me.thatonedevil.YoinkGUIClient.logger
import me.thatonedevil.utils.LatestErrorLog
import me.thatonedevil.utils.Utils.debug
import me.thatonedevil.utils.Utils.sendChat
import me.thatonedevil.utils.Utils.toClickCommand
import me.thatonedevil.utils.Utils.toClickURL
import me.thatonedevil.utils.Utils.toComponent
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI

object UpdateChecker {

    var serverName: String? = "Unknown"
    var currentUpdateVersion: ModrinthVersion? = null

    suspend fun getUpdateVersion(): ModrinthVersion? {
        if (currentUpdateVersion != null) {
            return currentUpdateVersion
        }

        return withContext(Dispatchers.IO) {
            val latest = getLatestVersionFromModrinth()
            if (latest == null) {
                null
            } else if (latest.cleanVersion == BuildConfig.VERSION) {
                null
            } else {
                currentUpdateVersion = latest
                currentUpdateVersion
            }
        }
    }

    fun setupJoinListener() {
        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            serverName = when (client.currentServerEntry?.address){
                "0", "localhost" -> "Singleplayer"
                else -> client.currentServerEntry?.address ?: "Singleplayer"
            }

            debug("Server name: $serverName")

            checkVersion()
        }
    }

    fun checkVersion(){
        CoroutineScope(Dispatchers.IO).launch {
            getUpdateVersion()?.let { version ->
                sendChat(
                    "\n<color:#FFA6CA>A new update is available: &m&c${BuildConfig.VERSION}&r &a${version.cleanVersion}".toComponent(),
                    "<color:#8968CD>${version.getUpdateLink()}\n&7&o(Click to open)\n".toClickURL(version.getUpdateLink())
                )
            } ?: run {
                sendChat("<color:#77DD77>You have the latest version of YoinkGUI! &7&o(Click to open changelog)".toClickCommand("/yoinkguiclient changelog"))
            }
        }
    }

    private fun getLatestVersionFromModrinth(): ModrinthVersion? {
        try {
            val url = URI("https://api.modrinth.com/v2/project/5j4oEPp2/version").toURL()
            val reader = BufferedReader(InputStreamReader(url.openStream()))
            val elements: JsonArray = Gson().fromJson(reader, JsonArray::class.java)

            for (element in elements) {
                val version = ModrinthVersion(element)
                if (version.supportsGameVersion(BuildConfig.MC_VERSION)) {
                    debug("Found compatible version: ${version.cleanVersion} for MC ${BuildConfig.MC_VERSION}")
                    return version
                }
            }

            logger.error("No compatible version found for MC ${BuildConfig.MC_VERSION}")
            return null
        } catch (error: IOException) {
            LatestErrorLog.record(error, "Update Check Failure")
            logger.error("Checking for update failed!")
        }
        return null
    }
}