package me.thatonedevil.keybinds

import me.thatonedevil.YoinkGUIClient.keybindCategory
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

interface Key {
    fun keyName(): String
    fun key(): Int

    fun whenPressed()
    fun keyType(): InputUtil.Type = InputUtil.Type.KEYSYM

    fun register(): KeyBinding {
        //? if >=1.21.9 {
        val keyBinding = KeyBinding(
            keyName(),
            keyType(),
            key(),
            keybindCategory
        )
        //? } else {
        /*val keyBinding = KeyBinding(
            keyName(),
            keyType(),
            key(),
            "key.category.minecraft.keybinds"
        )*/
        //?}

        return KeyBindingHelper.registerKeyBinding(keyBinding)
    }



}