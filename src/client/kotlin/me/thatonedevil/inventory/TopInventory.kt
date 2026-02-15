package me.thatonedevil.inventory

import me.thatonedevil.utils.Utils.debug
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.MerchantMenu
import net.minecraft.world.inventory.AbstractContainerMenu

class TopInventory(private val client: Minecraft) {

    private val currentScreenHandler
        get() = client.player?.containerMenu

    fun getTopInventory(): Any? {
        return currentScreenHandler?.also {
            debug("Top Inventory: $it")
        }
    }

    fun isTopInventoryEmpty(): Boolean {
        return currentScreenHandler?.items?.isEmpty() ?: true
    }

    fun inventoryItems(): List<ItemStack> {
        val handler = currentScreenHandler ?: run {
            debug("Top inventory is null")
            return emptyList()
        }

        return when (handler) {
            is MerchantMenu -> getTradeItems(handler)
            else -> getRegularItems(handler)
        }
    }

    private fun getTradeItems(handler: MerchantMenu): List<ItemStack> {
        val tradeItems = handler.offers.flatMap { tradeOffer ->
            buildList {
                add(tradeOffer.itemCostA.itemStack)
                tradeOffer.itemCostB.ifPresent { add(it.itemStack) }
                add(tradeOffer.result)
            }
        }.filterNot { it.isEmpty }

        debug("Trade items: $tradeItems")
        return tradeItems
    }

    private fun getRegularItems(handler: Any): List<ItemStack> {
        return (handler as? AbstractContainerMenu)
            ?.items
            ?.filterNot { it.isEmpty }
            ?.also { debug("Items: $it") }
            ?: emptyList()
    }

}