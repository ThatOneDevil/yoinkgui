package me.thatonedevil.utils

import me.thatonedevil.utils.Utils.toClickCommand
import java.util.concurrent.atomic.AtomicReference

object LatestErrorLog {
    private val latest = AtomicReference<Throwable?>(null)
    private val latestMessage = AtomicReference<String?>(null)

    fun record(t: Throwable?, context: String? = null) {
        latest.set(t)
        latestMessage.set(context)
        Utils.sendChat(("<color:#FF6961>[YoinkGUI] An error has occurred: ${getLatestErrorName()}.\n" +
                "/yoinkguiclient debug and report it on GitHub. &7&o(Click to run)").toClickCommand("/yoinkguiclient debug"))
    }

    fun getLatestThrowable(): Throwable? = latest.get()
    fun getLatestMessage(): String? = latestMessage.get()

    fun getLatestStackTraceMessage(): String? = getLatestThrowable()?.stackTraceToString() ?: getLatestMessage()
    fun getLatestErrorName(): String? = getLatestThrowable()?.let { it::class.simpleName } ?: getLatestMessage()
}