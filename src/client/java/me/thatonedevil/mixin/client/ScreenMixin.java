package me.thatonedevil.mixin.client;

import me.thatonedevil.YoinkGUIClient;
import me.thatonedevil.config.YoinkGuiSettings;
import me.thatonedevil.handlers.ItemParseHandler;
import me.thatonedevil.keybinds.YoinkSingleKeybind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final String BUTTON_TEXT = "Yoink and Parse NBT into file";

    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    @Inject(method = "init(II)V", at = @At("TAIL"))
    private void yoink$addParseButton(int width, int height, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();

        if (client.player == null || client.level == null) {
            return;
        }

        Screen screen = client.gui.screen();
        if (!(screen instanceof InventoryScreen
                || screen instanceof ContainerScreen
                || screen instanceof MerchantScreen
                || screen instanceof CreativeModeInventoryScreen
                || screen instanceof ShulkerBoxScreen)) {
            return;
        }

        YoinkGuiSettings config = YoinkGUIClient.getYoinkGuiSettings();
        if (!config.getEnableYoinkButton().get()) {
            return;
        }

        int buttonWidth = Math.max(20, Math.round(BUTTON_WIDTH * config.getButtonScaleFactor().get()));
        Button button = Button.builder(
                        Component.literal(BUTTON_TEXT),
                        ignored -> ItemParseHandler.INSTANCE.handleParseButton(client))
                .bounds(config.getButtonX().get(), config.getButtonY().get(), buttonWidth, BUTTON_HEIGHT)
                .build();

        this.addRenderableWidget(button);
    }

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    private static void yoink$addSingleItemHint(
            Minecraft minecraft,
            ItemStack itemStack,
            CallbackInfoReturnable<List<Component>> cir
    ) {
        if (!(minecraft.gui.screen() instanceof AbstractContainerScreen)) {
            return;
        }

        YoinkGuiSettings config = YoinkGUIClient.getYoinkGuiSettings();
        if (!config.getEnableSingleItemYoink().get()) {
            return;
        }

        List<Component> modifiedTooltip = new ArrayList<>(cir.getReturnValue());
        Component key = YoinkSingleKeybind.keyMapping.getTranslatedKeyMessage();

        modifiedTooltip.add(Component.literal(""));
        modifiedTooltip.add(Component.literal("§ePress §6" + key.getString() + " §eto Yoink item"));
        cir.setReturnValue(modifiedTooltip);
    }
}
