package me.thatonedevil.utils

import me.thatonedevil.YoinkGUIClient
import me.thatonedevil.utils.Utils.sendChat
import me.thatonedevil.utils.Utils.toClickCommand
import me.thatonedevil.utils.Utils.toClickCopy

object ErrorReporter {
    private const val CHAT_FOOTER = " &7&o(Report on github, Click to copy)"

    private fun sendDetailedChatMessage(chatContext: String) {
        sendChat("<color:#FF6961>$chatContext: $CHAT_FOOTER".toClickCommand("/yoinkguiclient debug"))
    }

    fun report(
        throwable: Throwable,
        context: String,
        chatContext: String = context,
        sendDetailedChat: Boolean = true
    ) {
        LatestErrorLog.record(throwable, context)

        if (sendDetailedChat) {
            sendDetailedChatMessage(chatContext)
        }

        YoinkGUIClient.logger.error("$context: ${throwable.stackTraceToString()}")
    }

    fun reportDebug(
        throwable: Throwable,
        context: String,
        debugMessage: String? = null,
        chatContext: String = context,
        sendDetailedChat: Boolean = false
    ) {
        LatestErrorLog.record(throwable, context)

        if (sendDetailedChat) {
            sendDetailedChatMessage(chatContext)
        }

        if (debugMessage != null) {
            YoinkGUIClient.logger.debug(debugMessage, throwable)
        } else {
            YoinkGUIClient.logger.debug(context, throwable)
        }
    }
}



