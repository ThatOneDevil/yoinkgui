package me.thatonedevil.mixin.client;

import me.thatonedevil.YoinkGUIClient;
import me.thatonedevil.config.YoinkGuiSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.text.Text.literal;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        if (!(client.currentScreen instanceof InventoryScreen
                || client.currentScreen instanceof GenericContainerScreen
                || client.currentScreen instanceof MerchantScreen
                || client.currentScreen instanceof CreativeInventoryScreen
                || client.currentScreen instanceof ShulkerBoxScreen)) {
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

        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        int mouseXUi = (int) (client.mouse.getX() * scaledWidth / client.getWindow().getWidth());
        int mouseYUi = (int) (client.mouse.getY() * scaledHeight / client.getWindow().getHeight());

        YoinkGUIClient.INSTANCE.setParseButtonHovered(mouseXUi >= parseButtonX && mouseXUi <= parseButtonX + parseButtonWidth &&
                mouseYUi >= parseButtonY && mouseYUi <= parseButtonY + parseButtonHeight);

        int parseBgColor = YoinkGUIClient.INSTANCE.getParseButtonHovered() ? 0xAA444 : 0xAA000;
        context.fill(parseButtonX, parseButtonY, parseButtonX + parseButtonWidth, parseButtonY + parseButtonHeight, parseBgColor);
        context.drawCenteredTextWithShadow(
                client.textRenderer,
                literal(parseButtonText),
                parseButtonX + parseButtonWidth / 2,
                parseButtonY + (parseButtonHeight - 8) / 2,
                0xFFFFF
        );

    }

}