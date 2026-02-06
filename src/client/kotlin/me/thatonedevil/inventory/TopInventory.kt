package me.thatonedevil.inventory

import me.thatonedevil.utils.Utils.debug
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.screen.MerchantScreenHandler
import net.minecraft.screen.ScreenHandler

class TopInventory(private val client: MinecraftClient) {

    private val currentScreenHandler
        get() = client.player?.currentScreenHandler

    fun getTopInventory(): Any? {
        return currentScreenHandler?.also {
            debug("Top Inventory: $it")
        }
    }

    fun isTopInventoryEmpty(): Boolean {
        return currentScreenHandler?.stacks?.isEmpty() ?: true
    }

    fun inventoryItems(): List<ItemStack> {
        val handler = currentScreenHandler ?: run {
            debug("Top inventory is null")
            return emptyList()
        }

        return when (handler) {
            is MerchantScreenHandler -> getTradeItems(handler)
            else -> getRegularItems(handler)
        }
    }

    private fun getTradeItems(handler: MerchantScreenHandler): List<ItemStack> {
        val tradeItems = handler.recipes.flatMap { tradeOffer ->
            buildList {
                add(tradeOffer.firstBuyItem.itemStack)
                tradeOffer.secondBuyItem.ifPresent { add(it.itemStack) }
                add(tradeOffer.sellItem)
            }
        }.filterNot { it.isEmpty }

        debug("Trade items: $tradeItems")
        return tradeItems
    }

    private fun getRegularItems(handler: Any): List<ItemStack> {
        return (handler as? ScreenHandler)
            ?.stacks
            ?.filterNot { it.isEmpty }
            ?.also { debug("Items: $it") }
            ?: emptyList()
    }

}