package me.thatonedevil.commands

import com.mojang.brigadier.Command
import me.thatonedevil.BuildConfig.MC_VERSION
import me.thatonedevil.BuildConfig.VERSION
import me.thatonedevil.utils.LatestErrorLog
import me.thatonedevil.utils.Utils.sendChat
import me.thatonedevil.utils.Utils.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class DebugCommand {
    fun execute(): Int {
        val latestStacktrace = LatestErrorLog.getLatestStackTraceMessage()
        val className = LatestErrorLog.getLatestErrorName()

        val errorInfo = className ?: latestStacktrace ?: "No error information available."
        val message = debugMessage(errorInfo)
        val plainText = PlainTextComponentSerializer.plainText().serialize(debugMessage(latestStacktrace ?: "No stacktrace available."))

        sendChat(message.clickEvent(ClickEvent.copyToClipboard(plainText)))
        return Command.SINGLE_SUCCESS
    }

    private fun debugMessage(error: String): Component {
        return Component.text()
            .append("&f--- <color:#FFA6CA>YoinkGUI Debug Info &f---\n".toComponent())
            .append(" <color:#8968CD>Game Version: &e$MC_VERSION\n".toComponent())
            .append(" <color:#8968CD>Mod Version: &e$VERSION\n".toComponent())
            .append(" <color:#8968CD>Java Version: &e${System.getProperty("java.version")}\n".toComponent())
            .append(Component.newline())
            .append("&f--- <color:#FFA6CA>YoinkGUI Error Info &f---\n".toComponent())
            .append(Component.newline())
            .append(" <color:#8968CD>Latest Error: &c${LatestErrorLog.getLatestMessage()}\n".toComponent())
            .append(" <color:#8968CD>Error Stacktrace: &c$error\n".toComponent())
            .append(Component.newline())
            .append("&7&o(Click to copy)".toComponent())
            .build()
    }
}