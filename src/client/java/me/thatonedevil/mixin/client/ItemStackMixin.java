package me.thatonedevil.mixin.client;

import me.thatonedevil.YoinkGUIClient;
import me.thatonedevil.config.YoinkGuiSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void onGetTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
        List<Text> tooltip = cir.getReturnValue();
        YoinkGuiSettings config = YoinkGUIClient.getYoinkGuiSettings();

        if (!config.getEnableSingleItemYoink().get()) {
            return;
        }

        tooltip.add(Text.literal(""));

        tooltip.add(Text.literal("§ePress X to yoink item"));

        YoinkGUIClient.setHoveredItemStack((ItemStack)(Object)this);
    }
}

