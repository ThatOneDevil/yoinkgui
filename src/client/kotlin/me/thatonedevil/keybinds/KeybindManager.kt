package me.thatonedevil.keybinds

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.KeyMapping

class KeybindManager {

    private val keys: List<Key> = listOf(
        MenuKeybind(), YoinkSingleKeybind()
    )

    private val bindings = mutableMapOf<KeyMapping, Key>()

    fun register() {
        keys.forEach { key ->
            val binding = key.register()
            bindings[binding] = key
        }

        ClientTickEvents.END_CLIENT_TICK.register {
            bindings.forEach { (binding, key) ->
                if (binding.consumeClick()) {
                    key.whenPressed()
                }
            }
        }
    }
}
