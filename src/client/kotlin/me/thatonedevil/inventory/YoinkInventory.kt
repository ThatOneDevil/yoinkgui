package me.thatonedevil.inventory

import me.thatonedevil.utils.Utils.sendChat
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.Tag
import net.minecraft.nbt.NbtOps

class YoinkInventory(private val player: LocalPlayer, private val inventory: TopInventory) {

    private val topInventory = inventory.getTopInventory()
    private val encodedItems = mutableListOf<Tag>()

    fun yoinkItems() {
        if (topInventory == null || inventory.isTopInventoryEmpty()) {
            sendChat("<color:#FF6961>No inventory found or it is empty!")
            return
        }

        val registryOps = player.registryAccess().createSerializationContext(NbtOps.INSTANCE)
        inventory.inventoryItems().forEach { itemStack ->
            if (!itemStack.isEmpty) {
                val encodeResult = ItemStack.CODEC.encodeStart(registryOps, itemStack)
                encodeResult.result().ifPresent { nbtElement ->
                    encodedItems.add(nbtElement)
                }
            }
        }

    }

    companion object {
        fun yoinkSingleItem(player: LocalPlayer, itemStack: ItemStack): String? {
            if (itemStack.isEmpty) {
                sendChat("<color:#FF6961>No inventory found or item is empty!")
                return null
            }

            val registryOps = player.registryAccess().createSerializationContext(NbtOps.INSTANCE)
            val encodeResult = ItemStack.CODEC.encodeStart(registryOps, itemStack)
            return encodeResult.result().get().toString()
        }
    }


    fun getYoinkedItems(): List<Tag> = encodedItems
}
