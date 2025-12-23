package me.thatonedevil.utils

//? if >1.21.1 {
import me.thatonedevil.YoinkGUIClient.logger
import me.thatonedevil.YoinkGUIClient.yoinkGuiSettings
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.client.MinecraftClient

//?} else {
/*import net.kyori.adventure.platform.fabric.FabricClientAudiences*/
//?}


object Utils {
    private val miniMessage = MiniMessage.miniMessage()

    //? if >1.21.1 {
    private val audience = MinecraftClientAudiences.of().audience()
    //?} else {
    /*private val audience = FabricClientAudiences.of().audience();*/
    //?}

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

    // Ensure message sending runs on the client/render thread
    fun sendChat(message: String) {
        val mc = MinecraftClient.getInstance()
        val action = Runnable { audience.sendMessage(message.toComponent()) }
        mc?.execute(action) ?: action.run()
    }

    fun sendChat(vararg messages: Component) {
        val mc = MinecraftClient.getInstance()
        val action = Runnable {
            for (component in messages) {
                audience.sendMessage(component)
            }
        }
        mc?.execute(action) ?: action.run()
    }

    fun debug(message: String) {
        if (yoinkGuiSettings.debugMode.get() == true) {
            logger.info(message)
        }
    }

}
