package me.thatonedevil.utils

import me.thatonedevil.utils.Utils.toClickCommand
import me.thatonedevil.utils.Utils.toClickURL
import java.util.concurrent.atomic.AtomicReference

object LatestErrorLog {
    private val latest = AtomicReference<Throwable?>(null)
    private val latestMessage = AtomicReference<String?>(null)

    fun record(t: Throwable?, context: String? = null) {
        latest.set(t)
        latestMessage.set(context)

        val message = ("<color:#FF6961>[YoinkGUI] An error has occurred: ${getLatestErrorName()}.\n" +
                "Run /yoinkguiclient debug and report it on GitHub. &7&o(Click to run)")
            .toClickCommand("/yoinkguiclient debug")

        val githubLink = "&7&o(Report on GitHub)"
            .toClickURL("https://github.com/ThatOneDevil/yoinkgui/issues")

        Utils.sendChat(message, githubLink)
    }

    fun getLatestThrowable(): Throwable? = latest.get()
    fun getLatestMessage(): String? = latestMessage.get()

    fun getLatestStackTraceMessage(): String? = getLatestThrowable()?.stackTraceToString() ?: getLatestMessage()
    fun getLatestErrorName(): String? = getLatestThrowable()?.let { it::class.simpleName } ?: getLatestMessage()
}