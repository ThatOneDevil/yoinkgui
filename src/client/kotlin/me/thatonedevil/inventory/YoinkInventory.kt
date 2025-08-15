package me.thatonedevil.inventory

import me.thatonedevil.utils.Utils.sendChat
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps

class YoinkInventory(private val player: ClientPlayerEntity, private val inventory: TopInventory) {

    private val topInventory = inventory.getTopInventory()
    private val encodedItems = mutableListOf<NbtElement>()

    fun yoinkItems() {
        if (topInventory == null || inventory.isTopInventoryEmpty()) {
            sendChat("<color:#FF6961>No inventory found or it is empty!")
            return
        }

        val registryOps = player.registryManager.getOps(NbtOps.INSTANCE)
        inventory.inventoryItems()?.forEach { itemStack ->
            if (!itemStack.isEmpty) {
                val encodeResult = ItemStack.CODEC.encodeStart(registryOps, itemStack)
                encodeResult.result().ifPresent { nbtElement ->
                    encodedItems.add(nbtElement)
                }
            }
        }

    }

    fun getYoinkedItems(): List<NbtElement> = encodedItems
}
