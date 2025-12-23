package me.thatonedevil.inventory

import me.thatonedevil.utils.Utils.debug
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack

class TopInventory(client: MinecraftClient) {

    private val topInventory = client.player?.currentScreenHandler

    fun getTopInventory(): Any? {
        debug("Top Inventory: $topInventory")
        return topInventory
    }

    fun isTopInventoryEmpty(): Boolean {
        return topInventory?.stacks!!.isEmpty()
    }

    fun inventoryItems(): MutableList<ItemStack>? {
        val items = topInventory?.stacks?.stream()?.toList()
        debug("Items: $items")
        return items
    }
}