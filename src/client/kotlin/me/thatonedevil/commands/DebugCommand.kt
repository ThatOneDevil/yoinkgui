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
        val latestStacktrace = LatestErrorLog.getLatestStackTrace()

        val componentBuilder = Component.text()
            .append("&f--- <color:#FFA6CA>YoinkGUI Debug Info &f---\n".toComponent())
            .append(" <color:#8968CD>Game Version: &e$MC_VERSION\n".toComponent())
            .append(" <color:#8968CD>Mod Version: &e$VERSION\n".toComponent())
            .append(" <color:#8968CD>Java Version: &e${System.getProperty("java.version")}\n".toComponent())
            .append(Component.empty())
            .append("&f--- <color:#FFA6CA>YoinkGUI Error Info &f---\n".toComponent())
            .append(Component.empty())
            .append(" <color:#8968CD>Latest Error: &c${LatestErrorLog.getLatestMessage()}\n".toComponent())
            .append(" <color:#8968CD>Error Stacktrace: &c$latestStacktrace\n".toComponent())
            .append(Component.empty())
            .append("&7&o(Click to copy)".toComponent())
            .build()

        val deserialized = PlainTextComponentSerializer.plainText().serialize(componentBuilder)

        sendChat(componentBuilder.clickEvent(ClickEvent.copyToClipboard(deserialized)))
        return Command.SINGLE_SUCCESS
    }
}