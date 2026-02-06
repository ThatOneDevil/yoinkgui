package me.thatonedevil.commands

import com.mojang.brigadier.Command
import me.thatonedevil.BuildConfig.MC_VERSION
import me.thatonedevil.BuildConfig.RELEASE
import me.thatonedevil.BuildConfig.VERSION
import me.thatonedevil.utils.LatestErrorLog
import me.thatonedevil.utils.Utils.sendChat
import me.thatonedevil.utils.Utils.toClickCopy
import me.thatonedevil.utils.Utils.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class DebugCommand {
    fun execute(): Int {
        val latestStacktrace = LatestErrorLog.getLatestStackTraceMessage() ?: "No stacktrace available."
        val className = LatestErrorLog.getLatestErrorName() ?: "No error name available."

        val message = debugMessage(className, latestStacktrace)
        val plainText = PlainTextComponentSerializer.plainText().serialize(debugMessage(className, latestStacktrace, false))

        sendChat(message.toClickCopy(plainText))
        return Command.SINGLE_SUCCESS
    }

    private fun debugMessage(errorMessage: String, stackTrace: String, clickCopy: Boolean = true): Component {
        return Component.text()
            .append(Component.newline())
            .append("&f--- <color:#FFA6CA>YoinkGUI Debug Info &f---\n".toComponent())
            .append(" <color:#8968CD>Game Version: <color:#FFD4A3>$MC_VERSION\n".toComponent())
            .append(" <color:#8968CD>Mod Version: <color:#FFD4A3>$VERSION\n".toComponent())
            .append(" <color:#8968CD>Release Type: <color:#FFD4A3>${RELEASE}\n".toComponent())
            .append(" <color:#8968CD>Java Version: <color:#FFD4A3>${System.getProperty("java.version")}\n".toComponent())
            .append(Component.newline())
            .append("&f--- <color:#FFA6CA>YoinkGUI Error Info &f---\n".toComponent())
            .append(Component.newline())
            .append(" <color:#8968CD>Latest Error: <color:#FF6961>${errorMessage}\n".toComponent())
            .append(" <color:#8968CD>Error Stacktrace: <color:#FF6961>$stackTrace\n".toComponent())
            .also {
                if (clickCopy) {
                    it.append(Component.newline())
                    it.append("&7&o(Click to copy)".toComponent())
                }
            }
            .build()
    }
}