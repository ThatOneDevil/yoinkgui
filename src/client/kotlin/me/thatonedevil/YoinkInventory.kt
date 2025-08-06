package me.thatonedevil

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps

class YoinkInventory(private val player: ClientPlayerEntity, private val inventory: TopInventory) {

    private val topInventory = inventory.getTopInventory()
    private val encodedItems = mutableListOf<NbtElement>()

    fun yoinkItems() {
        if (topInventory == null || inventory.isTopInventoryEmpty()) {
            println("No top inventory found or it is empty!")
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