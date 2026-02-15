package me.thatonedevil.keybinds

//? if >=1.21.9 {
import me.thatonedevil.YoinkGUIClient.keybindCategory
//? }
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import com.mojang.blaze3d.platform.InputConstants

interface Key {
    fun keyName(): String
    fun key(): Int

    fun whenPressed()
    fun keyType(): InputConstants.Type = InputConstants.Type.KEYSYM

    fun register(): KeyMapping {
        //? if >=1.21.9 {
        val keyBinding = KeyMapping(
            keyName(),
            keyType(),
            key(),
            keybindCategory
        )
        //? } else {
        /*val keyBinding = KeyMapping(
            keyName(),
            keyType(),
            key(),
            "key.category.minecraft.keybinds"
        )
        *///?}

        return KeyBindingHelper.registerKeyBinding(keyBinding)
    }



}