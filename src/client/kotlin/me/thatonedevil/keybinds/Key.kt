package me.thatonedevil.keybinds

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier

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
            newKeybindCategory
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

    companion object {
        val newKeybindCategory: KeyBinding.Category =
            KeyBinding.Category.create(Identifier.of("keybinds"))
    }


}