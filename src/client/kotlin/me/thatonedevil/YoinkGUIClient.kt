package me.thatonedevil

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.thatonedevil.commands.YoinkGuiClientCommandRegistry
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.gui.ButtonPositionScreen
import me.thatonedevil.inventory.TopInventory
import me.thatonedevil.inventory.YoinkInventory
import me.thatonedevil.keybinds.KeybindManager
import me.thatonedevil.utils.LatestErrorLog
import me.thatonedevil.utils.Utils.sendChat
import me.thatonedevil.utils.Utils.toClickCopy
import me.thatonedevil.utils.api.UpdateChecker
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object YoinkGUIClient : ClientModInitializer {
    var parseButtonHovered = false
    var wasLeftClicking = false
    private var hoveredItemStack: ItemStack? = null

    val logger: Logger = LoggerFactory.getLogger(BuildConfig.MOD_ID)

    @JvmStatic
    val yoinkGuiSettings = YoinkGuiSettings

    override fun onInitializeClient() {
        UpdateChecker.setupJoinListener()
        YoinkGuiClientCommandRegistry.register()
        KeybindManager().register()

        yoinkGuiSettings // Load settings on client init

        // Button click logic.
        ClientTickEvents.END_CLIENT_TICK.register { client ->

            val isLeftClicking = GLFW.glfwGetMouseButton(client.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

            if (client.currentScreen == null) {
                return@register
            }
            if (client.currentScreen is ButtonPositionScreen) {
                return@register
            }

            if (client.player != null && isLeftClicking && !wasLeftClicking) {
                when {
                    parseButtonHovered -> handleParseButton(client)
                }
            }

            wasLeftClicking = isLeftClicking
        }
    }

    fun handleParseButton(client: MinecraftClient) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val player = client.player ?: return@launch
                val configDir = client.runDirectory.resolve("config")
                val yoinkInventory = YoinkInventory(player, TopInventory(client))
                val yoinkedItems = yoinkInventory.apply { yoinkItems() }.getYoinkedItems().map { it.toString() }

                if (yoinkedItems.isEmpty()) {
                    sendChat("<color:#FF6961>Inventory is empty!")
                    return@launch
                }

                NBTParser.saveFormattedNBTToFile(yoinkedItems, configDir)

            } catch (e: Exception) {
                LatestErrorLog.record(e, "Error during NBT parsing")
                sendChat("<color:#FF6961>Error during NBT parsing: ${e.message} &7&o(Report on github, Click to copy)".toClickCopy(e.message.toString()))
                logger.error("Error during NBT parsing: ${e.stackTraceToString()}")

            }
        }
    }

    //temporary
    @JvmStatic
    fun getHoveredItemStack(): ItemStack? {
        return hoveredItemStack
    }


    @JvmStatic
    fun setHoveredItemStack(itemStack: ItemStack?) {
        hoveredItemStack = itemStack
    }
}