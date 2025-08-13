package me.thatonedevil

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.thatonedevil.YoinkGUI.logger
import me.thatonedevil.inventory.TopInventory
import me.thatonedevil.inventory.YoinkInventory
import me.thatonedevil.utils.Utils.sendChat
import me.thatonedevil.utils.Utils.toClickable
import me.thatonedevil.utils.Utils.toComponent
import me.thatonedevil.utils.api.UpdateChecker
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

object YoinkGUIClient : ClientModInitializer {

    private var buttonHovered = false
    var parseButtonHovered = false
    private var wasLeftClicking = false

    override fun onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val isLeftClicking = GLFW.glfwGetMouseButton(client.window.handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

            if (client.player != null && isLeftClicking && !wasLeftClicking) {
                when {
                    buttonHovered -> handleYoinkButton(client)
                    parseButtonHovered -> handleParseButton(client)
                }
            }

            wasLeftClicking = isLeftClicking
        }

        UpdateChecker.setupJoinListener()
    }

    private fun handleYoinkButton(client: MinecraftClient) {
        val yoinkInventory = YoinkInventory(client.player!!, TopInventory(client))
        yoinkInventory.yoinkItems()
        logger.info("Yoinked Items: ${yoinkInventory.getYoinkedItems()}")
    }

    private fun handleParseButton(client: MinecraftClient) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val player = client.player ?: return@launch
                val configDir = client.runDirectory.resolve("config")
                val yoinkedItems = YoinkInventory(player, TopInventory(client)).apply { yoinkItems() }.getYoinkedItems().map { it.toString() }

                if (yoinkedItems.isEmpty()) {
                    client.execute {
                        sendChat("<color:#FF6961>Inventory is empty!".toComponent())
                    }
                    return@launch
                }

                NBTParser.saveFormattedNBTToFile(yoinkedItems, configDir)

            } catch (e: Exception) {
                client.execute {
                    sendChat("<color:#FF6961>Error during NBT parsing: ${e.message} ()".toClickable(e.message.toString()))
                    logger.error("Error during NBT parsing: ${e.stackTraceToString()}")
                }
            }
        }
    }
}