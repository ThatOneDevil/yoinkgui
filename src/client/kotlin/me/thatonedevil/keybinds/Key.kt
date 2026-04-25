package me.thatonedevil.keybinds

import me.thatonedevil.YoinkGUIClient.keybindCategory
import net.minecraft.client.KeyMapping
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper

interface Key {
    fun keyName(): String
    fun key(): Int
    fun whenPressed()
    fun keyType(): InputConstants.Type = InputConstants.Type.KEYSYM

    // Optional: override for keys that need to fire while a screen is open
    fun registerScreenEvents() {}

    fun register(): KeyMapping {
        val keyBinding = KeyMapping(keyName(), keyType(), key(), keybindCategory)
        return KeyMappingHelper.registerKeyMapping(keyBinding)

    }
}