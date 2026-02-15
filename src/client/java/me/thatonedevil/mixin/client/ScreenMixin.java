package me.thatonedevil.mixin.client;

import me.thatonedevil.YoinkGUIClient;
import me.thatonedevil.config.YoinkGuiSettings;
import me.thatonedevil.handlers.ParseButtonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.network.chat.Component.literal;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }

        if (!(client.screen instanceof InventoryScreen
                || client.screen instanceof ContainerScreen
                || client.screen instanceof MerchantScreen
                || client.screen instanceof CreativeModeInventoryScreen
                || client.screen instanceof ShulkerBoxScreen)) {
            return;
        }

        YoinkGuiSettings config = YoinkGUIClient.getYoinkGuiSettings();

        if (!config.getEnableYoinkButton().get()){
            return;
        }

        float scaleFactor = config.getButtonScaleFactor().get();

        int baseButtonWidth = 160;
        int baseButtonHeight = 20;

        int parseButtonX = config.getButtonX().get();
        int parseButtonY = config.getButtonY().get();
        int parseButtonWidth = (int) (baseButtonWidth * scaleFactor);
        int parseButtonHeight = (int) (baseButtonHeight * scaleFactor);
        String parseButtonText = "Yoink and Parse NBT into file";

        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();
        int mouseXUi = (int) (client.mouseHandler.xpos() * scaledWidth / client.getWindow().getScreenWidth());
        int mouseYUi = (int) (client.mouseHandler.ypos() * scaledHeight / client.getWindow().getScreenHeight());

        ParseButtonHandler.INSTANCE.setParseButtonHovered(mouseXUi >= parseButtonX && mouseXUi <= parseButtonX + parseButtonWidth &&
                mouseYUi >= parseButtonY && mouseYUi <= parseButtonY + parseButtonHeight);

        int parseBgColor = ParseButtonHandler.INSTANCE.getParseButtonHovered() ? 0xAA444444 : 0xAA000000;
        context.fill(parseButtonX, parseButtonY, parseButtonX + parseButtonWidth, parseButtonY + parseButtonHeight, parseBgColor);
        context.drawCenteredString(
                client.font,
                literal(parseButtonText),
                parseButtonX + parseButtonWidth / 2,
                parseButtonY + (parseButtonHeight - 8) / 2,
                0xFFFFFFFF
        );
    }

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    private static void onGetTooltipFromItem(Minecraft client, ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        if (!(client.screen instanceof AbstractContainerScreen)) {
            return;
        }

        YoinkGuiSettings config = YoinkGUIClient.getYoinkGuiSettings();

        if (!config.getEnableSingleItemYoink().get()) {
            return;
        }

        List<Component> originalTooltip = cir.getReturnValue();

        List<Component> modifiedTooltip = new ArrayList<>(originalTooltip);

        modifiedTooltip.add(literal(""));
        modifiedTooltip.add(literal("§ePress Y to Yoink item"));

        cir.setReturnValue(modifiedTooltip);
    }

}