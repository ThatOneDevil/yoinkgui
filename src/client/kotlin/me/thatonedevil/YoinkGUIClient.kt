package me.thatonedevil

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

object YoinkGUIClient : ClientModInitializer {

    var buttonHovered = false
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
    }

    private fun handleYoinkButton(client: MinecraftClient) {
        val yoinkInventory = YoinkInventory(client.player!!, TopInventory(client))
        yoinkInventory.yoinkItems()
        println("Yoinked Items: ${yoinkInventory.getYoinkedItems()}")
    }

    private fun handleParseButton(client: MinecraftClient) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val player = client.player!!
                val yoinkInventory = YoinkInventory(player, TopInventory(client))
                yoinkInventory.yoinkItems()

                val yoinkedItems = yoinkInventory.getYoinkedItems().map { it.toString() }
                val configDir = client.runDirectory.resolve("config")

                client.execute {
                    println("Starting NBT parsing for ${yoinkedItems.size} items...")
                }

                NBTParser.saveFormattedNBTToFile(yoinkedItems, configDir)

                client.execute {
                    println("NBT parsing completed successfully!")
                }
            } catch (e: Exception) {
                client.execute {
                    println("Error during NBT parsing: ${e.message}")
                }
            }
        }
    }
}