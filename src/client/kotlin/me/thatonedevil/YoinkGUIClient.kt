package me.thatonedevil

import me.thatonedevil.commands.YoinkGuiClientCommandRegistry
import me.thatonedevil.config.YoinkGuiSettings
import me.thatonedevil.handlers.SingleItemYoinkHandler
import me.thatonedevil.handlers.ParseButtonHandler
import me.thatonedevil.keybinds.KeybindManager
import me.thatonedevil.utils.api.UpdateChecker
import net.fabricmc.api.ClientModInitializer
import net.minecraft.util.Identifier
import net.minecraft.client.option.KeyBinding
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object YoinkGUIClient : ClientModInitializer {
    val logger: Logger = LoggerFactory.getLogger(BuildConfig.MOD_ID)

    @JvmStatic
    val yoinkGuiSettings = YoinkGuiSettings

    //? if >=1.21.9 {
    val keybindCategory: KeyBinding.Category = KeyBinding.Category.create(Identifier.of("keybinds"))
    //? }

    override fun onInitializeClient() {
        UpdateChecker.setupJoinListener()
        YoinkGuiClientCommandRegistry.register()
        KeybindManager().register()

        yoinkGuiSettings // Load settings on client init

        // Register event handlers
        ParseButtonHandler.register()
        SingleItemYoinkHandler.register()
    }
}