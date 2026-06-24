package me.thatonedevil.utils

import me.thatonedevil.YoinkGUIClient.logger
import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.client.Minecraft
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences


object Utils {
    private val miniMessage = MiniMessage.miniMessage()

    private val audience = MinecraftClientAudiences.of().audience()

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

    fun String.toClickCopy(message: String): Component {
        return this.toComponent().clickEvent(ClickEvent.copyToClipboard(message))
    }
    fun Component.toClickCopy(message: String): Component {
        return this.clickEvent(ClickEvent.copyToClipboard(message))
    }

    fun String.toClickURL(message: String): Component {
        return this.toComponent().clickEvent(ClickEvent.openUrl(message))
    }
    fun String.toClickCommand(command: String): Component {
        return this.toComponent().clickEvent(ClickEvent.runCommand(command))
    }

    fun sendChat(message: String) {
        sendChat(message.toComponent())
    }

    fun sendChat(vararg messages: Component) {
        val mc = Minecraft.getInstance()
        if (mc.isSameThread) {
            messages.forEach { audience.sendMessage(it) }
        } else {
            mc.execute {
                messages.forEach { audience.sendMessage(it) }
            }
        }
    }

    fun debug(message: String) {
        if (yoinkGuiSettings.debugMode.get() == true) {
            logger.info(message)
        }
    }

}
