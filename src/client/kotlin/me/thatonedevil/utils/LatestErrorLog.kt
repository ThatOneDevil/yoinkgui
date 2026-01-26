package me.thatonedevil.utils

import java.util.concurrent.atomic.AtomicReference

object LatestErrorLog {
    private val latest = AtomicReference<Throwable?>(null)
    private val latestMessage = AtomicReference<String?>(null)

    fun record(t: Throwable?, context: String? = null) {
        latest.set(t)
        latestMessage.set(context)
    }

    fun getLatestThrowable(): Throwable? = latest.get()
    fun getLatestMessage(): String? = latestMessage.get()

    fun getLatestStackTraceMessage(): String? = getLatestThrowable()?.stackTraceToString() ?: getLatestMessage()
    fun getLatestErrorName(): String? = getLatestThrowable()?.let { it::class.simpleName } ?: getLatestMessage()
}