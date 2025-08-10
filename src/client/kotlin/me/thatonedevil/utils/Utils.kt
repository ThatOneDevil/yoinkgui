package me.thatonedevil.utils

import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage

object Utils {
    private val miniMessage = MiniMessage.miniMessage()
    private val client = MinecraftClientAudiences.of().audience();
    private val colorReplacements = mapOf(
        "&0" to "<black>", "&1" to "<dark_blue>", "&2" to "<dark_green>", "&3" to "<dark_aqua>",
        "&4" to "<dark_red>", "&5" to "<dark_purple>", "&6" to "<gold>", "&7" to "<gray>",
        "&8" to "<dark_gray>", "&9" to "<blue>", "&a" to "<green>", "&b" to "<aqua>",
        "&c" to "<red>", "&d" to "<light_purple>", "&e" to "<yellow>", "&f" to "<white>",
        "&l" to "<bold>", "&o" to "<italic>", "&n" to "<underlined>", "&m" to "<strikethrough>",
        "&r" to "<reset>"
    )

    private val colorRegex = Regex(colorReplacements.keys.joinToString("|") { Regex.escape(it) })

    private fun convertLegacyToMini(input: String): String {
        return colorRegex.replace(input) { match -> colorReplacements[match.value] ?: match.value }
    }

    fun String.toComponent(): Component {
        return miniMessage.deserialize(convertLegacyToMini(this))
    }

    fun String.toClickable(message: String): Component {
        return this.toComponent().clickEvent(ClickEvent.copyToClipboard(message))
    }
    fun String.toClickURL(message: String): Component {
        return this.toComponent().clickEvent(ClickEvent.openUrl(message))
    }

    fun sendChat(message: String) {
        client.sendMessage(message.toComponent())
    }

    fun sendChat(vararg messages: Component) {
        messages.forEach { message ->
            client.sendMessage(message)
        }
    }
}