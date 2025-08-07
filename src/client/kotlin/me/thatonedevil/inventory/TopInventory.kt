package me.thatonedevil.inventory

import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack

class TopInventory(client: MinecraftClient) {

    private val topInventory = client.player?.currentScreenHandler

    fun getTopInventory(): Any? {
        return topInventory
    }

    fun isTopInventoryEmpty(): Boolean {
        return topInventory?.stacks!!.isEmpty()
    }

    fun inventoryItems(): MutableList<ItemStack>? {
        return topInventory?.stacks?.stream()?.toList()
    }
}